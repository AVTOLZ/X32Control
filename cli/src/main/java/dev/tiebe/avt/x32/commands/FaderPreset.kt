package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.getChannel

class FaderPreset(private val osc: OSCController): Command {
    override var arguments: List<Any> = listOf()
    private val off = List(16) { 0.0 }
    override fun setArguments(args: List<String>): Command? {
        if(args.size != 2) {
            return null
        } else {
            when(args[1]) {
                "off"   -> arguments = off
            }
        }
        return this
    }

    override fun run() {
        arguments.forEachIndexed { channel, level ->
            osc.getChannel(channel + 1).mix.setLevel(level as Float)
        }
    }
}