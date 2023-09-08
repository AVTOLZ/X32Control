package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import kotlin.math.log10
import kotlin.math.pow

open class Eq(val fader: Fader) {
    open val mainMessage = "/${fader.classString}/${fader.idString}/eq"
    open val stateOSCMessage = "$mainMessage/on"
    open val bandOSCMessage = "$mainMessage/%d"
    open val typeOSCMessage = "$bandOSCMessage/type"
    open val frequencyOSCMessage = "$bandOSCMessage/f"
    open val gainOSCMessage = "$bandOSCMessage/g"
    open val qOSCMessage = "$bandOSCMessage/q"

    fun setState(state: Boolean) {
        fader.x32Osc.sendMessage(OSCMessage(stateOSCMessage, listOf(if (state) 1 else 0)))
    }

    suspend fun getType(band: Int): BiQuadraticFilter.Companion.FilterType {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        val type = fader.x32Osc.getValue(OSCMessage(typeOSCMessage.format(band)))?.arguments?.get(0) as Int

        return getFilterType(type)
    }

    fun setType(band: Int, type: BiQuadraticFilter.Companion.FilterType) {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.x32Osc.sendMessage(OSCMessage(typeOSCMessage.format(band), listOf(getFilterValue(type))))
    }

    suspend fun getFrequency(band: Int): Float {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        return (fader.x32Osc.getValue(OSCMessage(frequencyOSCMessage.format(band)))?.arguments?.get(0) as Float)
    }

    fun setFrequency(band: Int, frequency: Float) {
        if (frequency < 0 || frequency > 1)
            throw IllegalArgumentException("Frequency must be between 0 and 1")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.x32Osc.sendMessage(OSCMessage(frequencyOSCMessage.format(band), listOf(frequency)))
    }

    suspend fun getGain(band: Int): Float {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        return (fader.x32Osc.getValue(OSCMessage(gainOSCMessage.format(band)))?.arguments?.get(0) as Float)
    }

    fun setGain(band: Int, gain: Float) {
        if (gain < 0 || gain > 1)
            throw IllegalArgumentException("Gain must be between 0 and 1")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.x32Osc.sendMessage(OSCMessage(gainOSCMessage.format(band), listOf(gain)))
    }

    suspend fun getQ(band: Int): Float {
        if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        return (fader.x32Osc.getValue(OSCMessage(qOSCMessage.format(band)))?.arguments?.get(0) as Float)
    }

    fun setQ(band: Int, q: Float) {
        if (q < 0 || q > 1)
            throw IllegalArgumentException("Q must be between 0 and 1")
        else if (band < 1 || band > fader.eqAmount)
            throw IllegalArgumentException("Band must be between 1 and ${fader.eqAmount}")

        fader.x32Osc.sendMessage(OSCMessage(qOSCMessage.format(band), listOf(q)))
    }

    companion object {
        fun getFilterType(index: Int): BiQuadraticFilter.Companion.FilterType {
            return when (index) {
                0 -> BiQuadraticFilter.Companion.FilterType.HIGHPASS
                1 -> BiQuadraticFilter.Companion.FilterType.LOWSHELF
                2 -> BiQuadraticFilter.Companion.FilterType.PEAK
                3 -> BiQuadraticFilter.Companion.FilterType.VEQ
                4 -> BiQuadraticFilter.Companion.FilterType.HIGHSHELF
                5 -> BiQuadraticFilter.Companion.FilterType.LOWPASS

                else -> BiQuadraticFilter.Companion.FilterType.LOWSHELF
            }
        }

        fun getFilterValue(filter: BiQuadraticFilter.Companion.FilterType): Int {
            return when (filter) {
                BiQuadraticFilter.Companion.FilterType.HIGHPASS -> 0
                BiQuadraticFilter.Companion.FilterType.LOWSHELF -> 1
                BiQuadraticFilter.Companion.FilterType.PEAK -> 2
                BiQuadraticFilter.Companion.FilterType.VEQ -> 3
                BiQuadraticFilter.Companion.FilterType.HIGHSHELF -> 4
                BiQuadraticFilter.Companion.FilterType.LOWPASS -> 5
                BiQuadraticFilter.Companion.FilterType.BANDPASS -> TODO()
                BiQuadraticFilter.Companion.FilterType.NOTCH -> TODO()
            }
        }

        fun decibelAddition(db1: Double, db2: Double): Double {
            val factor1 = 10.0.pow(db1 / 10.0)
            val factor2 = 10.0.pow(db2 / 10.0)

            return 10.0 * log10(factor1 + factor2)
        }

    }

}