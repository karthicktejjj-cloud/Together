package com.together.app.network.socket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

class SocketClient(
    private val host: String,
    private val port: Int,
    private val onMessageReceived: (Message) -> Unit
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var socket: Socket? = null
    private var writer: PrintWriter? = null
    private var isRunning = false
    val clientId = "Guest_${System.currentTimeMillis() % 10000}"

    fun connect() {
        isRunning = true
        scope.launch {
            while (isRunning) {
                try {
                    Log.d("JOIN_TRACE", "Step 1: Before opening socket to $host:$port")
                    Log.d("SocketClient", "Attempting connection to $host:$port")
                    val newSocket = Socket(host, port)
                    socket = newSocket
                    Log.d("JOIN_TRACE", "Step 2: Socket connected to $host:$port")
                    Log.d("SocketClient", "Connected to $host:$port. Local address: ${newSocket.localSocketAddress}")
                    
                    writer = PrintWriter(newSocket.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(newSocket.getInputStream()))

                    // Start heartbeat sender
                    startHeartbeat()

                    while (isRunning) {
                        Log.d("SocketClient", "Waiting for message from server...")
                        val json = withContext(Dispatchers.IO) { reader.readLine() }
                        if (json == null) {
                            Log.d("SocketClient", "Server closed the connection (readLine returned null)")
                            break
                        }
                        Log.d("SocketClient", "JSON received from server: $json")
                        try {
                            val message = gson.fromJson(json, Message::class.java)
                            Log.d("SocketClient", "Message parsed: ${message.type} from ${message.sender}")
                            onMessageReceived(message)
                        } catch (e: Exception) {
                            Log.e("SocketClient", "Networking error: Error parsing message from server: $json", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("SocketClient", "Networking error: Connection error, retrying in 2s...", e)
                    delay(2000)
                } finally {
                    Log.d("SocketClient", "Finalizing connection to $host:$port")
                    try {
                        socket?.close()
                        Log.d("SocketClient", "Socket closed")
                    } catch (e: Exception) {
                        Log.e("SocketClient", "Networking error: Error closing socket", e)
                    }
                    socket = null
                    writer = null
                }
            }
        }
    }

    private fun startHeartbeat() {
        Log.d("SocketClient", "Starting heartbeat sender")
        scope.launch {
            while (isRunning && socket?.isConnected == true) {
                delay(5000)
                Log.d("SocketClient", "Triggering heartbeat send")
                sendMessage(Message(MessageType.HEARTBEAT, clientId))
            }
        }
    }

    fun sendMessage(message: Message) {
        scope.launch {
            try {
                val json = gson.toJson(message)
                if (message.type == MessageType.JOIN_ROOM) {
                    Log.d("JOIN_TRACE", "Step 4: JOIN_ROOM JSON sent: $json")
                }
                Log.d("SocketClient", "Sending message ${message.type}: $json")
                if (writer != null) {
                    writer?.println(json)
                    Log.d("SocketClient", "Message sent successfully")
                } else {
                    Log.w("SocketClient", "Cannot send message, writer is null")
                }
            } catch (e: Exception) {
                Log.e("SocketClient", "Networking error: Error sending message", e)
            }
        }
    }

    fun disconnect() {
        Log.d("SocketClient", "Disconnecting...")
        isRunning = false
        scope.launch(Dispatchers.IO) {
            try {
                socket?.close()
                Log.d("SocketClient", "Socket closed manually")
            } catch (e: Exception) {
                Log.e("SocketClient", "Networking error: Error during disconnect socket close", e)
            }
            scope.cancel()
            Log.d("SocketClient", "Scope cancelled")
        }
    }
}
