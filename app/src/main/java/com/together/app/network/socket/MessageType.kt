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
    STREAM_URL,
    PREPARE_PLAYBACK,
    PLAYER_READY,
    SYNC_START,
    VIDEO_ENDED,
    REQUEST_NEXT_VIDEO_SELECTION
}
