package com.together.app.model

data class DiscoveredRoom(
    val roomId: String,
    val roomCode: String,
    val roomName: String,
    val hostName: String,
    val deviceName: String,
    val ipAddress: String,
    val port: Int
)
