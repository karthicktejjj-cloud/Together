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

                var rangeHeader: String? = null
                var line: String? = reader.readLine()
                while (!line.isNullOrEmpty()) {
                    if (line.startsWith("Range:", ignoreCase = true)) {
                        rangeHeader = line.substring("Range:".length).trim()
                    }
                    line = reader.readLine()
                }

                val isGet = requestLine.startsWith("GET /video") || requestLine.startsWith("GET /")
                val isHead = requestLine.startsWith("HEAD /video") || requestLine.startsWith("HEAD /")

                if (isGet || isHead) {
                    streamVideo(socket.getOutputStream(), sendBody = isGet, rangeHeader = rangeHeader)
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
                } catch (_: Exception) {
                    Log.e("VideoServer", "Networking error: Error closing socket for $remoteAddress")
                }
            }
        }
    }

    private fun streamVideo(output: OutputStream, sendBody: Boolean, rangeHeader: String?) {
        val uri = videoUri ?: return
        var inputStream: InputStream? = null
        try {
            // Obtain real file size from AssetFileDescriptor
            val totalBytes = try {
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            } catch (e: Exception) {
                -1L
            }

            if (totalBytes <= 0) {
                Log.e("VideoServer", "Could not determine file size for $uri")
                send404(output)
                return
            }

            var start = 0L
            var end = totalBytes - 1
            var isPartial = false

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                val rangeValue = rangeHeader.substring(6)
                try {
                    if (rangeValue.startsWith("-")) {
                        // bytes=-500 (last 500 bytes)
                        val suffix = rangeValue.substring(1).toLong()
                        start = (totalBytes - suffix).coerceAtLeast(0)
                        end = totalBytes - 1
                        isPartial = true
                    } else {
                        val parts = rangeValue.split("-")
                        start = parts[0].toLong()
                        if (parts.size > 1 && parts[1].isNotEmpty()) {
                            end = parts[1].toLong()
                        } else {
                            end = totalBytes - 1
                        }
                        isPartial = true
                    }
                } catch (e: Exception) {
                    Log.e("VideoServer", "Error parsing range header: $rangeHeader")
                    isPartial = false
                }
            }

            // Range validation
            if (start < 0) start = 0
            if (end >= totalBytes) end = totalBytes - 1
            if (start > end) {
                isPartial = false
                start = 0
                end = totalBytes - 1
            }

            val contentLength = end - start + 1
            val status = if (isPartial) "HTTP/1.1 206 Partial Content" else "HTTP/1.1 200 OK"
            
            val responseHeaders = StringBuilder()
            responseHeaders.append("$status\r\n")
            responseHeaders.append("Content-Type: video/mp4\r\n")
            responseHeaders.append("Content-Length: $contentLength\r\n")
            responseHeaders.append("Accept-Ranges: bytes\r\n")
            if (isPartial) {
                responseHeaders.append("Content-Range: bytes $start-$end/$totalBytes\r\n")
            }
            responseHeaders.append("Connection: close\r\n\r\n")

            output.write(responseHeaders.toString().toByteArray())
            output.flush()

            Log.d("VideoServer", "Sent headers: $status (Start: $start, End: $end, Size: $contentLength)")

            if (sendBody) {
                inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    if (start > 0) {
                        inputStream.skip(start)
                    }

                    val buffer = ByteArray(64 * 1024)
                    var remaining = contentLength
                    while (remaining > 0) {
                        val toRead = remaining.coerceAtMost(buffer.size.toLong()).toInt()
                        val bytesRead = inputStream.read(buffer, 0, toRead)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                        remaining -= bytesRead
                    }
                    output.flush()
                    Log.d("VideoServer", "Streaming completed for range $start-$end")
                }
            }
        } catch (e: Exception) {
            Log.e("VideoServer", "Error during video streaming", e)
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
