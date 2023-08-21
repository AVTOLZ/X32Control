package dev.tiebe.avt.x32.api.channel

import dev.tiebe.avt.x32.OSCController

class Channel(val oscController: OSCController, val id: Int) {
    private val idString = id.toString().padStart(2, '0')

    val config = Config(this)
    val soundConfig = SoundConfig(this)
    val mix = Mix(this)


    init {
        if (id < 1 || id > 32)
            throw IllegalArgumentException("Channel number must be between 1 and 32")
    }
}