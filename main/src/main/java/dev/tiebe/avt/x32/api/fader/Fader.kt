@file:Suppress("PrivatePropertyName")

package dev.tiebe.avt.x32.api.fader

import dev.tiebe.avt.x32.OSCController

abstract class Fader(val oscController: OSCController, val id: String) {
    constructor(oscController: OSCController, id: Int): this(oscController, id.toString())

    private val idString = id.padStart(2, '0')
    abstract val classString: String

    private lateinit var config_: Config

    val config: Config
        get() {
            return if (this::config_.isInitialized)
                config_
            else {
                config_ = Config(this)
                config_
            }
        }

    private lateinit var soundConfig_: SoundConfig

    val soundConfig: SoundConfig
        get() {
            return if (this::soundConfig_.isInitialized)
                soundConfig_
            else {
                soundConfig_ = SoundConfig(this)
                soundConfig_
            }
        }

    private lateinit var mix_: Mix

    val mix: Mix
        get() {
            return if (this::mix_.isInitialized)
                mix_
            else {
                mix_ = Mix(this)
                mix_
            }
        }
}