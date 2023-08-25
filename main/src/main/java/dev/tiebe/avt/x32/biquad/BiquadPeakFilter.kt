package dev.tiebe.avt.x32.biquad

import dev.tiebe.avt.x32.utils.Complex
import kotlin.math.*

class BiquadPeakFilter(
    val sampleRate: Double,
    private val frequency: Double,
    private val gain: Double,
    private val q: Double
) {
    // Coefficients for the filter equation
    var a0 = 0.0
    var a1 = 0.0
    var a2 = 0.0
    var b0 = 0.0
    var b1 = 0.0
    var b2 = 0.0

    // State variables for filter equation
    var x1 = 0.0
    var x2 = 0.0
    var y1 = 0.0
    var y2 = 0.0

    init {
        calculateCoefficients()
    }

    private fun calculateCoefficients() {
        val A: Double = 10.0.pow(gain / 40)
        val omega = 2 * Math.PI * frequency / sampleRate
        val alpha = sin(omega) / (2 * q)
        b0 = 1 + alpha * A
        b1 = -2 * cos(omega)
        b2 = 1 - alpha * A
        a0 = 1 + alpha / A
        a1 = -2 * cos(omega)
        a2 = 1 - alpha / A

        // Normalize coefficients so that a0 = 1
        b0 /= a0
        b1 /= a0
        b2 /= a0
        a1 /= a0
        a2 /= a0
        a0 = 1.0
    }

    fun calculateMagnitudeResponse(frequency: Double): Double {
        val w = 2 * Math.PI * frequency / sampleRate

        val numerator = b0 * b0 + b1 * b1 + b2 * b2 + 2 * (b0 * b1 + b1 * b2) * cos(w) + 2 * b0 * b2 * cos(2 * w)
        val denominator = a0 * a0 + a1 * a1 + a2 * a2 + 2 * (a0 * a1 + a1 * a2) * cos(w) + 2 * a0 * a2 * cos(2 * w)
        val magnitude = sqrt(numerator / denominator)

        return 20 * log10(magnitude)
    }
}