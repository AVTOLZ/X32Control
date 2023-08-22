package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.Commands
import dev.tiebe.avt.x32.OSCController

class Unlock(private val osc: OSCController): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Command {
        return this
    }

    override fun run() {
        Commands(osc).unlock()
    }
}