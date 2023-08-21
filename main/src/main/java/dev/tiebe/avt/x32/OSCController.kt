package dev.tiebe.avt.x32

import com.illposed.osc.MessageSelector
import com.illposed.osc.OSCMessage
import com.illposed.osc.OSCMessageEvent
import com.illposed.osc.OSCMessageListener
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.util.concurrent.Future
import java.util.concurrent.FutureTask

class OSCController(ip: String, port: Int, localPort: Int, daemonThread: Boolean = true) {
    private val client = OSCPortOut(InetAddress.getByName(ip), port)
    private val server = OSCPortIn(localPort).apply { isDaemonListener = daemonThread }

    private val registeredCallbacks = mutableListOf<(OSCMessageEvent) -> Unit>()

    fun addMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.add(callback)
    fun addMessageCallback(callback: OSCMessageListener) = registeredCallbacks.add(callback::acceptMessage)
    fun removeMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.remove(callback)
    fun removeMessageCallback(callback: OSCMessageListener) = registeredCallbacks.remove(callback::acceptMessage)

    fun sendMessage(message: OSCMessage) {
        client.send(message)
    }

    suspend fun getValue(message: OSCMessage): OSCMessage {
        val channel = Channel<OSCMessage>()

        val listener = OSCMessageListener {
            if (it.message.address == message.address) {
                runBlocking {
                    launch {
                        channel.send(it.message)
                    }
                }
            }
        }

        addMessageCallback(listener)

        client.send(message)
        val result = channel.receive()

        removeMessageCallback(listener)
        channel.close()

        return result
    }



    init {
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
}