package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.fader.Color
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.commands.FakeLock


class Commands(private val osc: OSCController) {

    fun color(channel: Int, colorString: String) {
        val color = Color.valueOf(colorString.uppercase())
        osc.getChannel(channel).config.setColor(color)
    }
}