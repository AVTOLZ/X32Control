package dev.tiebe.avt.x32.api.fader.matrix

import dev.tiebe.avt.x32.X32OSC
import dev.tiebe.avt.x32.api.fader.*

class Matrix(x32Osc: X32OSC, id: Int): Fader(x32Osc, id) {
    override val classString: String = "mtx"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 6

    init {
        if (id < 1 || id > 6)
            throw IllegalArgumentException("Matrix number must be between 1 and 6")
    }
}