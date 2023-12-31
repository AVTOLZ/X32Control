package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getStatus

class Lock(private val osc: OSCController): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Command {
        return this
    }

    override fun run() {
        osc.getStatus().setLock(true)
    }
}