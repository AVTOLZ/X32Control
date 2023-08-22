@file:Suppress("PrivatePropertyName")

package dev.tiebe.avt.x32.api.fader

import dev.tiebe.avt.x32.OSCController

abstract class Fader(val oscController: OSCController, val id: Int) {
    private val idString = id.toString().padStart(2, '0')
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

    init {
        if (id < 1 || id > 32)
            throw IllegalArgumentException("Channel number must be between 1 and 32")
    }
}