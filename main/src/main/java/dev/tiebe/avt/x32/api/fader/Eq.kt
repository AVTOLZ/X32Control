package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import kotlin.math.log10

class Eq(val fader: Fader) {
    open val stateOSCMessage = "/${fader.classString}/${fader.idString}/eq/on"

    fun setState(state: Boolean) {
        fader.oscController.sendMessage(OSCMessage(stateOSCMessage, listOf(if (state) 1 else 0)))
    }

    fun setType(band: Int, type: EQType) {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage("/${fader.classString}/${fader.idString}/eq/$band/type", listOf(type.value)))
    }

    fun setFrequency(band: Int, frequency: Int) {
        if (frequency < 20 || frequency > 20000)
            throw IllegalArgumentException("Frequency must be between 20 and 20000 Hz")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage("/${fader.classString}/${fader.idString}/eq/$band/f", listOf(frequency)))
    }

    fun setGain(band: Int, gain: Float) {
        if (gain < -15 || gain > 15)
            throw IllegalArgumentException("Gain must be between -15 and 15 dB")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        val x32Gain = (gain * 4).toInt() / 4f

        fader.oscController.sendMessage(OSCMessage("/${fader.classString}/${fader.idString}/eq/$band/g", listOf(x32Gain)))
    }

    fun setQ(band: Int, q: Int) {
        /*if (q < 0 || q > 10) TODO: check what the min and max is, and check step size
            throw IllegalArgumentException("Q must be between 0 and 10")
        else */if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage("/${fader.classString}/${fader.idString}/eq/$band/q", listOf(q)))
    }


    companion object {
        enum class EQType(val value: Int) {
            LCut(0),
            LShv(1),
            PEQ(2),
            VEQ(3),
            HShv(4),
            HCut(5)
        }
    }

}