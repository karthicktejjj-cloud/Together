package com.together.app.network.streaming

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.*
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import com.together.app.utils.NetworkUtils

class VideoServer(
    private val context: Context,
    private val port: Int
) {
    private var serverSocket: ServerSocket? = null
    private var isRunning = false
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var videoUri: Uri? = null

    fun start(uri: Uri) {
        this.videoUri = uri
        isRunning = true
        scope.launch {
            try {
                // Reliability fix: ensure any previous socket is closed
                serverSocket?.close()
                
                Log.d("VideoServer", "Attempting to start video server on port $port")
                serverSocket = ServerSocket(port)
                
                val hostIp = NetworkUtils.getLocalIpAddress(context) ?: "Unknown"
                Log.d("VideoServer", "Video server SUCCESSFULLY started")
                Log.d("VideoServer", ">> Host IP: $hostIp")
                Log.d("VideoServer", ">> Listening Port: $port")
                Log.d("VideoServer", ">> Video Path: $uri")
                
                while (isRunning) {
                    val clientSocket = serverSocket?.accept() ?: break
                    Log.d("VideoServer", "Accepted new connection from: ${clientSocket.remoteSocketAddress}")
                    handleClient(clientSocket)
                }
            } catch (e: Exception) {
                Log.e("VideoServer", "Networking error: Critical failure in video server startup", e)
            }
        }
    }

    private fun handleClient(socket: Socket) {
        val remoteAddress = socket.remoteSocketAddress
        Log.d("VideoServer", "Connection started: $remoteAddress")
        scope.launch {
            try {
                val input = socket.getInputStream()
                val reader = input.bufferedReader()
                val requestLine = reader.readLine()
                Log.d("VideoServer", "HTTP request from $remoteAddress: $requestLine")

                if (requestLine == null) return@launch

                // Consume all remaining headers to ensure clean TCP close
                var line: String?
                while (reader.readLine().also { line = it } != null && line!!.isNotEmpty()) {
                    // Just consuming
                }

                val isGet = requestLine.startsWith("GET /video") || requestLine.startsWith("GET /")
                val isHead = requestLine.startsWith("HEAD /video") || requestLine.startsWith("HEAD /")

                if (isGet || isHead) {
                    Log.d("VideoServer", "Incoming request: ${if (isGet) "GET" else "HEAD"} /video")
                    Log.d("VideoServer", "Matched request. Sending Response: 200 OK")
                    streamVideo(socket.getOutputStream(), sendBody = isGet)
                } else {
                    Log.w("VideoServer", "Unrecognized request from $remoteAddress: $requestLine. Sending Response: 404 Not Found")
                    send404(socket.getOutputStream())
                }
            } catch (e: java.net.SocketException) {
                Log.e("VideoServer", "Networking error: SocketException handling client $remoteAddress", e)
            } catch (e: java.io.IOException) {
                Log.e("VideoServer", "Networking error: IOException handling client $remoteAddress", e)
            } catch (e: Exception) {
                Log.e("VideoServer", "Networking error: Unexpected error handling client $remoteAddress", e)
            } finally {
                Log.d("VideoServer", "Closing connection for client: $remoteAddress")
                try {
                    socket.close()
                } catch (e: Exception) {
                    Log.e("VideoServer", "Networking error: Error closing socket for $remoteAddress", e)
                }
            }
        }
    }

    private fun streamVideo(output: OutputStream, sendBody: Boolean) {
        val uri = videoUri ?: return
        var inputStream: InputStream? = null
        try {
            inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                send404(output)
                return
            }

            // Improve file size detection
            val totalBytes = try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: inputStream.available().toLong()
            } catch (_: Exception) {
                inputStream.available().toLong()
            }
            
            Log.d("VideoServer", "Streaming headers for file size: $totalBytes bytes. Send body: $sendBody")
            
            // Send HTTP headers
            val headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: video/mp4\r\n" +
                    "Content-Length: $totalBytes\r\n" +
                    "Accept-Ranges: bytes\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(headers.toByteArray())
            output.flush()
            Log.d("VideoServer", "HTTP Response Headers Sent: 200 OK")

            if (sendBody) {
                // Stream file content
                val buffer = ByteArray(64 * 1024) // 64KB buffer
                var bytesRead: Int
                var totalRead: Long = 0
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                }
                output.flush()
                Log.d("VideoServer", "Streaming finished successfully. Total bytes sent: $totalRead")
            }
        } catch (e: java.net.SocketException) {
            Log.e("VideoServer", "Networking error: SocketException during file streaming", e)
        } catch (e: java.io.IOException) {
            Log.e("VideoServer", "Networking error: IOException during file streaming", e)
        } catch (e: Exception) {
            Log.e("VideoServer", "Networking error: Unexpected error during file streaming", e)
        } finally {
            inputStream?.close()
        }
    }

    private fun send404(output: OutputStream) {
        val response = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n"
        output.write(response.toByteArray())
        output.flush()
    }

    fun stop() {
        isRunning = false
        scope.launch(Dispatchers.IO) {
            serverSocket?.close()
            scope.cancel()
        }
    }
}
