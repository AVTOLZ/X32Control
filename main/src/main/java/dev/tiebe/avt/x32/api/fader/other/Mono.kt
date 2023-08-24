package dev.tiebe.avt.x32.api.fader.other

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.*

class Mono(oscController: OSCController): Fader(oscController, "m") {
    override val classString: String = "main"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 6

}