package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.advancedTestBands
import dev.tiebe.avt.x32.api.fader.*
import dev.tiebe.avt.x32.api.fader.Color
import dev.tiebe.avt.x32.api.fader.Eq.Companion.getFilterType
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.getFaderFromIndex
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import dev.tiebe.avt.x32.utils.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

class EQFaderSync(val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()

    companion object {
        const val updateFrequency = 30

        var savedFaderSettings: List<FaderSettings> = listOf()
        val subscriptions = mutableListOf<UUID>()
    }
    override fun setArguments(args: List<String>): Command? {
        return if (args.size == 2) {
            arguments = listOf(args[1].toBoolean())
            this
        } else {
            null
        }
    }

    private val faders = List(16) { osc.getFaderFromIndex((it + 17).toString()) }
    private val frequencies = List(faders.size) { index -> index.toDouble().mapToLin(0..15, 20.0..20000.0) }

    private var bands: List<EQBand> = listOf()

    override fun run() {
        if (arguments[0] as Boolean) {
            runBlocking {
                val fader = osc.getStatus().getSelection()

                savedFaderSettings = List(16) { FaderSettings.fromFader(osc.getFaderFromIndex((it + 17).toString())) }
                changeFaderSettings()

                subscribeEQ(fader)
                subscribeSolo(fader)
                subscribeFaders(fader)
            }
        } else {
            for (sub in subscriptions) {
                osc.unsubscribe(sub)
            }

            for (i in 0..15) {
                savedFaderSettings[i].applyTo(osc.getFaderFromIndex((i + 17).toString()))
            }
        }
    }

    private fun changeFaderSettings() {
        faders.forEachIndexed { index, fader ->
            fader.mix.setMute(true)
            fader.config.setSolo(false)
            fader.config.setSource(43) //fx, almost always unused. cant be off, otherwise the led wont be on
            fader.mix.setStereo(false)
            fader.mix.setMono(false)

            fader.config.setColor(Color.CYAN, false)
            fader.config.setName(frequencies[index].toInt().toString())
            fader.config.setIcon(Icon.BLANK)
        }
    }

    private var selectedBand = -1

    private fun subscribeSolo(fader: Fader) {
        val soloFaders = List(fader.eqAmount) { faders[it] }
        println(faders[0].idString)

        for (soloFader in soloFaders) {
            subscriptions.add(
                osc.subscribe("/-stat/solosw/${soloFader.idString}", updateFrequency) { message ->
                    val solo = message.message.arguments[0] as Int
                    if (solo == 1) {
                        for (otherSoloFader in soloFaders) {
                            otherSoloFader.config.setSolo(false)
                            otherSoloFader.config.setColor(Color.CYAN, false)
                        }

                        soloFader.config.setColor(Color.CYAN, true)

                        println("Soloing band ${soloFaders.indexOf(soloFader) + 1}")
                        selectedBand = soloFaders.indexOf(soloFader) + 1
                    }
                }
            )
        }
    }

    @Volatile var changingFader: Pair<Fader?, Long> = null to 0
    private fun subscribeFaders(fader: Fader) {
        faders.forEachIndexed { index, eqFader ->
            subscriptions.add(
                osc.subscribe(eqFader.mix.levelOSCCommand, updateFrequency) { message ->
                    val level = message.message.arguments[0] as Float

                    if (selectedBand == -1) return@subscribe

                    changingFader = eqFader to System.currentTimeMillis()

                    bands[selectedBand - 1].freq = frequencies[index].fromX32Frequency()
                    bands[selectedBand - 1].gain = level.toDouble()

                    fader.eq.setFrequency(selectedBand, frequencies[index].fromX32Frequency().toFloat())
                    fader.eq.setGain(selectedBand, level)

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
            println(frequency)
            it.copy(freq = frequency).getBiquad()
        }

        faders.forEachIndexed { index, fader ->
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

            fader.mix.setLevel(newValue.toFloat())
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