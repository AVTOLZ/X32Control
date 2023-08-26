package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.fader.channel.Channel
import kotlin.math.floor

class Mix(val fader: Fader) {
    val idString = fader.id.padStart(2, '0')
    private val osc = fader.oscController

    var levelOSCCommand = "/${fader.classString}/$idString/mix/fader"
    var muteOSCCommand = "/${fader.classString}/$idString/mix/on"

    suspend fun getMute(): Boolean {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/mix/on"))?.arguments?.get(0) == 0
    }

    fun setMute(mute: Boolean) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/on", listOf(if (mute) 0 else 1)))
    }

    suspend fun getLevel(): Float {
        return (osc.getValue(OSCMessage(levelOSCCommand))?.arguments?.get(0) as Float)
    }

    fun setLevel(level: Float) {
        if (level < 0 || level > 1)
            throw IllegalArgumentException("Level must be between 0 and 1")

        val x32Value = floor(level * 1023) / 1023
        osc.sendMessage(OSCMessage(levelOSCCommand, listOf(x32Value)))
    }

    suspend fun getStereo(): Boolean {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/mix/st"))?.arguments?.get(0) == 1
    }

    fun setStereo(state: Boolean) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/st", listOf(if (state) 1 else 0)))
    }

    suspend fun getMono(): Boolean {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/mix/mono"))?.arguments?.get(0) == 1
    }

    fun setMono(state: Boolean) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/mono", listOf(if (state) 1 else 0)))
    }

    fun setMonoLevel(level: Float) {
        if (level < 0 || level > 1)
            throw IllegalArgumentException("Level must be between 0 and 1")

        val x32Value = floor(level * 1023) / 1023
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/mlevel", listOf(x32Value)))
    }

    fun setPan(pan: Int) {
        if (pan < -100 || pan > 100)
            throw IllegalArgumentException("Pan must be between -100 and 100")

        val x32Pan = (pan * 2) / 2f
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/pan", listOf(x32Pan)))
    }

    //TODO: /grp/dca and /grp/mute
}