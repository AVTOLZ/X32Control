package dev.tiebe.avt.x32

import com.illposed.osc.OSCMessage
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

        oscServer.connect()
        oscClient.sendMessage(OSCMessage("/test"))

        Thread.sleep(200)

        assertTrue(received)
    }

    @Test
    fun testGetValue() {

    }
}