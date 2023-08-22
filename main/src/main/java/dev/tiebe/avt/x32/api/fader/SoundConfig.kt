package dev.tiebe.avt.x32.api.fader

import com.illposed.osc.OSCMessage

class SoundConfig(private val fader: Fader) {
    private val idString = fader.id.toString().padStart(2, '0')
    private val osc = fader.oscController

    val delay = DelayConfig(fader)


}

class DelayConfig(fader: Fader) : SoundSubConfig(fader) {
    fun setDelay(delay: Float) {
        if (delay < 0.3 || delay > 500.0)
            throw IllegalArgumentException("Delay must be between 0.3 and 500.0")
        else if (delay % 0.1 != 0.0)
            throw IllegalArgumentException("Delay must be a multiple of 0.1")

        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/delay/time", listOf(delay)))
    }

    fun setState(state: Boolean) {
        osc.sendMessage(OSCMessage("/${fader.classString}/$idString/delay/on", listOf(if (state) 1 else 0)))
    }
}


open class SoundSubConfig(protected val fader: Fader) {
    protected val idString = fader.id.toString().padStart(2, '0')
    protected val osc = fader.oscController
}