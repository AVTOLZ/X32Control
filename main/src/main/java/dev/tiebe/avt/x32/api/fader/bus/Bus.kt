package dev.tiebe.avt.x32.api.fader.bus

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Fader

class Bus(oscController: OSCController, id: Int): Fader(oscController, id) {
    private val idString = id.toString().padStart(2, '0')
    override val classString: String = "bus"

    init {
        if (id < 1 || id > 16)
            throw IllegalArgumentException("Channel number must be between 1 and 32")
    }
}