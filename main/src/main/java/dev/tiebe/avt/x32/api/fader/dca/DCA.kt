package dev.tiebe.avt.x32.api.fader.dca

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Fader

class DCA(oscController: OSCController, id: Int): Fader(oscController, id) {
    private val idString = id.toString().padStart(2, '0')
    override val classString: String = "dca"

    init {
        if (id < 1 || id > 8)
            throw IllegalArgumentException("DCA number must be between 1 and 8")
    }
}