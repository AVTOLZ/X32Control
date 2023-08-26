package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Eq
import dev.tiebe.avt.x32.api.fader.Eq.Companion.getFilterType
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.getFaderFromIndex
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import dev.tiebe.avt.x32.utils.fromX32Gain
import dev.tiebe.avt.x32.utils.toX32Gain
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.math.log10
import kotlin.math.pow

class EQFaderSync(val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()

    //gain is van 0-1 (-15db-15db), freq is van 0-1 (20hz-20khz), q is van 0-1 (10-0.3 (nee, niet 0.3-10, 0 is 10, 1 is 0.3))
    val testBands = listOf(
        EQFaderSync.EQBand(type = BiQuadraticFilter.Companion.FilterType.LOWSHELF, freq = 0.245, gain = 0.9, q = 0.46478873),
        EQFaderSync.EQBand(
            type = BiQuadraticFilter.Companion.FilterType.PEAK, freq = 0.505, gain = 0.9583333, q = 0.1971831
        ),
        EQFaderSync.EQBand(type = BiQuadraticFilter.Companion.FilterType.PEAK, freq = 0.71, gain = 0.0, q = 0.0),
        EQFaderSync.EQBand(
            type = BiQuadraticFilter.Companion.FilterType.HIGHSHELF, freq = 0.925, gain = 0.85833335, q = 0.46478873
        )
    )

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

        println(bands)

        subscribeType(fader) { band, newType ->
            bands[band - 1].type = newType
        }
        subscribeFreq(fader) { band, newFreq ->
            bands[band - 1].freq = newFreq
        }
        subscribeQ(fader) { band, newQ ->
            bands[band - 1].q = newQ
        }
        subscribeGain(fader) { band, newGain ->
            bands[band - 1].gain = newGain
            runCalculations(bands)
        }
    }

    private val faders = List(16) { osc.getFaderFromIndex((it + 17).toString()).also { it.mix.setMute(true) } }

    private fun runCalculations(bands: List<EQBand>) {
        val biquads = bands.map { it.getBiquad() }

        faders.forEachIndexed { index, fader ->
            val freqAtFader = 20.0 * 10.0.pow(3 * index.toDouble())
            var total = 0.0

            for (biquad in biquads) {
                total += biquad.log_result(freqAtFader)
            }

            println(total)
            println(total.toX32Gain().toFloat())

            var gain = total.toX32Gain().toFloat()
            if (gain < 0) {
                gain = 0f
            } else if (gain > 1) {
                gain = 1f
            }
            fader.mix.setLevel(gain)
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