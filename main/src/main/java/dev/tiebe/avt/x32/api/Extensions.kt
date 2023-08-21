package dev.tiebe.avt.x32.api

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.channel.Channel
import dev.tiebe.avt.x32.api.internal.Status

fun OSCController.getChannel(id: Int) = Channel(this, id)
fun OSCController.getStatus() = Status.getInstance(this)