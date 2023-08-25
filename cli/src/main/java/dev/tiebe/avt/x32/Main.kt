package dev.tiebe.avt.x32

import com.illposed.osc.OSCMessage
import dev.tiebe.avt.x32.api.fader.channel.Channel
import dev.tiebe.avt.x32.commands.*
import kotlinx.coroutines.runBlocking

// User variables
const val IP = "192.168.0.20"

fun main(args: Array<String>) {
    var localPort = 10024
    if (args.isNotEmpty() && args[0].toIntOrNull() != null) {
        println("Using port ${args[0]}")
        localPort = args[0].toInt()
    }

    val osc = OSCController(IP, 10023, localPort)
    osc.connect()

    val channels = mutableListOf<Channel>()
    for(i in 1..32) {
        channels.add(Channel(osc, i))
    }
    // TODO Busses, Matrices, DCAs, masters
    var delim = ","
    while (true) {
        try {
            print("> ")
            var command = readlnOrNull()?.split(delim, ".", ",", " ")
            while (command == null) {
                command = readlnOrNull()?.split(delim, ".", ",", " ")
            }

            when (command[0]) {
                "fakelock" -> FakeLock(osc).setArguments(command).run()
                "lock" -> Lock(osc).setArguments(command).run()
                "unlock" -> Unlock(osc).setArguments(command).run()
                "mute" -> Mute(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg 2: Boolean")
                "speed" -> FakeLock.speed = command[1].toDoubleOrNull() ?: 1.0
                "fader" -> Fader(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg2: Fader level")
                "faderpre" -> FaderPreset(osc).setArguments(command)?.run() ?: println("Arg 1: Preset")
                "solo" -> Solo(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg 2: Boolean")
                "blocklock" -> BlockLock(osc).setArguments(command)?.run() ?: println("Arg 1: Boolean")
                "color" -> Color(osc).setArguments(command)?.run() ?: println("Arg 1: Channel\nArg 2: Color")
                "animation", "ani" -> Animation(osc).setArguments(command)?.run() ?: println("Arg 1: Animation")

                "eqsync" -> EQFaderSync(osc).setArguments(command)?.run() ?: println("Arg 1: Boolean")

                "custom" -> runBlocking { osc.getValue(OSCMessage(command[1])).let { message -> println("${message?.address}, ${message?.arguments}") } }
                "subscribe" -> osc.subscribe(command[1], 20) { println("Address: ${it.message.address}; Arguments: ${it.message.arguments}") }
                "delim" -> delim = command[1]
                else -> println("Command not found")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}