package dev.tiebe.avt.osc.x32.commands

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.getStatus

class Unlock(private val osc: X32OSC): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Command {
        return this
    }

    override fun run() {
        FakeLock.animationThreadRunning = false

        osc.getStatus().setLock(false)
    }
}