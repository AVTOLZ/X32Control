package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.channel.Color
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.api.internal.Screen
import dev.tiebe.avt.x32.commands.FakeLock
import kotlinx.coroutines.runBlocking


class Commands(private val osc: OSCController) {

    fun lock() {
        osc.getStatus().setLock(true)
    }
    fun unlock() {
        FakeLock.animationThreadRunning = false

        osc.getStatus().setLock(false)
    }

    fun mute(channel: Int) {
        osc.getChannel(channel).mix.setMute(true)
    }
    fun unmute(channel: Int) {
        osc.getChannel(channel).mix.setMute(false)
    }
    fun fader(channel: Int, level: Float) {
        osc.getChannel(channel).mix.setLevel(level)
    }
    fun solo(channel: Int, bool: Boolean) {
        osc.getChannel(channel).config.setSolo(bool)
    }
    fun color(channel: Int, colorString: String) {
        val color = Color.valueOf(colorString.uppercase())
        osc.getChannel(channel).config.setColor(color)
    }
}