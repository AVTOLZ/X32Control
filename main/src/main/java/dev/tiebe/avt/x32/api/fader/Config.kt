package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.channelIndex
import dev.tiebe.avt.x32.api.fader.auxin.AuxIn
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

    suspend fun getName(): String {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/config/name"))?.arguments?.get(0) as String
    }

    fun setName(name: String) {
        if (name.length > 12)
            throw IllegalArgumentException("Name must be 12 characters or less")

        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/name", listOf(name)))
    }

    suspend fun getIcon(): Icon {
        return Icon.entries[osc.getValue(OSCMessage("/${fader.classString}/$idString/config/icon"))?.arguments?.get(0) as Int]
    }

    fun setIcon(icon: Icon) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/icon", listOf(icon.value)))
    }

    suspend fun getColor(): Pair<Color, Boolean> {
        val value = osc.getValue(OSCMessage("/${fader.classString}/$idString/config/color"))

        val color = Color.entries[value?.arguments?.get(0) as Int % Color.entries.size]
        val inverted = value.arguments[0] as Int >= Color.entries.size

        return Pair(color, inverted)
    }

    fun setColor(color: Color, inverted: Boolean = false) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/color", listOf(color.value + if (inverted) Color.entries.size else 0)))
    }

    suspend fun getSource(): Int {
        return osc.getValue(OSCMessage("/${fader.classString}/$idString/config/source"))?.arguments?.get(0) as Int
    }

    fun setSource(source: Int) {
        if (source < 0 || source > 64)
            throw IllegalArgumentException("Source must be between 0 and 64")

        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/config/source", listOf(source)))
    }

    suspend fun getSolo(): Boolean {
        return osc.getValue(OSCMessage("/-stat/solosw/${fader.channelIndex}"))?.arguments?.get(0) == 1
    }

    fun setSolo(state: Boolean) {
        val offsetId = fader.channelIndex
        osc.sendMessage(OSCMessage("/-stat/solosw/$offsetId", listOf(if (state) 1 else 0)))
    }
}