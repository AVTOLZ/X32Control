package dev.tiebe.avt.osc.quickq.api.fader.other

import dev.tiebe.avt.osc.quickq.QuickQOSC
import dev.tiebe.avt.x32.api.fader.*

class Mono(quickQOSC: dev.tiebe.avt.osc.quickq.QuickQOSC): Fader(quickQOSC, "m") {
    override val classString: String = "main"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 6

}