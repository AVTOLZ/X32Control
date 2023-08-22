package dev.tiebe.avt.x32.commands

interface Command {
    var arguments: List<String>

    fun setArguments(args: List<String>): Command?

    fun run()

}