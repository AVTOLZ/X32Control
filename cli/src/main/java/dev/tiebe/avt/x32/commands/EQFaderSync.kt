package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController

class EQFaderSync(val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()

    companion object {
        @Volatile var synced = false
        @Volatile lateinit var syncThread: Thread
    }
    override fun setArguments(args: List<String>): Command? {
        if (args.size == 1) {
            arguments = listOf(!synced)
            return this
        } else {
            return null
        }
    }

    override fun run() {
        if (arguments[0] as Boolean) {
            syncThread = Thread {
                synced = true



                while (synced) {
                    Thread.sleep(100)
                }
            }
            syncThread.start()
        } else {
            synced = false
        }
    }
}