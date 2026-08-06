package com.together.app.network.socket

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap

class SocketServer(
    private val port: Int,
    private val onMessageReceived: (Message) -> Unit
) {
    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    
    private val clients = ConcurrentHashMap<String, PrintWriter>()

    fun start() {
        isRunning = true
        scope.launch {
            try {
                serverSocket = ServerSocket(port)
                Log.d("SocketServer", "Server socket created on port $port")
                
                // Start heartbeat job
                startHeartbeat()

                while (isRunning) {
                    Log.d("SocketServer", "Waiting for client connection...")
                    val clientSocket = serverSocket?.accept() ?: break
                    Log.d("JOIN_TRACE", "Step 6: Socket accepted from ${clientSocket.remoteSocketAddress}")
                    Log.d("SocketServer", "New client accepted: ${clientSocket.remoteSocketAddress}")
                    scope.launch {
                        handleClient(clientSocket)
                    }
                }
            } catch (e: Exception) {
                Log.e("JOIN_TRACE", "Step FAILURE: Error starting server on port $port", e)
                Log.e("SocketServer", "Error starting server", e)
            }
        }
    }

    private suspend fun handleClient(socket: Socket) {
        val remoteAddress = socket.remoteSocketAddress
        Log.d("SocketServer", "Handling client: $remoteAddress")
        val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val writer = PrintWriter(socket.getOutputStream(), true)
        var clientId: String? = null

        try {
            while (isRunning) {
                val json = try {
                    withContext(Dispatchers.IO) { reader.readLine() }
                } catch (e: Exception) {
                    Log.e("JOIN_TRACE", "Error reading from client $remoteAddress: ${e.message}", e)
                    null
                }

                if (json == null) {
                    Log.d("JOIN_TRACE", "Socket closed by Guest $remoteAddress (readLine returned null or error)")
                    Log.d("SocketServer", "Client $remoteAddress ($clientId) closed the stream")
                    break
                }
                Log.d("SocketServer", "JSON received from $clientId: $json")
                
                val message = try {
                    val msg = gson.fromJson(json, Message::class.java)
                    if (msg.type == MessageType.JOIN_ROOM) {
                        Log.d("JOIN_TRACE", "Step 8: JSON parsed on Host from $remoteAddress")
                    }
                    msg
                } catch (e: Exception) {
                    Log.e("JOIN_TRACE", "Step 8 FAILURE: Failed to parse JSON from $remoteAddress: $json", e)
                    Log.e("SocketServer", "Failed to parse JSON from $remoteAddress: $json", e)
                    continue
                }
                Log.d("SocketServer", "Message parsed: ${message.type} from ${message.sender}")
                
                if (message.type == MessageType.HEARTBEAT) {
                    Log.d("SocketServer", "HEARTBEAT received from client $clientId")
                }

                if (message.type == MessageType.JOIN_ROOM) {
                    Log.d("JOIN_TRACE", "Step 7: JOIN_ROOM message detected from $remoteAddress")
                    val id = message.sender ?: "Guest_${System.currentTimeMillis()}"
                    clientId = id
                    clients[id] = writer
                }
                
                onMessageReceived(message)
                
                if (message.type == MessageType.LEAVE_ROOM) {
                    break
                }

                // Broadcast to others if needed (e.g. PLAY/PAUSE)
                if (message.type != MessageType.HEARTBEAT && message.type != MessageType.PARTICIPANT_LIST_UPDATED) {
                    broadcast(message, excludeSender = clientId)
                }
            }
        } catch (e: Exception) {
            Log.e("JOIN_TRACE", "Step FAILURE: Error handling client $clientId at $remoteAddress", e)
            Log.e("SocketServer", "Error handling client $clientId at $remoteAddress", e)
        } finally {
            Log.d("JOIN_TRACE", "Socket connection with $clientId at $remoteAddress finalized (closed)")
            clientId?.let { 
                Log.d("SocketServer", "Removing client $it from clients map")
                clients.remove(it) 
            }
            try {
                socket.close()
                Log.d("SocketServer", "Socket closed for $clientId")
            } catch (e: Exception) {
                Log.e("SocketServer", "Error closing socket for $clientId", e)
            }
            Log.d("SocketServer", "Emitting LEAVE_ROOM for $clientId")
            onMessageReceived(Message(MessageType.LEAVE_ROOM, clientId ?: "Unknown"))
        }
    }

    fun broadcast(message: Message, excludeSender: String? = null) {
        scope.launch {
            val json = gson.toJson(message)
            Log.d("SocketServer", "Broadcasting message ${message.type} (excluding $excludeSender): $json")
            clients.forEach { (id, writer) ->
                if (id != excludeSender) {
                    try {
                        writer.println(json)
                        Log.d("SocketServer", "Message sent to client: $id")
                    } catch (e: Exception) {
                        Log.e("SocketServer", "Error sending message to client $id", e)
                    }
                }
            }
        }
    }

    private fun startHeartbeat() {
        Log.d("SocketServer", "Starting heartbeat job")
        scope.launch {
            while (isRunning) {
                delay(5000)
                Log.d("SocketServer", "Triggering periodic heartbeat")
                val heartbeat = Message(MessageType.HEARTBEAT, "SERVER")
                broadcast(heartbeat)
            }
        }
    }

    fun stop() {
        isRunning = false
        scope.launch(Dispatchers.IO) {
            serverSocket?.close()
            clients.values.forEach { it.close() }
            clients.clear()
            scope.cancel()
        }
    }
}
