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
    // Use a reusable CoroutineScope that we don't cancel in stop()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null
    private var videoUri: Uri? = null

    fun start(uri: Uri) {
        Log.d("VideoServer", "VideoServer: START requested")
        this.videoUri = uri
        
        // Ensure any existing server loop is stopped before starting a new one
        stop()
        
        isRunning = true
        serverJob = scope.launch {
            try {
                Log.d("VideoServer", "VideoServer: Listening on port $port")
                serverSocket = ServerSocket(port)
                
                val hostIp = NetworkUtils.getLocalIpAddress(context) ?: "Unknown"
                Log.d("VideoServer", "Video server SUCCESSFULLY started at http://$hostIp:$port/video")
                
                while (isRunning && isActive) {
                    val clientSocket = try {
                        serverSocket?.accept()
                    } catch (e: Exception) {
                        null
                    } ?: break
                    
                    Log.d("VideoServer", "VideoServer: Client connected from ${clientSocket.remoteSocketAddress}")
                    handleClient(clientSocket)
                }
            } catch (e: Exception) {
                if (isRunning) {
                    Log.e("VideoServer", "VideoServer: Critical failure in video server startup", e)
                }
            } finally {
                Log.d("VideoServer", "VideoServer: Server loop exited")
            }
        }
    }

    private fun handleClient(socket: Socket) {
        val remoteAddress = socket.remoteSocketAddress
        scope.launch {
            try {
                val input = socket.getInputStream()
                val reader = input.bufferedReader()
                val requestLine = reader.readLine()
                Log.d("VideoServer", "HTTP request from $remoteAddress: $requestLine")

                if (requestLine == null) {
                    socket.close()
                    return@launch
                }

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
                } catch (_: Exception) {
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
        Log.d("VideoServer", "VideoServer: STOP requested")
        isRunning = false
        serverJob?.cancel()
        try {
            serverSocket?.close()
            Log.d("VideoServer", "VideoServer: SOCKET CLOSED")
        } catch (e: Exception) {
            Log.e("VideoServer", "VideoServer: Error closing socket", e)
        }
        serverSocket = null
        serverJob = null
    }

    /**
     * Permanently destroys the server and its scope.
     */
    fun destroy() {
        stop()
        scope.cancel()
    }
}
