package dev.tiebe.avt.x32.api.channel

import com.illposed.osc.OSCMessage

class SoundConfig(private val channel: Channel) {
    private val idString = channel.id.toString().padStart(2, '0')
    private val osc = channel.oscController

    val delay = DelayConfig(channel)


}

class DelayConfig(channel: Channel) : SoundSubConfig(channel) {
    fun setDelay(delay: Float) {
        if (delay < 0.3 || delay > 500.0)
            throw IllegalArgumentException("Delay must be between 0.3 and 500.0")
        else if (delay % 0.1 != 0.0)
            throw IllegalArgumentException("Delay must be a multiple of 0.1")

        osc.sendMessage(OSCMessage("/ch/$idString/delay/time", listOf(delay)))
    }

    fun setState(state: Boolean) {
        osc.sendMessage(OSCMessage("/ch/$idString/delay/on", listOf(if (state) 1 else 0)))
    }
}


open class SoundSubConfig(protected val channel: Channel) {
    protected val idString = channel.id.toString().padStart(2, '0')
    protected val osc = channel.oscController
}