package com.together.app.data.repository

import com.together.app.model.Room
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class RoomRepository {
    private val _rooms = mutableMapOf<String, Room>()
    
    private val _currentRoom = MutableStateFlow<Room?>(null)
    val currentRoom: StateFlow<Room?> = _currentRoom

    fun setCurrentRoom(room: Room?) {
        _currentRoom.value = room
    }

    fun createRoom(room: Room): Room {
        _rooms[room.roomId] = room
        _currentRoom.value = room
        return room
    }

    fun getRoom(roomId: String): Room? {
        return _rooms[roomId]
    }
    
    fun generateRoomCode(): String {
        return UUID.randomUUID().toString().substring(0, 6).uppercase()
    }

    fun updateParticipants(participants: List<com.together.app.model.Participant>) {
        _currentRoom.value = _currentRoom.value?.copy(participants = participants)
    }

    fun updateRoomStatus(status: com.together.app.model.RoomStatus) {
        _currentRoom.value = _currentRoom.value?.copy(status = status)
    }
}
