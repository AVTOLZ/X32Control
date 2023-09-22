package dev.tiebe.avt.osc.x32.api.fader.other

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.fader.*

class LR(x32Osc: X32OSC): Fader(x32Osc, "st") {
    override val classString: String = "main"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 6

}