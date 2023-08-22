package dev.tiebe.avt.x32.commands

import dev.tiebe.avt.x32.OSCController
import dev.tiebe.avt.x32.api.*
import dev.tiebe.avt.x32.api.fader.Color
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
            val channels = List(16) { osc.getChannel(it + 1) }
            val dcas = List(8) { osc.getDCA(it + 1) }
            val lr = osc.getLR()

            val faders = channels + dcas + lr

            animationThreadRunning = true
            var index = 0
            val cols = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA)

            for (fader in faders.indices) {
                faders[fader].config.setColor(Color.WHITE)
                faders[fader].config.setSolo(true)
                faders[fader].mix.setMute(true)
                if (fader != 0) {
                    faders[fader-1].config.setColor(Color.OFF)
                    faders[fader-1].config.setSolo(false)
                    faders[fader-1].mix.setMute(false)
                }
                Thread.sleep((250/speed).toLong())
            }

            faders.last().config.setColor(Color.OFF)
            faders.last().config.setSolo(false)
            faders.last().mix.setMute(false)

            Thread.sleep((2000/speed).toLong())


            while (animationThreadRunning) {
                for (fader in faders) {
                    fader.config.setColor(cols[index])
                    Thread.sleep(((1000/64)/speed).toLong())
                }

                if(index < 5) index++ else index = 0

                Thread.sleep(((1000 / 4) /speed).toLong())
            }
        }

        animationThread.start()

        runBlocking {
            osc.getStatus().setLock(true)
            val currentScreen = osc.getStatus().getScreen()

            osc.getStatus().setScreen(if (currentScreen == Screen.LOCK) Screen.HOME else currentScreen)
        }
    }

}