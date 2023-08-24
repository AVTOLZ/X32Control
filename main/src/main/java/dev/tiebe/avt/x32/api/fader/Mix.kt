package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.fader.channel.Channel
import kotlin.math.floor

class Mix(val fader: Fader) {
    private val idString = fader.id.toString().padStart(2, '0')
    private val osc = fader.oscController // wat is het leuke dat je gevonden hebt?; dat ik je kan forceren om me te followen :); is dat leuk tho?; je betn net een gymleraar haaha; whahaha, voor mij is t leuk

    suspend fun getMute(): Boolean {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/mix/on"))?.arguments?.get(0) == 0
    }

    fun setMute(mute: Boolean) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/on", listOf(if (mute) 0 else 1)))
    }

    fun setLevel(level: Float) {
        if (level < 0 || level > 1)
            throw IllegalArgumentException("Level must be between 0 and 1")

        val x32Value = floor(level * 1023) / 1023
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/mix/fader", listOf(x32Value)))
    }

}