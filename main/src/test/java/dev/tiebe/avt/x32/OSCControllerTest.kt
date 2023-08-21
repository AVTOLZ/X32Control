package dev.tiebe.avt.x32

import com.illposed.osc.OSCMessage
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

class OSCControllerTest {
    private val oscClient = OSCController("localhost", 10023, 10024)
    private val oscServer = OSCController("localhost", 10024, 10023)

    @Test
    fun testSendAndReceive() {
        var received = false
        oscServer.addMessageCallback {
            if (it.message.address == "/test")
                received = true
        }

        oscClient.sendMessage(OSCMessage("/test"))

        Thread.sleep(200)

        assertTrue(received)
    }

    @Test
    fun testGetValue() {
        runBlocking {
            oscServer.addMessageCallback {
                if (it.message.address == "/test")
                    oscServer.sendMessage(OSCMessage("/test", listOf(1)))
            }

            val message = oscClient.getValue(OSCMessage("/test"))

            assertEquals("/test", message.address)
            assertEquals(1, message.arguments[0])
        }
    }
}