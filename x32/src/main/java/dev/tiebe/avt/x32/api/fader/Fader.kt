@file:Suppress("PrivatePropertyName")

package dev.tiebe.avt.x32.api.fader

import dev.tiebe.avt.x32.X32OSC

abstract class Fader(val x32Osc: X32OSC, val id: String) {
    constructor(x32Osc: X32OSC, id: Int): this(x32Osc, id.toString())

    val idString = id.padStart(2, '0')
    abstract val classString: String
    abstract val eqAmount: Int


    abstract val config: Config
    abstract val soundConfig: SoundConfig
    abstract val mix: Mix
    abstract val eq: Eq
}