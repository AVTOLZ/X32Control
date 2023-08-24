@file:Suppress("PrivatePropertyName")

package dev.tiebe.avt.x32.api.fader

import dev.tiebe.avt.x32.OSCController

abstract class Fader(val oscController: OSCController, val id: String) {
    constructor(oscController: OSCController, id: Int): this(oscController, id.toString())

    val idString = id.padStart(2, '0')
    abstract val classString: String
    abstract val eqAmount: Int


    abstract val config: Config
    abstract val soundConfig: SoundConfig
    abstract val mix: Mix
    abstract val eq: Eq
}