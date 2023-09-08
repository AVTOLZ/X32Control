package dev.tiebe.avt.x32

import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageEvent
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class QuickQOSCTest {
    private val oscClient = QuickQOSC("localhost", 10023, 10024)
    private val oscServer = QuickQOSC("localhost", 10024, 10023)

    @Test
    fun testSendAndReceive() {
        var received = false
        val callback = { it: OSCMessageEvent ->
            if (it.message.address == "/test")
                received = true
        }
        oscServer.addMessageCallback(callback)
        oscClient.sendMessage(OSCMessage("/test"))

        Thread.sleep(200)

        assertTrue(received)

        oscServer.removeMessageCallback(callback)
    }

    @Test
    fun testGetValue() {
        runBlocking {
            val callback = { it: OSCMessageEvent ->
                if (it.message.address == "/test")
                    oscServer.sendMessage(OSCMessage("/test", listOf(1)))
            }

            oscServer.addMessageCallback(callback)

            val message = oscClient.getValue(OSCMessage("/test"))

            assertEquals("/test", message?.address)
            assertEquals(1, message?.arguments?.get(0) ?: -1)

            oscServer.removeMessageCallback(callback)

            val callback2 = { it: OSCMessageEvent ->
                if (it.message.address == "/test")
                    oscServer.sendMessage(OSCMessage("/invalidtest", listOf(1)))
            }

            oscServer.addMessageCallback(callback2)

            val message2 = oscClient.getValue(OSCMessage("/test"))
            assertNull(message2)

            oscServer.removeMessageCallback(callback2)
        }
    }
}