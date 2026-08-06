package com.together.app.network.socket

data class Message(
    val type: MessageType,
    val sender: String,
    val payload: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
