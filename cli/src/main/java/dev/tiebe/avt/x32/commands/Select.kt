package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getStatus

class Select(val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()

    override fun setArguments(args: List<String>): Command? {
        return if (args.size == 2 && args[1].toIntOrNull() != null) {
            arguments = listOf(args[1].toInt())
            this
        } else {
            null
        }
    }

    override fun run() {
        println("Selecting channel ${arguments[0]}")
        osc.getStatus().setSelection(arguments[0] as Int)
    }
}