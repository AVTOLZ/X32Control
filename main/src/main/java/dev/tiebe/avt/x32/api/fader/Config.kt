package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.fader.bus.Bus
import dev.tiebe.avt.x32.api.fader.channel.Channel

class Config(val fader: Fader) {
    private val idString = fader.id.toString().padStart(2, '0')
    private val osc = fader.oscController

    fun setName(name: String) {
        if (name.length > 12)
            throw IllegalArgumentException("Name must be 12 characters or less")

        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/name", listOf(name)))
    }

    fun setIcon(icon: Icon) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/icon", listOf(icon.value)))
    }

    fun setColor(color: Color) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/color", listOf(color.value)))
    }

    fun setSource(source: Int) {
        if (source < 0 || source > 64)
            throw IllegalArgumentException("Source must be between 0 and 64")

        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/src", listOf(source)))
    }

    fun setSolo(state: Boolean) {
        val offsetId = when (fader) {
            is Channel -> fader.id.toString().padStart(2, '0')
            is Bus -> (fader.id + 48).toString().padStart(2, '0')
            else -> throw IllegalStateException("Fader must be either a channel or a bus")
        }
        osc.sendMessage(OSCMessage("/-stat/solosw/$offsetId", listOf(if (state) 1 else 0)))
    }
}