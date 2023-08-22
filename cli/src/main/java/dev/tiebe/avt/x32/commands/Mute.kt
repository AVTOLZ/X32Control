package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.Commands
import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getChannel

class Mute(private val osc: OSCController): Command {

    override var arguments: List<Any> = listOf()
    override fun setArguments(args: List<String>): Mute? {
        try {
            args[0].toInt()
            args[1].toBooleanStrict()
        } catch (exception: NumberFormatException) {
            println("Channel number not valid.")
            return null
        }  catch (exception: IllegalArgumentException) {
            println("Boolean argument not valid")
        }
        arguments = args
        return this
    }

    override fun run() {
        osc.getChannel(arguments[0] as Int).mix.setMute(arguments[1] as Boolean)
    }
}