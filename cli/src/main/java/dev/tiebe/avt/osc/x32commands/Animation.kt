package dev.tiebe.avt.osc.x32.commands

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.getChannel
import dev.tiebe.avt.osc.x32.api.getDCA
import dev.tiebe.avt.osc.x32.api.getLR
import kotlin.math.pow
import kotlin.math.sin

class Animation(private val osc: X32OSC): Command {
    override var arguments: List<Any> = listOf()
    private val channels = List(16) { osc.getChannel(it + 1) } + List(8) { osc.getDCA(it + 1) } + osc.getLR()

    override fun setArguments(args: List<String>): Command? {
        if(args.size < 2) {
            return null
        } else {
            when(args[1]) {
                "sine"              -> {}
                "binary", "bin"     -> {}
                "base"              -> { args[2].toIntOrNull() ?: return null; arguments = listOf("animation", "base", args[2].toInt()); return this }

                else        -> print("Unknown command")
            }
        }

        arguments = args
        return this
    }

    override fun run() {
        when(arguments[1]) {
            "sine"          -> sine()
            "binary", "bin" -> binary()
            "base"           -> baseN(arguments[2] as Int)

            else        -> print("Unknown command")
        }
    }

    private fun sine() {
        var t = 0.0
        while (true) {
            t += 0.05
            channels.forEachIndexed { index, channel ->
                channel.mix.setLevel((0.5 * sin(t + 0.3 * index) + 0.5).toFloat())
            }
            Thread.sleep(20)
        }
    }

    private fun binary() {
        var counter = 0
        while (true) {
            channels.reversed().forEachIndexed { index, channel ->
                val binaryIndex = 1 shl index
                val level = if (counter and binaryIndex == binaryIndex) 1.0f else 0.0f
                channel.mix.setLevel(level)
            }
            counter++
            Thread.sleep(200)
        }
    }

    private fun baseN(base: Int) {
        var counter = 0
        while (true) {
            channels.reversed().forEachIndexed { index, channel ->
                val baseDigit = counter / (base.toDouble().pow(index.toDouble())).toInt() % base
                val level = (baseDigit.toDouble() / (base - 1)).toFloat()
                channel.mix.setLevel(level)
            }
            counter++
            Thread.sleep(250)
        }
    }
}