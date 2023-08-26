package dev.tiebe.avt.x32.utils

import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

fun Float.toDb(): Float = if (this >= 0.5) this * 40f - 30f
    else if (this >= 0.25) this * 80f - 50f
    else if (this >= 0.0625) this * 160f - 70f
    else if (this >= 0.0) this * 480f - 90f
    else 0.0f

fun Float.dbToFloat() = if (this < -60) (this + 90f) / 480f
    else if (this < -30) (this + 70f) / 160f
    else if (this < -10) (this + 50f) / 80f
    else if (this <= 10) (this + 30f) / 40f
    else throw IllegalStateException("Cannot convert db value to float, value is too high")


fun List<String>.toCorrectTypes(): List<Any> {
    return this.map {
        when {
            it.toBooleanStrictOrNull() != null -> it.toBooleanStrict()
            it.toFloatOrNull() != null -> it.toFloat()
            it.toIntOrNull() != null -> it.toInt()
            else -> it
        }
    }
}

fun Double.toX32Frequency(): Double = 20.0 * (10.0).pow(3 * this)
fun Double.toX32Q(): Double = 10 * (0.3 / 10.0).pow(this)
fun Double.toX32Gain(): Double = -15.0 + this * (15.0 - -15.0)

fun Double.fromX32Frequency(): Double = (this / 20.0).pow(1.0 / 3.0)
fun Double.fromX32Q(): Double = (this / 10.0).pow(1.0 / 3.0)
fun Double.fromX32Gain(): Double = (this + 15.0) / (15.0 - -15.0)

fun Double.mapToLin(fromRange: IntRange, toRange: ClosedFloatingPointRange<Double>): Double {
    val logMin = log10(toRange.start)
    val logMax = log10(toRange.endInclusive)
    val scale = (logMax - logMin) / (fromRange.last - fromRange.first)
    return 10.0.pow(logMin + scale * (this - fromRange.first))
}
