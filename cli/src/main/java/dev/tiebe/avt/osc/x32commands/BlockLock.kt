package dev.tiebe.avt.osc.x32.commands

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.*
import kotlinx.coroutines.runBlocking
import java.util.UUID

class BlockLock(private val osc: X32OSC): Command {
    companion object {
        var subscription: UUID? = null
    }

    override var arguments: List<Any> = listOf()

    override fun setArguments(args: List<String>): Command? {
        return if (args.size == 1) this
        else if (args.size == 2 && args[1].toBooleanStrictOrNull() != null) {
            arguments = listOf(args[1].toBooleanStrict())
            this
        } else {
            null
        }
    }

    override fun run() {
        if (subscription != null) {
            println("Already blocking locks. Disable with: blockunlock,false")
        }

        if (arguments.isNotEmpty() && !(arguments[0] as Boolean)) {
            if (subscription != null)
                osc.unsubscribe(subscription ?: return)
            else
                println("Not blocking locks")

            return
        }

        runBlocking {
            val currentLock = osc.getStatus().getLock()

            subscription = osc.subscribe("/-stat/lock", 10) {
                if ((it.message.arguments[0] as Int == 1) != currentLock) {
                    println("Lock changed")
                    osc.getStatus().setLock(currentLock)
                }
            }
        }
    }

}