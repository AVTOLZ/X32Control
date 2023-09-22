package dev.tiebe.avt.osc.x32.commands

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.getChannel
import kotlinx.coroutines.runBlocking

class Mute(private val osc: X32OSC): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Mute? {
        when (args.size) {
            2 -> {
                val arg1 = args[1].toIntOrNull()

                if (arg1 == null) return null

                runBlocking {
                    arguments = listOf(arg1, !osc.getChannel(arg1).mix.getMute())
                }
                return this
            }
            3 -> {
                val arg1 = args[1].toIntOrNull()
                val arg2 = args[2].toBooleanStrictOrNull()

                if (arg1 == null || arg2 == null) return null

                arguments = listOf(arg1, arg2)
                return this
            }
            else -> {
                return null
            }
        }
    }

    override fun run() {
        osc.getChannel(arguments[0] as Int).mix.setMute(arguments[1] as Boolean)
    }
}