package dev.tiebe.avt.x32

import com.illposed.osc.*
import com.illposed.osc.transport.OSCPort
import com.illposed.osc.transport.OSCPortIn
import com.illposed.osc.transport.OSCPortOut
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.net.InetSocketAddress

@Suppress("MemberVisibilityCanBePrivate", "unused") //Public API, so don't need IDE warnings.
class OSCController(ip: String, port: Int, localPort: Int, daemonThread: Boolean = true) {
    val remote = InetSocketAddress(InetAddress.getByName(ip), port)
    private val client = OSCPortOut(OSCSerializerAndParserBuilder(), remote, InetSocketAddress(OSCPort.generateWildcard(remote), localPort))
    private val server = OSCPortIn(localPort).apply { isDaemonListener = daemonThread }

    private val registeredCallbacks = mutableListOf<(OSCMessageEvent) -> Unit>()

    fun addMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.add(callback)
    fun addMessageCallback(callback: OSCMessageListener) = registeredCallbacks.add(callback::acceptMessage)
    fun removeMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.remove(callback)
    fun removeMessageCallback(callback: OSCMessageListener) = registeredCallbacks.remove(callback::acceptMessage)

    fun sendMessage(message: OSCMessage) {
        val maxMessageSize = 512  // Replace with the buffer's actual size
        var totalMessageSize = 0

        message.arguments.forEachIndexed { index, argument ->
            if (argument is String) {
                if (totalMessageSize + argument.length > maxMessageSize) {
                    throw IllegalArgumentException("The message size limit of $maxMessageSize has been exceeded")
                }

                val paddedArgument = argument + "\u0000".repeat(4 - (argument.length % 4))
                totalMessageSize += paddedArgument.length
                message.arguments[index] = paddedArgument
            }
        }

        client.send(message)
    }

    suspend fun getValue(message: OSCMessage): OSCMessage? {
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

        return withTimeoutOrNull(2000) {
            val result = channel.receive()

            removeMessageCallback(listener)
            channel.close()

            return@withTimeoutOrNull result
        }
    }



    init {
        server.dispatcher.addListener(
            object : MessageSelector {
                override fun isInfoRequired(): Boolean = false
                override fun matches(messageEvent: OSCMessageEvent?): Boolean = true
            }
        ) { message ->
            println(message)
            registeredCallbacks.iterator().forEach { it(message) }
        }

        server.startListening()
    }
}