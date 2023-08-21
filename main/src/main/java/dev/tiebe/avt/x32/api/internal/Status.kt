package dev.tiebe.avt.x32.api.internal

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.OSCController

class Status private constructor(private val osc: OSCController) {

    fun setLock(state: Boolean) {
        osc.sendMessage(OSCMessage("/-stat/lock", listOf(if (state) 1 else 0)))
    }

    suspend fun getLock(): Boolean {
        val value = osc.getValue(OSCMessage("/-stat/lock")) ?: throw IllegalStateException("Could not get lock")

        return value.arguments[0] as Int == 1
    }

    fun setScreen(screen: Screen) {
        if (screen == Screen.LOCK)
            throw IllegalArgumentException("Screen cannot be set to LOCK")

        osc.sendMessage(OSCMessage("/-stat/screen", listOf(screen.id)))
    }

    suspend fun getScreen(): Screen {
        val value = osc.getValue(OSCMessage("/-stat/screen")) ?: throw IllegalStateException("Could not get screen")

        return Screen.entries[value.arguments[0] as Int]
    }

    companion object {
        @Volatile private var INSTANCES: MutableMap<OSCController, Status> = mutableMapOf()

        fun getInstance(osc: OSCController): Status {
            if (!INSTANCES.containsKey(osc)) {
                INSTANCES[osc] = Status(osc)
            }

            return INSTANCES[osc]!!
        }
    }

}