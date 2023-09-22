package dev.tiebe.avt.osc.x32.api

import dev.tiebe.avt.osc.x32.X32OSC
import dev.tiebe.avt.osc.x32.api.fader.Fader
import dev.tiebe.avt.osc.x32.api.fader.auxin.AuxIn
import dev.tiebe.avt.osc.x32.api.fader.bus.Bus
import dev.tiebe.avt.osc.x32.api.fader.channel.Channel
import dev.tiebe.avt.osc.x32.api.fader.dca.DCA
import dev.tiebe.avt.osc.x32.api.fader.fxrtn.FtxRn
import dev.tiebe.avt.osc.x32.api.fader.matrix.Matrix
import dev.tiebe.avt.osc.x32.api.fader.other.LR
import dev.tiebe.avt.osc.x32.api.fader.other.Mono
import dev.tiebe.avt.osc.x32.api.internal.Status

fun X32OSC.getChannel(id: Int) = Channel(this, id)
fun X32OSC.getStatus() = Status.getInstance(this)
fun X32OSC.getBus(id: Int) = Bus(this, id)
fun X32OSC.getLR() = LR(this)
fun X32OSC.getMono() = Mono(this)
fun X32OSC.getMatrix(id: Int) = Matrix(this, id)
fun X32OSC.getFtxRn(id: Int) = FtxRn(this, id)
fun X32OSC.getDCA(id: Int) = DCA(this, id)
fun X32OSC.getAuxIn(id: Int) = AuxIn(this, id)


val Fader.channelIndex get() = when (this) {
        is Channel -> this.id.padStart(2, '0')
        is AuxIn -> (this.id + 32)
        is FtxRn -> (this.id + 40)
        is Bus -> (this.id + 48)
        is Matrix -> (this.id + 64)
        is LR -> "71"
        is Mono -> "72"
        is DCA -> this.id + 72
        else -> throw IllegalStateException("Fader is not a valid type")
    }

fun X32OSC.getFaderFromIndex(index: String) = getFaderFromIndex(index.toInt())

fun X32OSC.getFaderFromIndex(index: Int) = when (index) {
    in 1..32 -> getChannel(index)
    in 33..40 -> getAuxIn(index - 32)
    in 41..48 -> getFtxRn(index - 40)
    in 49..56 -> getBus(index - 48)
    in 57..62 -> getMatrix(index - 56)
    71 -> getLR()
    72 -> getMono()
    in 73..80 -> getDCA(index - 72)
    else -> null
}
