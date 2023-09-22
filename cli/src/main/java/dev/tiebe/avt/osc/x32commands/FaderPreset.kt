package dev.tiebe.avt.osc.x32.commands

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.getFaderFromIndex
import kotlin.math.sin

class FaderPreset(private val osc: X32OSC): Command {
    override var arguments: List<Any> = listOf()
    private val off = List(80) { 0.0f }
    private val line = List(80) { it / 80f }
    private val zigzag = List(80) { it % 2f }
    private val line8 = List(80) { (it % 8f) / 7}
    private val sine = List(80) { (0.5 * sin((0.4 * it)) + 0.5).toFloat() }

    //private val
    override fun setArguments(args: List<String>): Command? {
        if(args.size != 2) {
            return null
        } else {
            when(args[1]) {
                "off"       -> arguments = off
                "line"      -> arguments = line
                "zigzag"    -> arguments = zigzag
                "line8"     -> arguments = line8
                "sine"      -> arguments = sine

                else        -> print("Unknown command")
            }
        }
        return this
    }

    override fun run() {
        arguments.forEachIndexed { channel, level ->
            osc.getFaderFromIndex(channel + 1)?.mix?.setLevel(level as Float)
            Thread.sleep(50)
        }
    }
}