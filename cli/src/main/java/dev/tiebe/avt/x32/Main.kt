package dev.tiebe.avt.x32

import dev.tiebe.avt.x32.api.channel.Channel

//gewoon hier neergooien, of wil je liever in apart bestand?; trouwens nvm, is prima zo
val IP = "192.168.1.94"

fun main() {
    val osc = OSCController(IP, 10023, 10024)
    val channels = mutableListOf<Channel>()
    for(i in 1..32) {
        channels.add(Channel(osc, i))
    }
    // TODO Busses, Matrices, DCAs, masters
    while(true) {
        var command = readlnOrNull()?.split(",")
        while(command == null) {
            command = readlnOrNull()?.split(",")
        }

        val commands = Commands(osc)

        when(command[0]) {
            // No parameters
            "lock"          -> commands.lock()
            "unlock"        -> commands.unlock()
            "fakelock"      -> commands.fakelock()

            // One parameter
            "mute"          -> try { commands.mute(command[1].toInt()) } catch(exception: ArrayIndexOutOfBoundsException) {println("This command requires a parameter: the channel. Example: mute,1")}
            "unmute"        -> try { commands.unmute(command[1].toInt()) } catch(exception: ArrayIndexOutOfBoundsException) {println("This command requires a parameter: the channel. Example: unmute,1")}
            "solo"          -> try { commands.solo(command[1].toInt(), command[2].toBooleanStrict()) } catch(exception: ArrayIndexOutOfBoundsException) {println("This command requires two parameters: the channel and the new state. Example: solo,1,true")}

            // Two parameters
            "fader"         -> try { commands.fader(command[1].toInt(), command[2].toFloat()) } catch(exception: ArrayIndexOutOfBoundsException) {println("This command requires two parameters: the channel and the level of the fader. Example: fader,1,0.5")}
            "color"         -> try { commands.color(command[1].toInt(), command[2]) } catch(exception: ArrayIndexOutOfBoundsException) {println("This command requires two parameters: the channel and the color. Example: color,1,red")}

        }

    }
}