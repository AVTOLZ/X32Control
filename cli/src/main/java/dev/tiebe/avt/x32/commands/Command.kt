package dev.tiebe.avt.x32.commands

interface Command {
    var arguments: List<Any>

    fun setArguments(args: List<String>): Command?

    fun run()

}