package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.X32OSC
import dev.tiebe.avt.x32.api.getChannel

class Solo(private val osc: X32OSC): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Solo? {
        if(args.size != 3) {
            return null
        } else {
            val arg1 = args[1].toIntOrNull()
            val arg2 = args[2].toBooleanStrictOrNull()

            if (arg1 == null || arg2 == null) return null

            arguments = listOf(arg1, arg2)
            return this
        }
    }

    override fun run() {
        osc.getChannel(arguments[0] as Int).config.setSolo(arguments[1] as Boolean)
    }
}