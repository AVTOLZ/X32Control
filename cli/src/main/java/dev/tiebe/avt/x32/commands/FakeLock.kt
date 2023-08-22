package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.channel.Color
import dev.tiebe.avt.x32.api.getChannel
import dev.tiebe.avt.x32.api.getStatus
import dev.tiebe.avt.x32.api.internal.Screen
import kotlinx.coroutines.runBlocking

class FakeLock(private val osc: OSCController): Command {
    companion object {
        @Volatile var animationThreadRunning = false
        @Volatile var speed = 1.0

        private lateinit var animationThread: Thread
    }

    override var arguments: List<Any> = listOf()

    override fun setArguments(args: List<String>): Command {
        return this
    }

    override fun run() {
        animationThread = Thread {
            animationThreadRunning = true
            var index = 0
            val cols = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

            for(i in 1..16) {
                osc.getChannel(i).config.setColor(Color.WHITE)
                if (i != 1)
                    osc.getChannel(i - 1).config.setColor(Color.OFF)
                Thread.sleep((250/speed).toLong())
            }

            osc.getChannel(16).config.setColor(Color.OFF)

            Thread.sleep((2000/speed).toLong())


            while (animationThreadRunning) {
                for (i in 1..16) {
                    osc.getChannel(i).config.setColor(cols[index])
                    Thread.sleep(((1000/64)/speed).toLong())
                }

                if(index < 5) index++ else index = 0

                Thread.sleep(((1000 / 4) /speed).toLong())
            }
        }

        animationThread.start()

        runBlocking {
            osc.getStatus().setLock(true)
            osc.getStatus().setScreen(Screen.HOME)  //TODO: osc.getStatus().getScreen())
        }
    }

}