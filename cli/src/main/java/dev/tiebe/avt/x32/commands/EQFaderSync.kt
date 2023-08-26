package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.advancedTestBands
import dev.tiebe.avt.x32.api.fader.Eq
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

        var synced = false
        val subscriptions = mutableListOf<UUID>()
    }
    override fun setArguments(args: List<String>): Command? {
        if (args.size == 2) {
            arguments = listOf(args[1].toBoolean())
            return this
        } else {
            return null
        }
    }

    override fun run() {
        if (arguments[0] as Boolean) {
            runBlocking {
                val fader = osc.getStatus().getSelection()
                subscribeEQ(fader)
            }
        } else {
            for (sub in subscriptions) {
                osc.unsubscribe(sub)
            }
        }
    }

    private fun subscribeEQ(fader: Fader) {
        val bands = runBlocking {
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

    private val faders = List(16) { osc.getFaderFromIndex((it + 17).toString()).also { it.mix.setMute(true) } }

    private fun runCalculations(bands: List<EQBand>) {
        val frequencies = List(faders.size) { index -> index.toDouble().mapToLin(0..15, 20.0..20000.0) }

        //TODO: check nearest fader to every bands frequency, and calculate from there
        val biquads = bands.map {
            val frequency = findNearestValue(it.freq.toX32Frequency(), frequencies).fromX32Frequency()
            println(frequency)
            it.copy(freq = frequency).getBiquad()
        }

        faders.forEachIndexed { index, fader ->
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