package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage

open class Eq(val fader: Fader) {
    open val mainMessage = "/${fader.classString}/${fader.idString}/eq"
    open val stateOSCMessage = "$mainMessage/on"
    open val bandOSCMessage = "$mainMessage/%d"
    open val typeOSCMessage = "$bandOSCMessage/type"
    open val frequencyOSCMessage = "$bandOSCMessage/f"
    open val gainOSCMessage = "$bandOSCMessage/g"
    open val qOSCMessage = "$bandOSCMessage/q"

    fun setState(state: Boolean) {
        fader.oscController.sendMessage(OSCMessage(stateOSCMessage, listOf(if (state) 1 else 0)))
    }

    fun setType(band: Int, type: EQType) {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage(typeOSCMessage.format(band), listOf(type.value)))
    }

    fun setFrequency(band: Int, frequency: Float) {
        if (frequency < 20 || frequency > 20000)
            throw IllegalArgumentException("Frequency must be between 20 and 20000 Hz")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage(frequencyOSCMessage.format(band), listOf(frequency)))
    }

    fun setGain(band: Int, gain: Float) {
        if (gain < -15 || gain > 15)
            throw IllegalArgumentException("Gain must be between -15 and 15 dB")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        val x32Gain = (gain * 4).toInt() / 4f

        fader.oscController.sendMessage(OSCMessage(gainOSCMessage.format(band), listOf(x32Gain)))
    }

    fun setQ(band: Int, q: Float) {
        if (q < 0 || q > 1)
            throw IllegalArgumentException("Q must be between 0 and 1")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.oscController.sendMessage(OSCMessage(qOSCMessage.format(band), listOf(q)))
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

/*        fun EQType.getBiquadFilter(sampleRate: Double, frequency: Double, gain: Double, q: Double): BiquadFilter = when (this) {
                EQType.LCut -> TODO()
                EQType.LShv -> TODO()
                EQType.PEQ -> BiquadPeakFilter(sampleRate, frequency, gain, q)
                EQType.VEQ -> BiquadPeakFilter(sampleRate, frequency, gain, q/2.3)
                EQType.HShv -> TODO()
                EQType.HCut -> TODO()
            }*/
    }

}