package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.fader.Eq
import dev.tiebe.avt.x32.biquad.BiQuadraticFilter
import dev.tiebe.avt.x32.commands.EQFaderSync
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.axis.LogAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

val testBands = listOf(
    EQFaderSync.EQBand(type = BiQuadraticFilter.Companion.FilterType.LOWSHELF, freq = 0.245, gain = 0.9, q = 0.46478873),
    EQFaderSync.EQBand(
        type = BiQuadraticFilter.Companion.FilterType.PEAK, freq = 0.505, gain = 0.9583333, q = 0.1971831
    ),
    EQFaderSync.EQBand(type = BiQuadraticFilter.Companion.FilterType.PEAK, freq = 0.71, gain = 0.0, q = 0.0, isVeq = true),
    EQFaderSync.EQBand(
        type = BiQuadraticFilter.Companion.FilterType.HIGHSHELF, freq = 0.925, gain = 0.85833335, q = 0.46478873
    )
)

fun main() {
    val frequencies = (20..20000).map { it.toDouble() }

    val series = XYSeries("Frequency Response")

    frequencies.forEach {
        var total = 0.0

        for (band in testBands) {
            val biquad = band.getBiquad()

            total += biquad.log_result(it)
        }

/*        val minValueLog = log10(20.0)
        val maxValueLog = log10(20000.0)

        val valueLog = log10(it)

        val value = ((valueLog - minValueLog) / (maxValueLog - minValueLog)) * 16

        series.add(value, total)*/
        series.add(log10(it / 20.0) / 3.0, total)
    }

    // Create dataset
    val dataset = XYSeriesCollection()
    dataset.addSeries(series)

    // Create chart
    val chart = ChartFactory.createXYLineChart("Biquad Filter", "Frequency", "Magnitude (dB)",
        dataset, PlotOrientation.VERTICAL, true, true, false)

/*    val logAxis = LogAxis("Frequency")
    logAxis.setBase(10.0)
    logAxis.setRange(20.0, 20000.0)  // Range of frequencies from 20 Hz to 20 kHz

    val numberFormat = DecimalFormat("0.#")  // or whichever format suits your needs, e.g. "0.#"
    numberFormat.maximumIntegerDigits = 5
    logAxis.setNumberFormatOverride(numberFormat)

    chart.getXYPlot().setDomainAxis(logAxis)  // Apply log axis to the chart*/

    val frame = ChartFrame("Biquad Filter", chart)
    frame.pack()
    frame.isVisible = true
}