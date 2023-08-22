package dev.tiebe.avt.x32.api

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.fader.aux.AuxIn
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