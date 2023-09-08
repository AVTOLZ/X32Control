package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.X32OSC
import dev.tiebe.avt.x32.api.fader.*
import dev.tiebe.avt.x32.api.fader.Color
import dev.tiebe.avt.x32.api.fader.Eq.Companion.getFilterType
import dev.tiebe.avt.x32.api.fader.Eq.Companion.getFilterValue
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.getBus
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.api.internal.Status
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import dev.tiebe.avt.x32.utils.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.round

class EQFaderSync(val osc: X32OSC): Command {
    override var arguments: List<Any> = listOf()

    companion object {
        const val updateFrequency = 30

        @Volatile var savedFaderSettings: MutableMap<Fader, FaderSettings> = mutableMapOf()
        @Volatile var savedFaderBankState: Pair<Status.Companion.ChannelFaderBank, Status.Companion.GroupFaderBank>? = null
        val subscriptions = mutableListOf<UUID>()
    }
    override fun setArguments(args: List<String>): Command? {
        return if (args.size == 2 && args[1].toBooleanStrictOrNull() != null) {
            arguments = listOf(args[1].toBooleanStrict())
            this
        } else {
            arguments = listOf(subscriptions.isEmpty())
            null
        }
    }

    private val faders = List(16) { osc.getChannel(it + 17) }.associateWith { -0.1 }
    private val frequencies = List(faders.size) { index -> index.toDouble().mapToLin(0..15, 20.0..20000.0) }

    private val settingsFaders = List(4) { osc.getBus(9 + it) }

    private var bands: List<EQBand> = listOf()

    override fun run() {
        if (arguments[0] as Boolean) {
            runBlocking {
                val fader = osc.getStatus().getSelection()

                savedFaderSettings = (faders.keys.associateWith { FaderSettings.fromFader(it) } + settingsFaders.associateWith { FaderSettings.fromFader(it) }).toMutableMap()
                changeFaderSettings()

                val channelFaderBank = osc.getStatus().getChannelFaderBank()
                val groupFaderBank = osc.getStatus().getGroupFaderBank()

                savedFaderBankState = channelFaderBank to groupFaderBank

                osc.getStatus().setChannelFaderBank(Status.Companion.ChannelFaderBank.CH17_32)
                osc.getStatus().setGroupFaderBank(Status.Companion.GroupFaderBank.BUS9_16)

                subscribeEQ(fader)
                subscribeSolo(fader)
                subscribeSettings(fader)

                subscribeFaders(fader)

                Runtime.getRuntime().addShutdownHook(Thread {
                    for (sub in subscriptions) {
                        osc.unsubscribe(sub)
                    }

                    println("Restoring settings..")
                    savedFaderSettings.forEach {
                        it.value.forceApplyTo(it.key)
                    }

                    if (savedFaderBankState == null) return@Thread

                    osc.getStatus().setChannelFaderBank(savedFaderBankState!!.first)
                    osc.getStatus().setGroupFaderBank(savedFaderBankState!!.second)
                })
            }
        } else {
            for (sub in subscriptions) {
                osc.unsubscribe(sub)
            }

            savedFaderSettings.forEach {
                it.value.applyTo(it.key)
            }

            if (savedFaderBankState == null) return
            osc.getStatus().setChannelFaderBank(savedFaderBankState!!.first)
            osc.getStatus().setGroupFaderBank(savedFaderBankState!!.second)

            subscriptions.clear()
            savedFaderSettings.clear()
            savedFaderBankState = null
        }
    }

    private fun changeFaderSettings() {
        faders.keys.forEachIndexed { index, fader ->
            fader.mix.setMute(true)
            fader.config.setSolo(false)
            fader.config.setSource(43) //fx, almost always unused. cant be off, otherwise the led wont be on
            fader.mix.setStereo(false)
            fader.mix.setMono(false)
            fader.mix.setLevel(0.0f)

            fader.config.setColor(Color.CYAN, false)
            fader.config.setName(frequencies[index].toInt().toString())
            fader.config.setIcon(Icon.BLANK)
        }

        settingsFaders.forEachIndexed { index, bus ->
            bus.mix.setMute(true)
            bus.config.setSolo(false)
            bus.config.setSource(43) //fx, almost always unused. cant be off, otherwise the led wont be on
            bus.mix.setStereo(false)
            bus.mix.setMono(false)
            bus.mix.setLevel(0.0f)

            bus.config.setColor(Color.MAGENTA, false)
            bus.config.setIcon(Icon.BLANK)

            when (index) {
                0 -> {
                    bus.config.setName("Q")
                }
                1 -> {
                    bus.config.setName("Frequency")
                }
                2 -> {
                    bus.config.setName("Gain")
                }
                3 -> {
                    bus.config.setName("Type")
                }
            }
        }
    }

    @Volatile private var settingsChangingFader: Pair<Fader?, Long> = null to 0

    private fun subscribeSettings(fader: Fader) {
        settingsFaders.forEachIndexed { index, bus ->
            subscriptions.add(
                osc.subscribe(bus.mix.levelOSCCommand, updateFrequency) { message ->
                    val level = message.message.arguments[0] as Float

                    if (selectedBand == -1) return@subscribe

                    settingsChangingFader = bus to System.currentTimeMillis()

                    when (index) {
                        0 -> {
                            bands[selectedBand - 1].q = level.toDouble()
                            fader.eq.setQ(selectedBand, level)
                            runCalculations(bands)
                        }
                        1 -> {
                            bands[selectedBand - 1].freq = level.toDouble()
                            fader.eq.setFrequency(selectedBand, level)
                            runCalculations(bands)
                        }
                        2 -> {
                            bands[selectedBand - 1].gain = level.toDouble()
                            fader.eq.setGain(selectedBand, level)
                            runCalculations(bands)
                        }
                        3 -> {
                            val newType = getFilterType(round(level * 5).toInt())
                            bands[selectedBand - 1].type = newType
                            fader.eq.setType(selectedBand, newType)

                            bus.config.setName(newType.toString())
                        }
                    }

                    Thread {
                        Thread.sleep(505)
                        if (index == 3) {
                            if ((settingsChangingFader.first == bus && System.currentTimeMillis() - settingsChangingFader.second > 500) || settingsChangingFader.first != bus) {
                                bus.mix.setLevel(getFilterValue(bands[selectedBand - 1].type) / 5f)
                            }
                        }

                        if (settingsChangingFader.first == bus && System.currentTimeMillis() - settingsChangingFader.second > 500) {
                            runCalculations(bands)
                        }
                    }.start()
                }
            )
        }
    }

    private var selectedBand = -1

    private fun subscribeSolo(fader: Fader) {
        val soloFaders = List(fader.eqAmount) { faders.keys.toList()[it] }

        for (soloFader in soloFaders) {
            subscriptions.add(
                osc.subscribe("/-stat/solosw/${soloFader.idString}", updateFrequency) { message ->
                    val solo = message.message.arguments[0] as Int
                    if (solo == 1) {
                        for (otherSoloFader in soloFaders) {
                            if (otherSoloFader == soloFader) continue
                            otherSoloFader.config.setSolo(false)
                            otherSoloFader.config.setColor(Color.CYAN, false)
                        }

                        soloFader.config.setColor(Color.CYAN, true)

                        println("Soloing band ${soloFaders.indexOf(soloFader) + 1}")
                        selectedBand = soloFaders.indexOf(soloFader) + 1

                        settingsFaders[0].mix.setLevel(bands[selectedBand - 1].q.toFloat())
                        settingsFaders[1].mix.setLevel(bands[selectedBand - 1].freq.toFloat())
                        settingsFaders[2].mix.setLevel(bands[selectedBand - 1].gain.toFloat())
                        settingsFaders[3].mix.setLevel(getFilterValue(bands[selectedBand - 1].type) / 5f)

                        settingsFaders[0].config.setName(String.format("%.2f", bands[selectedBand - 1].q.toX32Q()))
                        settingsFaders[1].config.setName(String.format("%.2f", bands[selectedBand - 1].freq.toX32Frequency()))
                        settingsFaders[2].config.setName(String.format("%.2f", bands[selectedBand - 1].gain.toX32Gain()))
                        settingsFaders[3].config.setName(bands[selectedBand - 1].type.toString())

                    } else {
                        if (selectedBand == soloFaders.indexOf(soloFader) + 1) {
                            soloFader.config.setColor(Color.CYAN, false)

                            settingsFaders[0].mix.setLevel(0.0f)
                            settingsFaders[1].mix.setLevel(0.0f)
                            settingsFaders[2].mix.setLevel(0.0f)
                            settingsFaders[3].mix.setLevel(0.0f)

                            settingsFaders[0].config.setName("Q")
                            settingsFaders[1].config.setName("Frequency")
                            settingsFaders[2].config.setName("Gain")
                            settingsFaders[3].config.setName("Type")

                            selectedBand = -1
                        }
                    }
                }
            )
        }
    }

    @Volatile var changingFader: Pair<Fader?, Long> = null to 0
    private fun subscribeFaders(fader: Fader) {
        faders.keys.forEachIndexed { index, eqFader ->
            subscriptions.add(
                osc.subscribe(eqFader.mix.levelOSCCommand, updateFrequency) { message ->
                    val level = message.message.arguments[0] as Float

                    if (selectedBand == -1) return@subscribe

                    changingFader = eqFader to System.currentTimeMillis()

                    bands[selectedBand - 1].freq = frequencies[index].fromX32Frequency()
                    bands[selectedBand - 1].gain = level.toDouble()

                    fader.eq.setFrequency(selectedBand, frequencies[index].fromX32Frequency().toFloat())
                    fader.eq.setGain(selectedBand, level)

                    runCalculations(bands)

                    Thread {
                        Thread.sleep(505)
                        if (changingFader.first == eqFader && System.currentTimeMillis() - changingFader.second > 500) {
                            runCalculations(bands)
                        }
                    }.start()
                }
            )
        }
    }

    private fun subscribeEQ(fader: Fader) {
        bands = runBlocking {
             List(fader.eqAmount) { EQBand(fader.eq.getType(it + 1), fader.eq.getFrequency(it + 1).toDouble(), fader.eq.getGain(it + 1).toDouble(), fader.eq.getQ(it + 1).toDouble()) }
        }

        runCalculations(bands)

        subscribeType(fader) { band, newType ->
            val eqband = bands[band-1]

            if (eqband.type == newType) return@subscribeType
            bands[band - 1].type = newType
            runCalculations(bands)
        }
        subscribeFreq(fader) { band, newFreq ->
            val eqband = bands[band-1]

            if (eqband.freq == newFreq) return@subscribeFreq
            bands[band - 1].freq = newFreq
            runCalculations(bands)
        }
        subscribeQ(fader) { band, newQ ->
            val eqband = bands[band-1]

            if (eqband.q == newQ) return@subscribeQ
            bands[band - 1].q = newQ
            runCalculations(bands)
        }
        subscribeGain(fader) { band, newGain ->
            val eqband = bands[band-1]

            if (eqband.gain == newGain) return@subscribeGain
            bands[band - 1].gain = newGain
            runCalculations(bands)
        }
    }

    private fun runCalculations(bands: List<EQBand>) {
        val biquads = bands.map {
            val frequency = findNearestValue(it.freq.toX32Frequency(), frequencies).fromX32Frequency()
            it.copy(freq = frequency).getBiquad()
        }

        faders.entries.forEachIndexed { index, (fader, currentValue) ->
            if (changingFader.first == fader && System.currentTimeMillis() - changingFader.second < 500) return@forEachIndexed

            val freqAtFader = frequencies[index]
            var total = 0.0

            for (biquad in biquads) {
                total += biquad.result(freqAtFader) - 1
            }

            total += 1

            var db = 20 * log10(total)

            if (db < -15) db = -15.0 else if (db > 15) db = 15.0
            val newValue = ((db / 15.0f) + 1) /2.0

            if (currentValue == newValue) return@forEachIndexed
            fader.mix.setLevel(newValue.toFloat())
        }

        if (selectedBand == -1) {
            settingsFaders[0].mix.setLevel(0.0f)
            settingsFaders[1].mix.setLevel(0.0f)
            settingsFaders[2].mix.setLevel(0.0f)
            settingsFaders[3].mix.setLevel(0.0f)

            settingsFaders[0].config.setName("Q")
            settingsFaders[1].config.setName("Frequency")
            settingsFaders[2].config.setName("Gain")
            settingsFaders[3].config.setName("Type")

            return
        }
        settingsFaders.forEachIndexed { index, bus ->
            if (settingsChangingFader.first == bus && System.currentTimeMillis() - settingsChangingFader.second < 500) return@forEachIndexed
            if (index == 3) return@forEachIndexed
            val eqband = bands[selectedBand - 1]

            when (index) {
                0 -> {
                    bus.mix.setLevel(eqband.q.toFloat())
                    bus.config.setName(String.format("%.2f", eqband.q.toX32Q()))
                }
                1 -> {
                    bus.mix.setLevel(eqband.freq.toFloat())
                    bus.config.setName(String.format("%.2f", eqband.freq.toX32Frequency()))
                }
                2 -> {
                    bus.mix.setLevel(eqband.gain.toFloat())
                    bus.config.setName(String.format("%.2f", eqband.gain.toX32Gain()))
                }
            }
        }
    }

    private fun subscribeType(fader: Fader, onReceive: (Int, BiQuadraticFilter.Companion.FilterType) -> Unit) {
        repeat(fader.eqAmount) {
            subscriptions.add(
                osc.subscribe(fader.eq.typeOSCMessage.format(it + 1), updateFrequency) { message ->
                    onReceive(it + 1, getFilterType((message.message.arguments[0] as Int)))
                }
            )
        }
    }

    private fun subscribeFreq(fader: Fader, onReceive: (Int, Double) -> Unit) {
        repeat(fader.eqAmount) {
            subscriptions.add(
                osc.subscribe(fader.eq.frequencyOSCMessage.format(it + 1), updateFrequency) { message ->
                    onReceive(it + 1, (message.message.arguments[0] as Float).toDouble())
                }
            )
        }
    }

    private fun subscribeGain(fader: Fader, onReceive: (Int, Double) -> Unit) {
        repeat(fader.eqAmount) {
            subscriptions.add(
                osc.subscribe(fader.eq.gainOSCMessage.format(it + 1), updateFrequency) { message ->
                    onReceive(it + 1, (message.message.arguments[0] as Float).toDouble())
                }
            )
        }
    }

    private fun subscribeQ(fader: Fader, onReceive: (Int, Double) -> Unit) {
        repeat(fader.eqAmount) {
            subscriptions.add(
                osc.subscribe(fader.eq.qOSCMessage.format(it + 1), updateFrequency) { message ->
                    onReceive(it + 1, (message.message.arguments[0] as Float).toDouble())
                }
            )
        }
    }

    data class EQBand(var type: BiQuadraticFilter.Companion.FilterType, var freq: Double, var gain: Double, var q: Double) {
        fun getBiquad(): BiQuadraticFilter {
            val mappedFreq = 20.0 * (10.0).pow(3 * freq)
            val mappenGain = -15.0 + gain * (15.0 - -15.0)
            val mappedQ = 10 * (0.3 / 10.0).pow(q)

            return BiQuadraticFilter(type, mappedFreq, 44100.0, mappedQ, mappenGain)
        }

    }
}