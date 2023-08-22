package dev.tiebe.avt.x32.api.fader.other

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Fader

class LR(oscController: OSCController): Fader(oscController, "st") {
    private val idString = id.toString().padStart(2, '0')
    override val classString: String = "main"
}