package dev.tiebe.avt.osc.x32.api.fader.dca

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.fader.*

class DCA(x32Osc: X32OSC, id: Int): Fader(x32Osc, id) {
    override val classString: String = "dca"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)

    override val mix: Mix = Mix(this).apply {
        levelOSCCommand = "/${classString}/$id/fader"
        muteOSCCommand = "/${classString}/$id/on"
    }

    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 0

    init {
        if (id < 1 || id > 8)
            throw IllegalArgumentException("DCA number must be between 1 and 8")
    }
}