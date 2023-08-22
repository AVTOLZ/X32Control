package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.Commands
import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getStatus

class Unlock(private val osc: OSCController): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Command {
        return this
    }

    override fun run() {
        FakeLock.animationThreadRunning = false

        osc.getStatus().setLock(false)
    }
}