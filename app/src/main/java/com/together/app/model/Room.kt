package com.together.app.model

import android.net.Uri

data class Room(
    val roomId: String,
    val roomCode: String,
    val roomName: String,
    val hostName: String,
    val videoUri: Uri,
    val videoTitle: String,
    val createdTime: Long,
    val participants: List<Participant>,
    val maxParticipants: Int,
    val status: RoomStatus
)

enum class RoomStatus {
    WAITING,
    WATCHING,
    FINISHED,
    CLOSED
}
