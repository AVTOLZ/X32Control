package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.fader.channel.Channel
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.commands.*
import kotlinx.coroutines.runBlocking

// User variables
val IP = "192.168.0.20"

fun main() {
    val osc = OSCController(IP, 10023, 10024)
    osc.connect()

    val channels = mutableListOf<Channel>()
    for(i in 1..32) {
        channels.add(Channel(osc, i))
    }
    // TODO Busses, Matrices, DCAs, masters
    while (true) {
        try {
            print(" > ")
            var command = readlnOrNull()?.split(",")
            while (command == null) {
                command = readlnOrNull()?.split(",")
            }

            val commands = Commands(osc)

            when (command[0]) {
                "fakelock" -> FakeLock(osc).setArguments(command).run()
                "lock" -> Lock(osc).setArguments(command).run()
                "unlock" -> Unlock(osc).setArguments(command).run()
                "mute" -> Mute(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg 2: Boolean")
                "speed" -> FakeLock.speed = command[1].toDoubleOrNull() ?: 1.0
                "fader" -> Fader(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg2: Fader level")
                "faderpre" -> FaderPreset(osc).setArguments(command)?.run() ?: println("Arg 1: Preset")
                "solo" -> Solo(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg 2: Boolean")

                "color" -> try {
                    commands.color(command[1].toInt(), command[2])
                } catch (exception: IndexOutOfBoundsException) {
                    println("This command requires two parameters: the channel and the color. Example: color,1,red")
                }

                else -> println("Command not found")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}