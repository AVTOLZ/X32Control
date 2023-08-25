package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Eq
import dev.tiebe.avt.x32.api.fader.Fader
import java.util.*

class EQFaderSync(val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()

    //gain is van 0-1 (-15db-15db), freq is van 0-1 (20hz-20khz), q is van 0-1 (10-0.3 (nee, niet 0.3-10, 0 is 10, 1 is 0.3))
    val testBands = listOf(EQBand(type = Eq.Companion.EQType.LShv, freq =0.245, gain =0.9, q =0.46478873), EQBand(
        type = Eq.Companion.EQType.PEQ, freq =0.505, gain =0.9583333, q =0.1971831), EQBand(type = Eq.Companion.EQType.VEQ, freq =0.71, gain =0.0, q =0.0), EQBand(
        type = Eq.Companion.EQType.HShv, freq =0.925, gain =0.85833335, q =0.46478873))

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


/*            runBlocking {
                val fader = osc.getStatus().getSelection()
                subscribeEQ(fader)
            }*/
        } else {
            for (sub in subscriptions) {
                osc.unsubscribe(sub)
            }
        }
    }

    private fun subscribeEQ(fader: Fader) {
        val bands = MutableList(fader.eqAmount) { EQBand(Eq.Companion.EQType.LCut, 0.0, 0.0, 0.0) }

        subscribeType(fader) { band, newType ->
            bands[band - 1].type = newType
            calculateY(400f, bands)
        }
        subscribeFreq(fader) { band, newFreq ->
            bands[band - 1].freq = newFreq
            calculateY(400f, bands)
        }
        subscribeQ(fader) { band, newQ ->
            bands[band - 1].q = newQ
            calculateY(400f, bands)
        }
        subscribeGain(fader) { band, newGain ->
            bands[band - 1].gain = newGain
            calculateY(400f, bands)
        }

        Thread.sleep(2000)

        println(bands)
    }

    private fun subscribeType(fader: Fader, onReceive: (Int, Eq.Companion.EQType) -> Unit) {
        repeat(fader.eqAmount) {
            subscriptions.add(
                osc.subscribe(fader.eq.typeOSCMessage.format(it + 1), updateFrequency) { message ->
                    onReceive(it + 1, Eq.Companion.EQType.entries[message.message.arguments[0] as Int])
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

    private fun calculateY(calculateFreq: Float, currentEq: List<EQBand>) {
        for (eq in currentEq) {
            when (eq.type) {
                //Eq.Companion.EQType.LCut -> calculateYLCut(calculateFreq, eq)
                Eq.Companion.EQType.LShv -> calculateYLShv(calculateFreq, eq)
/*                Eq.Companion.EQType.PEQ -> calculateYPEQ(calculateFreq, eq)
                Eq.Companion.EQType.VEQ -> calculateYVEQ(calculateFreq, eq)*/
                else -> {}
            }


        }
    }

    private fun calculateYLShv(calculateFreq: Float, lShv: EQBand): Double {
        return 0.0
    }


    data class EQBand(var type: Eq.Companion.EQType, var freq: Double, var gain: Double, var q: Double)
}