package dev.tiebe.avt.x32.utils

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