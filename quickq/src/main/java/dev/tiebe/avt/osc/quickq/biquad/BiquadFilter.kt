package dev.tiebe.avt.osc.quickq.biquad

import kotlin.math.*


class BiQuadraticFilter {
    var a0 = 0.0
    var a1 = 0.0
    var a2 = 0.0
    var b0 = 0.0
    var b1 = 0.0
    var b2 = 0.0
    var x1 = 0.0
    var x2 = 0.0
    var y = 0.0
    var y1 = 0.0
    var y2 = 0.0
    var gain_abs = 0.0
    var type = FilterType.LOWPASS
    var center_freq = 0.0
    var sample_rate = 0.0
    var Q = 0.0
    var gainDB = 0.0

    constructor()
    constructor(type: FilterType, center_freq: Double, sample_rate: Double, Q: Double, gainDB: Double) {
        configure(type, center_freq, sample_rate, Q, gainDB)
    }

    // constructor without gain setting
    constructor(type: FilterType, center_freq: Double, sample_rate: Double, Q: Double) {
        configure(type, center_freq, sample_rate, Q, 0.0)
    }

    fun reset() {
        y2 = 0.0
        y1 = y2
        x2 = y1
        x1 = x2
    }

    fun frequency(): Double {
        return center_freq
    }

    @JvmOverloads
    fun configure(type: FilterType, center_freq: Double, sample_rate: Double, Q: Double, gainDB: Double = 0.0) {
        var Q = Q
        reset()
        Q = if (Q == 0.0) 1e-9 else Q
        Q = if (type == FilterType.VEQ) Q/2.3 else Q
        Q = if (type == FilterType.LOWPASS || type == FilterType.HIGHPASS) 0.707 else Q

        this.type = if (type == FilterType.VEQ) FilterType.PEAK else type
        this.sample_rate = sample_rate
        this.Q = Q
        this.gainDB = gainDB

        reconfigure(center_freq)
    }

    // allow parameter change while running
    fun reconfigure(cf: Double) {
        center_freq = cf
        // only used for peaking and shelving filters
        gain_abs = 10.0.pow(gainDB / 40)
        val omega = 2 * Math.PI * cf / sample_rate
        val sn = sin(omega)
        val cs = cos(omega)
        val alpha = sn / (2 * Q)
        val beta = sqrt(gain_abs + gain_abs)
        when (type) {
            FilterType.BANDPASS -> {
                b0 = alpha
                b1 = 0.0
                b2 = -alpha
                a0 = 1 + alpha
                a1 = -2 * cs
                a2 = 1 - alpha
            }

            FilterType.LOWPASS -> {
                b0 = (1 - cs) / 2
                b1 = 1 - cs
                b2 = (1 - cs) / 2
                a0 = 1 + alpha
                a1 = -2 * cs
                a2 = 1 - alpha
            }

            FilterType.HIGHPASS -> {
                b0 = (1 + cs) / 2
                b1 = -(1 + cs)
                b2 = (1 + cs) / 2
                a0 = 1 + alpha
                a1 = -2 * cs
                a2 = 1 - alpha
            }

            FilterType.NOTCH -> {
                b0 = 1.0
                b1 = -2 * cs
                b2 = 1.0
                a0 = 1 + alpha
                a1 = -2 * cs
                a2 = 1 - alpha
            }

            FilterType.PEAK -> {
                b0 = 1 + alpha * gain_abs
                b1 = -2 * cs
                b2 = 1 - alpha * gain_abs
                a0 = 1 + alpha / gain_abs
                a1 = -2 * cs
                a2 = 1 - alpha / gain_abs
            }

            FilterType.LOWSHELF -> {
                b0 = gain_abs * (gain_abs + 1 - (gain_abs - 1) * cs + beta * sn)
                b1 = 2 * gain_abs * (gain_abs - 1 - (gain_abs + 1) * cs)
                b2 = gain_abs * (gain_abs + 1 - (gain_abs - 1) * cs - beta * sn)
                a0 = gain_abs + 1 + (gain_abs - 1) * cs + beta * sn
                a1 = -2 * (gain_abs - 1 + (gain_abs + 1) * cs)
                a2 = gain_abs + 1 + (gain_abs - 1) * cs - beta * sn
            }

            FilterType.HIGHSHELF -> {
                b0 = gain_abs * (gain_abs + 1 + (gain_abs - 1) * cs + beta * sn)
                b1 = -2 * gain_abs * (gain_abs - 1 + (gain_abs + 1) * cs)
                b2 = gain_abs * (gain_abs + 1 + (gain_abs - 1) * cs - beta * sn)
                a0 = gain_abs + 1 - (gain_abs - 1) * cs + beta * sn
                a1 = 2 * (gain_abs - 1 - (gain_abs + 1) * cs)
                a2 = gain_abs + 1 - (gain_abs - 1) * cs - beta * sn
            }

            else -> {}
        }
        // prescale flter constants
        b0 /= a0
        b1 /= a0
        b2 /= a0
        a1 /= a0
        a2 /= a0
    }

    // provide a static amplitude result for testing
    fun result(f: Double): Double {
        val phi = sin(2.0 * Math.PI * f / (2.0 * sample_rate)).pow(2.0)
        var r =
            ((b0 + b1 + b2).pow(2.0) - 4.0 * (b0 * b1 + 4.0 * b0 * b2 + b1 * b2) * phi + 16.0 * b0 * b2 * phi * phi) / ((1.0 + a1 + a2).pow(
                2.0
            ) - 4.0 * (a1 + 4.0 * a2 + a1 * a2) * phi + 16.0 * a2 * phi * phi)
        if (r < 0) {
            r = 0.0
        }
        return sqrt(r)
    }

    // provide a static decibel result for testing
    fun log_result(f: Double): Double {
        var r: Double
        r = try {
            20 * log10(result(f))
        } catch (e: Exception) {
            -100.0
        }
        if (java.lang.Double.isInfinite(r) || java.lang.Double.isNaN(r)) {
            r = -100.0
        }
        return r
    }

    // return the constant set for this filter
    fun constants(): DoubleArray {
        return doubleArrayOf(a1, a2, b0, b1, b2)
    }

    // perform one filtering step
    fun filter(x: Double): Double {
        y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1
        x1 = x
        y2 = y1
        y1 = y
        return y
    }

    companion object {
        enum class FilterType {
            LOWPASS, HIGHPASS, BANDPASS, PEAK, VEQ, NOTCH, LOWSHELF, HIGHSHELF
        }

        const val LOWPASS = 0
        const val HIGHPASS = 1
        const val BANDPASS = 2
        const val PEAK = 3
        const val NOTCH = 4
        const val LOWSHELF = 5
        const val HIGHSHELF = 6
    }
}