package com.together.app.model

data class Participant(
    val id: String,
    val name: String,
    val isHost: Boolean = false,
    val isReady: Boolean = false
)
