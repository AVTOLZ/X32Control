package dev.tiebe.avt.x32

import com.illposed.osc.MessageSelector
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageEvent
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import java.net.InetAddress
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class OSCController(ip: String, port: Int, localPort: Int, daemonThread: Boolean = true) {
    private val client = OSCPortOut(InetAddress.getByName(ip), port)
    private val server = OSCPortIn(localPort).apply { isDaemonListener = daemonThread }

    private val registeredCallbacks = mutableListOf<(OSCMessageEvent) -> Unit>()

    fun connect() {
        server.dispatcher.addListener(
            object : MessageSelector {
                override fun isInfoRequired(): Boolean = false
                override fun matches(messageEvent: OSCMessageEvent?): Boolean = true
            }
        ) { message ->
            registeredCallbacks.forEach { it(message) }
        }

        server.startListening()


    }

    fun addMessageCallback(callback: (OSCMessageEvent) -> Unit) {
        registeredCallbacks.add(callback)
    }

    fun sendMessage(message: OSCMessage) {
        client.send(message)
    }

    fun getValue(message: OSCMessage): Float {
        TODO("Not yet implemented")

        server.dispatcher.addListener(
            object : MessageSelector {
                override fun isInfoRequired(): Boolean = false
                override fun matches(messageEvent: OSCMessageEvent?): Boolean = messageEvent?.message?.address == message.address
            }
        ) {

        }
    }


}