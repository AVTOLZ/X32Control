package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.fader.Eq
import dev.tiebe.avt.x32.api.fader.Eq.Companion.decibelAddition
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter.Companion.FilterType.*
import dev.tiebe.avt.x32.commands.EQFaderSync
import dev.tiebe.avt.x32.commands.EQFaderSync.*
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.axis.LogAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

val advancedTestBands = listOf(
    EQBand(type = PEAK, freq = 0.25999999046325684, gain = 0.8999999761581421, q = 0.0),
    EQBand(type = PEAK, freq = 0.25999999046325684, gain = 0.06666667014360428, q = 0.0),
    EQBand(type = VEQ, freq = 0.44999998807907104, gain = 0.9666666388511658, q = 0.0),
    EQBand(type = LOWPASS, freq = 0.9399999976158142, gain = 0.0, q = 0.14084507524967194)
)

val semiAdvancedTestBands = listOf(
    EQBand(type = PEAK, freq = 0.47999998927116394, gain = 0.949999988079071, q = 0.0),
    EQBand(type = PEAK, freq = 0.2849999964237213, gain = 0.8833333253860474, q = 0.6619718074798584),
    EQBand(type = BiQuadraticFilter.Companion.FilterType.PEAK, freq = 0.625, gain = 0.28333333134651184, q = 0.26760563254356384),
    EQBand(type = BiQuadraticFilter.Companion.FilterType.LOWPASS, freq = 0.8199999928474426, gain = 0.6666666865348816, q = 0.14084507524967194)
)

val testBands = listOf(
    EQBand(type = BiQuadraticFilter.Companion.FilterType.LOWSHELF, freq = 0.245, gain = 0.9, q = 0.46478873),
    EQBand(
        type = PEAK, freq = 0.505, gain = 0.9583333, q = 0.1971831
    ),
    EQBand(type = BiQuadraticFilter.Companion.FilterType.VEQ, freq = 0.71, gain = 0.0, q = 0.0),
    EQBand(
        type = BiQuadraticFilter.Companion.FilterType.HIGHSHELF, freq = 0.925, gain = 0.85833335, q = 0.46478873
    )
)

fun main() {
    val frequencies = (20..20000).map { it.toDouble() }

    val series = XYSeries("Frequency Response")

    frequencies.forEach {
        var total: Double = 0.0 //? = null

        for (band in advancedTestBands) {
            val biquad = band.getBiquad()

            if (it == 20.0) {
                println("Band: $band")
                println("a0: ${biquad.a0}, a1: ${biquad.a1}, a2: ${biquad.a2}, b0: ${biquad.b0}, b1: ${biquad.b1}, b2: ${biquad.b2}")
                println("Gain: ${biquad.log_result(it)}")
            }

            total += biquad.result(it) - 1

        }

        total += 1

        series.add(it, 20 * log10(total))
    }

    // Create dataset
    val dataset = XYSeriesCollection()
    dataset.addSeries(series)

    // Create chart
    val chart = ChartFactory.createXYLineChart("Biquad Filter", "Frequency", "Magnitude (dB)",
        dataset, PlotOrientation.VERTICAL, true, true, false)

    val logAxis = LogAxis("Frequency")
    logAxis.setBase(10.0)
    logAxis.setRange(20.0, 20000.0)  // Range of frequencies from 20 Hz to 20 kHz

    val numberFormat = DecimalFormat("0.#")  // or whichever format suits your needs, e.g. "0.#"
    numberFormat.maximumIntegerDigits = 5
    logAxis.setNumberFormatOverride(numberFormat)

    chart.getXYPlot().setDomainAxis(logAxis)  // Apply log axis to the chart

    val frame = ChartFrame("Biquad Filter", chart)
    frame.pack()
    frame.isVisible = true
}