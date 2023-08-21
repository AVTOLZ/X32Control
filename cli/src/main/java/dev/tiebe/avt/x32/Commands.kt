package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.channel.Color
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import kotlinx.coroutines.runBlocking


class Commands(private val osc: OSCController) {
    private val animationThread = Thread {
        var ch = 1
        var index = 0
        val cols = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

        for(i in 1..16) {
            osc.getChannel(i).config.setColor(Color.WHITE)
            if (i != 1)
                osc.getChannel(i - 1).config.setColor(Color.OFF)
        }

        while (true) {
            if (Thread.currentThread().isInterrupted) {
                break
            }

            osc.getChannel(ch).config.setColor(cols[index])

            if (ch == 16) {
                ch = 1
                index++
            } else ch++

            if(index != 5) index++ else index = 0

            Thread.sleep(1000 / 16)
        }
    }

    fun lock() {
        osc.getStatus().setLock(true)
    }
    fun unlock() {
        osc.getStatus().setLock(false)
        if(! animationThread.isInterrupted) {
            animationThread.interrupt()
        }
    }
    fun fakelock(animation: Boolean = true) {
        if(animation) {
            animationThread.start()
        }

        runBlocking {
            osc.getStatus().setLock(true)
            osc.getStatus().setScreen(osc.getStatus().getScreen())
        }
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