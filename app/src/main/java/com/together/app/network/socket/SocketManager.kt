package com.together.app.network.socket

import android.content.Context
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class SocketManager private constructor() {

    private var server: SocketServer? = null
    private var client: SocketClient? = null

    private val _messages = MutableSharedFlow<Message>(extraBufferCapacity = 10)
    val messages: SharedFlow<Message> = _messages.asSharedFlow()

    fun getClientId(): String? {
        val id = client?.clientId ?: "Host"
        android.util.Log.d("SocketManager", "getClientId: $id")
        return id
    }

    fun startServer(port: Int) {
        if (server != null) {
            android.util.Log.d("SocketManager", "Server already running on port $port")
            return
        }
        android.util.Log.d("SocketManager", "startServer on port $port")
        stopAll()
        server = SocketServer(port) { message ->
            android.util.Log.d("SocketManager", "Server received message: ${message.type}")
            _messages.tryEmit(message)
        }
        server?.start()
    }

    fun startClient(host: String, port: Int) {
        if (client != null) {
            android.util.Log.d("SocketManager", "Client already connected to $host:$port")
            return
        }
        android.util.Log.d("SocketManager", "startClient connecting to $host:$port")
        stopAll()
        client = SocketClient(host, port) { message ->
            android.util.Log.d("SocketManager", "Client received message: ${message.type}")
            _messages.tryEmit(message)
        }
        client?.connect()
    }

    fun sendMessage(message: Message): Boolean {
        android.util.Log.d("SocketManager", "sendMessage: ${message.type} from ${message.sender}")
        server?.broadcast(message)
        return client?.sendMessage(message) ?: true
    }

    fun stopAll() {
        android.util.Log.d("SocketManager", "stopAll")
        server?.stop()
        client?.disconnect()
        server = null
        client = null
    }

    companion object {
        @Volatile
        private var INSTANCE: SocketManager? = null

        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }
}
