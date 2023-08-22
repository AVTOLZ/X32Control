package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.fader.aux.AuxIn
import dev.tiebe.avt.x32.api.fader.bus.Bus
import dev.tiebe.avt.x32.api.fader.channel.Channel
import dev.tiebe.avt.x32.api.fader.dca.DCA
import dev.tiebe.avt.x32.api.fader.fxrtn.FtxRn
import dev.tiebe.avt.x32.api.fader.matrix.Matrix
import dev.tiebe.avt.x32.api.fader.other.LR
import dev.tiebe.avt.x32.api.fader.other.Mono

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
            is Channel -> fader.id.padStart(2, '0')
            is AuxIn -> (fader.id + 32).padStart(2, '0')
            is FtxRn -> (fader.id + 40).padStart(2, '0')
            is Bus -> (fader.id + 48).padStart(2, '0')
            is Matrix -> (fader.id + 64).padStart(2, '0')
            is LR -> "71"
            is Mono -> "72"
            is DCA -> (fader.id + 72).padStart(2, '0')
            else -> throw IllegalStateException("Fader must be either a channel or a bus")
        }
        osc.sendMessage(OSCMessage("/-stat/solosw/$offsetId", listOf(if (state) 1 else 0)))
    }
}