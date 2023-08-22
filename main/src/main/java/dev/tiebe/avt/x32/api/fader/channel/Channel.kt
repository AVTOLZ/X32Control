package dev.tiebe.avt.x32.api.fader.channel

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Config
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.fader.Mix

class Channel(oscController: OSCController, id: Int): Fader(oscController, id) {
    private val idString = id.toString().padStart(2, '0')
    override val classString: String = "ch"

    init {
        if (id < 1 || id > 32)
            throw IllegalArgumentException("Channel number must be between 1 and 32")
    }
}