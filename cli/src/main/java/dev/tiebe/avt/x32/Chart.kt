package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.fader.Eq
import dev.tiebe.avt.x32.biquad.BiquadPeakFilter
import dev.tiebe.avt.x32.commands.EQFaderSync
import dev.tiebe.avt.x32.utils.Complex
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartFrame
import org.jfree.chart.ChartPanel
import org.jfree.chart.axis.LogAxis
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.text.DecimalFormat
import javax.swing.JFrame
import kotlin.math.PI
import kotlin.math.log10

import kotlin.math.pow

val testBands = listOf(
    EQFaderSync.EQBand(type = Eq.Companion.EQType.LShv, freq = 0.245, gain = 0.9, q = 0.46478873),
    EQFaderSync.EQBand(
        type = Eq.Companion.EQType.PEQ, freq = 0.505, gain = 0.9583333, q = 0.1971831
    ),
    EQFaderSync.EQBand(type = Eq.Companion.EQType.VEQ, freq = 0.71, gain = 0.0, q = 0.0),
    EQFaderSync.EQBand(
        type = Eq.Companion.EQType.HShv, freq = 0.925, gain = 0.85833335, q = 0.46478873
    )
)

fun main() {
    val band = testBands[1]
    val frequency = 20.0 * (10.0).pow(3 * band.freq)
    val gain = -15.0 + band.gain * (15.0 - -15.0)
    val q = 10 * (0.3 / 10.0).pow(band.q)

    println(frequency)
    println(gain)

    val biquad = BiquadPeakFilter(44100.0, frequency, gain, q)

    val frequencies = (0..44100).map { it.toDouble() }

    val series = XYSeries("Frequency Response")
    val sampleSize = 1000

    // Compute the magnitude response for a range of frequencies
    for (i in 0 until sampleSize) {
        series.add(frequencies[i], biquad.calculateMagnitudeResponse(frequencies[i]))
    }

    // Create dataset
    val dataset = XYSeriesCollection()
    dataset.addSeries(series)

    // Create chart
    val chart = ChartFactory.createXYLineChart(
        "Biquad Peak Filter Frequency Response",
        "Frequency (Hz)",
        "Gain (dB)",
        dataset
    )

    // Display the chart
    val frame = ChartFrame("Biquad Peak Filter Frequency Response", chart)
    frame.pack()
    frame.isVisible = true

/*    val response = mutableListOf<Pair<Double, Double>>()
    val nPoints = 1000   // Number of points for the plot
    for (i in 0 until nPoints) {
        val normalizedFrequency = i.toDouble() / (nPoints - 1) // Normalized frequency (0 to 1)
        val w = Math.PI * normalizedFrequency  // Frequency in rad/sample
        val ejw = Complex(Math.cos(w), Math.sin(w))  // e^(jw) in the form of a complex number
        val H = biquad.evaluateAt(ejw)   // H(e^(jw))
        response.add(Pair(normalizedFrequency, H.abs()))  // Save the magnitude of H(e^(jw))
    }

    val minFrequencyLog = log10(20.0)  // base frequency in Hz, in log scale
    val maxFrequencyLog = log10(20000.0)  // max frequency in Hz, in log scale

    val series = XYSeries("Frequency Response")
    response.forEach { (normalizedFrequency, magnitude) ->
        val frequencyLog = minFrequencyLog + normalizedFrequency * (maxFrequencyLog - minFrequencyLog)
        series.add(10.0.pow(frequencyLog), 20.0 * log10(magnitude))
    }

    val dataset = XYSeriesCollection().apply { addSeries(series) }
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
    frame.isVisible = true*/
}