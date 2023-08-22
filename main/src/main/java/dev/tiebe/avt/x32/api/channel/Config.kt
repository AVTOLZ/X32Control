package dev.tiebe.avt.x32.api.channel

import com.illposed.osc.OSCMessage

class Config(channel: Channel) {
    private val idString = channel.id.toString().padStart(2, '0')
    private val osc = channel.oscController

    fun setName(name: String) {
        if (name.length > 12)
            throw IllegalArgumentException("Name must be 12 characters or less")

        osc.sendMessage(OSCMessage("/ch/$idString/config/name", listOf(name)))
    }

    fun setIcon(icon: Icon) {
        osc.sendMessage(OSCMessage("/ch/$idString/config/icon", listOf(icon.value)))
    }

    fun setColor(color: Color) {
        osc.sendMessage(OSCMessage("/ch/$idString/config/color", listOf(color.value)))
    }

    fun setSource(source: Int) {
        if (source < 0 || source > 64)
            throw IllegalArgumentException("Source must be between 0 and 64")

        osc.sendMessage(OSCMessage("/ch/$idString/config/src", listOf(source)))
    }

    fun setSolo(state: Boolean) {
        osc.sendMessage(OSCMessage("/-stat/solosw/$idString", listOf(if (state) 1 else 0)))
    }
}