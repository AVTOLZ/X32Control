package dev.tiebe.avt.osc.x32.api.fader.auxin

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.fader.*

class AuxIn(x32Osc: X32OSC, id: Int): Fader(x32Osc, id) {
    override val classString: String = "auxin"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 4

    init {
        if (id < 1 || id > 8)
            throw IllegalArgumentException("Aux number must be between 1 and 8")
    }
}
