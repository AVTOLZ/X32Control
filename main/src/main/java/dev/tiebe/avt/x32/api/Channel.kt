package dev.tiebe.avt.x32.api

import dev.tiebe.avt.x32.OSCController

class Channel(val oscController: OSCController, val id: Int) {
    init {
        if (id < 1 || id > 32)
            throw IllegalArgumentException("Channel id must be between 1 and 32")
    }

    fun mute() {
        println("Muting channel $id")
    }


}