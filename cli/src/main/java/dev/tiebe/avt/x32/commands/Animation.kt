package dev.tiebe.avt.x32.commands

import com.illposed.osc.OSCBundle
import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getDCA
import dev.tiebe.avt.x32.api.getLR
import kotlin.math.pow
import kotlin.math.sin

class Animation(private val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()
    val channels = List(16) { osc.getChannel(it + 1) } + List(8) { osc.getDCA(it + 1) } + osc.getLR()

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
            //TODO: test if X32 support this
            val packets = mutableListOf<OSCMessage>()
            channels.forEachIndexed { index, channel ->
                packets.add(OSCMessage(channel.mix.levelOSCCommand, listOf((0.5 * sin(t + 0.3 * index) + 0.5).toFloat())))
            }

            osc.sendBundle(OSCBundle(packets.toList()))
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
            Thread.sleep(250)
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