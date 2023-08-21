package dev.tiebe.avt.x32.api.channel

import com.illposed.osc.OSCMessage
import kotlin.math.floor

class Mix(channel: Channel) {
    private val idString = channel.id.toString().padStart(2, '0')
    private val osc = channel.oscController // wat is het leuke dat je gevonden hebt?; dat ik je kan forceren om me te followen :); is dat leuk tho?; je betn net een gymleraar haaha; whahaha, voor mij is t leuk

    fun setMute(mute: Boolean) {
        osc.sendMessage(OSCMessage("/ch/$idString/mix/on", listOf(if (mute) 0 else 1)))
    }

    fun setLevel(level: Float) {
        if (level < 0 || level > 1)
            throw IllegalArgumentException("Level must be between 0 and 1")

        val x32Value = floor(level * 1023) / 1023
        osc.sendMessage(OSCMessage("/ch/$idString/mix/fader", listOf(x32Value)))
    }

}