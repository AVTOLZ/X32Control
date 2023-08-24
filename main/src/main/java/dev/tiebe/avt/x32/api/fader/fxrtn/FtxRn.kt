package dev.tiebe.avt.x32.api.fader.fxrtn

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.*

class FtxRn(oscController: OSCController, id: Int): Fader(oscController, id) {
    private val idString = id.toString().padStart(2, '0')
    override val classString: String = "fxrtn"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    init {
        if (id < 1 || id > 8)
            throw IllegalArgumentException("FtxRn number must be between 1 and 8")
    }
}