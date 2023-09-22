package dev.tiebe.avt.osc.x32.commands

interface Command {
    var arguments: List<Any>

    fun setArguments(args: List<String>): Command?

    fun run()

}