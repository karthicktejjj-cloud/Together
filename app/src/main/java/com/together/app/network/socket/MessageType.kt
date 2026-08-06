package com.together.app.network.socket

enum class MessageType {
    JOIN_ROOM,
    LEAVE_ROOM,
    CHAT,
    READY,
    PLAY,
    PAUSE,
    SEEK,
    HEARTBEAT,
    PARTICIPANT_LIST_UPDATED,
    USER_READY,
    START_WATCHING,
    STREAM_URL
}
