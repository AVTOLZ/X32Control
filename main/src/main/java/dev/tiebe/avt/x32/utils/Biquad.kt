package dev.tiebe.avt.x32.utils

import kotlin.math.*

data class Complex(val real: Double = 0.0, val imag: Double = 0.0) {

    operator fun plus(c: Complex) = Complex(real + c.real, imag + c.imag)

    operator fun minus(c: Complex) = Complex(real - c.real, imag - c.imag)

    operator fun times(c: Complex) =
        Complex(real * c.real - imag * c.imag, real * c.imag + imag * c.real)

    operator fun times(scalar: Double) = Complex(real * scalar, imag * scalar)

    operator fun div(c: Complex): Complex {
        val denom = c.real * c.real + c.imag * c.imag
        return Complex((real * c.real + imag * c.imag) / denom, (imag * c.real - real * c.imag) / denom)
    }

    fun abs(): Double = sqrt(real * real + imag * imag)

    fun inv(): Complex {
        val denom = real * real + imag * imag
        return Complex(real / denom, -imag / denom)
    }

    fun pow(n: Double): Complex {
        val r = abs()
        val theta = atan2(imag, real)
        val rn = r.pow(n)
        val arg = n * theta
        return Complex(rn * cos(arg), rn * sin(arg))
    }
}