package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getChannel

class Color(private val osc: OSCController): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Color? {
        try {
            if (args.size != 3) {
                return null
            } else {
                val arg1 = args[1].toIntOrNull()
                val arg2 = dev.tiebe.avt.x32.api.fader.Color.valueOf(args[2].uppercase())

                if (arg1 == null) return null

                arguments = listOf(arg1, arg2)
                return this
            }
        } catch (e: Exception) {
            return null
        }
    }

    override fun run() {
        osc.getChannel(arguments[0] as Int).config.setColor(arguments[1] as dev.tiebe.avt.x32.api.fader.Color)
    }
}