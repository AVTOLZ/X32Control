package dev.tiebe.avt.x32.api

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.Fader
import dev.tiebe.avt.x32.api.fader.auxin.AuxIn
import dev.tiebe.avt.x32.api.fader.bus.Bus
import dev.tiebe.avt.x32.api.fader.channel.Channel
import dev.tiebe.avt.x32.api.fader.dca.DCA
import dev.tiebe.avt.x32.api.fader.fxrtn.FtxRn
import dev.tiebe.avt.x32.api.fader.matrix.Matrix
import dev.tiebe.avt.x32.api.fader.other.LR
import dev.tiebe.avt.x32.api.fader.other.Mono
import dev.tiebe.avt.x32.api.internal.Status

fun OSCController.getChannel(id: Int) = Channel(this, id)
fun OSCController.getStatus() = Status.getInstance(this)
fun OSCController.getBus(id: Int) = Bus(this, id)
fun OSCController.getLR() = LR(this)
fun OSCController.getMono() = Mono(this)
fun OSCController.getMatrix(id: Int) = Matrix(this, id)
fun OSCController.getFtxRn(id: Int) = FtxRn(this, id)
fun OSCController.getDCA(id: Int) = DCA(this, id)
fun OSCController.getAuxIn(id: Int) = AuxIn(this, id)


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

fun OSCController.getFaderFromIndex(index: String) = getFaderFromIndex(index.toInt())

fun OSCController.getFaderFromIndex(index: Int) = when (index) {
    in 1..32 -> getChannel(index)
    in 33..40 -> getAuxIn(index - 32)
    in 41..48 -> getFtxRn(index - 40)
    in 49..56 -> getBus(index - 48)
    in 57..63 -> getMatrix(index - 56)
    71 -> getLR()
    72 -> getMono()
    in 73..80 -> getDCA(index - 72)
    else -> throw IllegalArgumentException("Index must be between 1 and 80")
}
