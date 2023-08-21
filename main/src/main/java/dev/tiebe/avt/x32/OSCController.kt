package dev.tiebe.avt.x32

import com.illposed.osc.transport.OSCPortOut
import java.net.InetAddress

class OSCController(ip: String, port: Int) {
    private val client = OSCPortOut(InetAddress.getByName(ip), port)

    private val registeredCallbacks = mutableListOf<() -> Unit>()

    fun connect() {
        client.connect()
    }

    fun addMessageCallback(callback: () -> Unit) {
        registeredCallbacks.add(callback)
    }


}