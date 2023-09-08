@file:Suppress("PrivatePropertyName")

package dev.tiebe.avt.x32.api.fader

import dev.tiebe.avt.x32.QuickQOSC

abstract class Fader(val quickQOSC: QuickQOSC, val id: String) {
    constructor(quickQOSC: QuickQOSC, id: Int): this(quickQOSC, id.toString())

    val idString = id.padStart(2, '0')
    abstract val classString: String
    abstract val eqAmount: Int


    abstract val config: Config
    abstract val soundConfig: SoundConfig
    abstract val mix: Mix
    abstract val eq: Eq
}