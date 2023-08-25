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
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

@Suppress("MemberVisibilityCanBePrivate", "unused") //Public API, so don't need IDE warnings.
class OSCController(ip: String, port: Int, localPort: Int, daemonThread: Boolean = true) {
    val remote = InetSocketAddress(InetAddress.getByName(ip), port)
    private val client = OSCPortOut(OSCSerializerAndParserBuilder(), remote, InetSocketAddress(OSCPort.generateWildcard(remote), localPort))
    private val server = OSCPortIn(localPort).apply { isDaemonListener = daemonThread }

    private val registeredCallbacks = CopyOnWriteArrayList<(OSCMessageEvent) -> Unit>()

    fun addMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.add(callback)
    fun addMessageCallback(callback: OSCMessageListener) = registeredCallbacks.add(callback::acceptMessage)
    fun removeMessageCallback(callback: (OSCMessageEvent) -> Unit) = registeredCallbacks.remove(callback)
    fun removeMessageCallback(callback: OSCMessageListener) = registeredCallbacks.remove(callback::acceptMessage)

    fun connect() {
        server.startListening()
    }

    val subscriptionThreads = mutableMapOf<UUID, Pair<Thread, (OSCMessageEvent) -> Unit>>()

    fun subscribe(address: String, updateTime: Int = 0, onReceive: (it: OSCMessageEvent) -> Unit): UUID {
        val messageCallback = { it: OSCMessageEvent ->
            if (it.message.address == address) {
                onReceive(it)
            }
        }

        val message = OSCMessage("/subscribe", listOf(address, updateTime))
        sendMessage(message)

        val thread = Thread {
            while (true) {
                if (Thread.interrupted()) {
                    println("INTERRUPTED")
                    sendMessage(OSCMessage("/unsubscribe", listOf(address)))
                    return@Thread
                }
                Thread.sleep(3500)

                sendMessage(OSCMessage("/renew", listOf(address)))
            }
        }

        thread.start()
        addMessageCallback(messageCallback)


        val uuid = UUID.randomUUID()
        subscriptionThreads[uuid] = thread to messageCallback

        return uuid
    }

    fun unsubscribe(uuid: UUID) {
        subscriptionThreads[uuid]?.first?.interrupt()
        removeMessageCallback(subscriptionThreads[uuid]?.second ?: return)
        subscriptionThreads.remove(uuid)
    }


    fun sendMessage(message: OSCMessage) {
        val maxMessageSize = 512  // Replace with the buffer's actual size
        var totalMessageSize = 0

        val paddedArgs = message.arguments.map { argument ->
            if (argument is String) {
                if (totalMessageSize + argument.length > maxMessageSize) {
                    throw IllegalArgumentException("The message size limit of $maxMessageSize has been exceeded")
                }

                val paddedArgument = argument + "\u0000".repeat(4 - (argument.length % 4))
                totalMessageSize += paddedArgument.length
                return@map paddedArgument
            }
            argument
        }

        client.send(OSCMessage(message.address, paddedArgs, message.info))
    }

    suspend fun getValue(message: OSCMessage): OSCMessage? {
        val channel = Channel<OSCMessage>()

        val listener = OSCMessageListener {
            if (it.message.address.startsWith(message.address)) {
                runBlocking {
                    launch {
                        channel.send(it.message)
                    }
                }
            }
        }

        addMessageCallback(listener)

        sendMessage(message)

        return withTimeoutOrNull(2000) {
            val result = channel.receive()

            removeMessageCallback(listener)
            channel.close()

            return@withTimeoutOrNull result
        }
    }



    init {
        server.dispatcher.addBadDataListener {
            println("Bad data received")
        }

        server.dispatcher.addListener(
            object : MessageSelector {
                override fun isInfoRequired(): Boolean = false
                override fun matches(messageEvent: OSCMessageEvent?): Boolean = true
            }
        ) { message ->
            registeredCallbacks.iterator().forEach { it(message) }
        }

        server.startListening()
    }
}