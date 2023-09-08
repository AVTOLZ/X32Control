package dev.tiebe.avt.x32.api.fader.bus

import dev.tiebe.avt.x32.QuickQOSC
import dev.tiebe.avt.x32.api.fader.*

class Bus(quickQOSC: QuickQOSC, id: Int): Fader(quickQOSC, id) {
    override val classString: String = "bus"

    override val config: Config = Config(this)
    override val soundConfig: SoundConfig = SoundConfig(this)
    override val mix: Mix = Mix(this)
    override val eq: Eq = Eq(this)

    override val eqAmount: Int = 6

    init {
        if (id < 1 || id > 16)
            throw IllegalArgumentException("Bus number must be between 1 and 16")
    }
}