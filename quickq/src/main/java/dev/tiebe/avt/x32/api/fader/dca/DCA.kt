package dev.tiebe.avt.x32.api.fader.dca

import dev.tiebe.avt.x32.QuickQOSC
import dev.tiebe.avt.x32.api.fader.*

class DCA(quickQOSC: QuickQOSC, id: Int): Fader(quickQOSC, id) {
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