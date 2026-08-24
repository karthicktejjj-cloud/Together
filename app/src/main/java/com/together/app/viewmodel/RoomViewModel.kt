package com.together.app.viewmodel

import androidx.lifecycle.ViewModel
import com.together.app.data.repository.RoomRepository
import com.together.app.model.Room
import kotlinx.coroutines.flow.StateFlow

import com.together.app.network.socket.Message
import com.together.app.network.socket.MessageType
import com.together.app.network.socket.SocketManager
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import com.together.app.network.streaming.VideoServer
import com.together.app.utils.NetworkUtils
import android.content.Context
import android.net.Uri

class RoomViewModel : ViewModel() {
    private val repository = RoomRepository()
    private val socketManager = SocketManager.getInstance()
    private var videoServer: VideoServer? = null
    
    val currentRoom: StateFlow<Room?> = repository.currentRoom
    private var pendingRoomCode: String? = null
    private var pendingRoomName: String? = null
    
    // Coordination state
    private val readyParticipants = mutableSetOf<String>()
    private var isPreparing = false

    private val _navigationEvent = kotlinx.coroutines.flow.MutableSharedFlow<String>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            socketManager.messages.collectLatest { message ->
                handleSocketMessage(message)
            }
        }
    }

    private fun handleSocketMessage(message: Message) {
        android.util.Log.d("JOIN_TRACE", "Step 8: Message received in ViewModel: ${message.type} from ${message.sender}")
        android.util.Log.d("RoomViewModel", "handleSocketMessage: ${message.type} from ${message.sender}")
        val gson = com.google.gson.Gson()
        val myId = socketManager.getClientId() ?: "Host"
        when (message.type) {
            MessageType.HEARTBEAT -> {
                android.util.Log.d("RoomViewModel", "Heartbeat received from ${message.sender}")
            }
            MessageType.PARTICIPANT_LIST_UPDATED -> {
                val payload = message.payload ?: run {
                    android.util.Log.w("RoomViewModel", "PARTICIPANT_LIST_UPDATED received with null payload")
                    return
                }
                
                val current = currentRoom.value
                val isGuest = pendingRoomCode != null && (current == null || current.roomCode != pendingRoomCode)
                
                if (isGuest) {
                    android.util.Log.d("JOIN_TRACE", "Step 11: JOIN_SUCCESS (PARTICIPANT_LIST_UPDATED) received on Guest")
                } else {
                    android.util.Log.d("JOIN_TRACE", "PARTICIPANT_LIST_UPDATED received on Host")
                }
                
                android.util.Log.d("RoomViewModel", "Updating participant list: $payload")
                val type = object : com.google.gson.reflect.TypeToken<List<com.together.app.model.Participant>>() {}.type
                try {
                    val participants: List<com.together.app.model.Participant> = gson.fromJson(payload, type)
                    android.util.Log.d("RoomViewModel", "Parsed ${participants.size} participants")
                    
                    val current = currentRoom.value
                    if (current == null || current.roomCode != pendingRoomCode) {
                        // Initialize room for Guest if not already set or if it's a different room
                        if (pendingRoomCode != null) {
                            android.util.Log.d("RoomViewModel", "Initializing Guest room with code: $pendingRoomCode")
                            val guestRoom = com.together.app.model.Room(
                                roomId = pendingRoomCode!!, 
                                roomCode = pendingRoomCode!!,
                                roomName = pendingRoomName ?: "Together Room",
                                hostName = "Host",
                                videoUri = android.net.Uri.EMPTY,
                                videoTitle = "Loading...",
                                createdTime = System.currentTimeMillis(),
                                participants = participants,
                                maxParticipants = 10,
                                status = com.together.app.model.RoomStatus.WAITING
                            )
                            repository.setCurrentRoom(guestRoom)
                        } else {
                            android.util.Log.w("RoomViewModel", "Received participant list but pendingRoomCode is null")
                        }
                    } else {
                        android.util.Log.d("RoomViewModel", "Updating participants for existing room: ${current.roomCode}")
                        repository.updateParticipants(participants)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("RoomViewModel", "Networking error: Error parsing participant list", e)
                }
            }
            MessageType.JOIN_ROOM -> {
                android.util.Log.d("RoomViewModel", "JOIN_ROOM requested by ${message.sender} (Payload: ${message.payload})")
                val room = currentRoom.value ?: run {
                    android.util.Log.w("RoomViewModel", "JOIN_ROOM ignored: currentRoom is null")
                    return
                }
                
                // Host logic: update list and broadcast
                if (room.hostName == "Host" || room.participants.any { it.id == myId && it.isHost }) {
                    val currentParticipants = room.participants
                    val newParticipant = com.together.app.model.Participant(
                        id = message.sender,
                        name = message.payload ?: "Guest",
                        isHost = false,
                        isReady = false
                    )
                    
                    if (currentParticipants.none { it.id == newParticipant.id }) {
                        val updatedList = currentParticipants + newParticipant
                        android.util.Log.d("JOIN_TRACE", "Step 9: Participant added on Host: ${newParticipant.name}")
                        android.util.Log.d("RoomViewModel", "Host adding new participant: ${newParticipant.name}. New total: ${updatedList.size}")
                        repository.updateParticipants(updatedList)
                        broadcastParticipantList(updatedList)
                    } else {
                        android.util.Log.d("RoomViewModel", "Participant ${message.sender} already in list. Re-broadcasting list.")
                        // CRITICAL FIX: Always broadcast list so Guest can finish joining even if they missed previous broadcast
                        broadcastParticipantList(currentParticipants)
                    }
                }
            }
            MessageType.LEAVE_ROOM -> {
                android.util.Log.d("RoomViewModel", "LEAVE_ROOM from ${message.sender}")
                val room = currentRoom.value ?: return
                val currentParticipants = room.participants
                val updatedList = currentParticipants.filter { it.id != message.sender }
                if (updatedList.size != currentParticipants.size) {
                    android.util.Log.d("RoomViewModel", "Participant ${message.sender} left. New total: ${updatedList.size}")
                    repository.updateParticipants(updatedList)
                    broadcastParticipantList(updatedList)
                }
            }
            MessageType.USER_READY -> {
                android.util.Log.d("RoomViewModel", "USER_READY from ${message.sender}: ${message.payload}")
                val room = currentRoom.value ?: return
                val currentParticipants = room.participants
                val updatedList = currentParticipants.map {
                    if (it.id == message.sender) it.copy(isReady = message.payload == "true") else it
                }
                repository.updateParticipants(updatedList)
                broadcastParticipantList(updatedList)
            }
            MessageType.START_WATCHING -> {
                android.util.Log.d("RoomViewModel", "START_WATCHING received: ${message.payload}")
                val videoUri = message.payload ?: return
                viewModelScope.launch {
                    val encodedUri = java.net.URLEncoder.encode(videoUri, "UTF-8")
                    _navigationEvent.emit("player/$encodedUri")
                }
            }
            MessageType.STREAM_URL -> {
                val streamUrl = message.payload ?: return
                android.util.Log.d("RoomViewModel", "STREAM_URL received: $streamUrl")
                android.util.Log.d("STREAM_TRACE", "Guest received STREAM_URL=$streamUrl")
                
                // Instead of navigating immediately, we prepare
                isPreparing = true
                viewModelScope.launch {
                    val encodedUri = java.net.URLEncoder.encode(streamUrl, "UTF-8")
                    _navigationEvent.emit("player/$encodedUri")
                }
            }
            MessageType.PLAYER_READY -> {
                handlePlayerReady(message.sender)
            }
            MessageType.SYNC_START -> {
                handleSyncStart(message.payload)
            }
            MessageType.VIDEO_ENDED -> {
                android.util.Log.d("Sync", "VIDEO_ENDED received from ${message.sender}")
                repository.updateRoomStatus(com.together.app.model.RoomStatus.FINISHED)
                viewModelScope.launch {
                    _navigationEvent.emit("waiting_room/${currentRoom.value?.roomId}")
                }
            }
            MessageType.REQUEST_NEXT_VIDEO_SELECTION -> {
                android.util.Log.d("Sync", "REQUEST_NEXT_VIDEO_SELECTION received: ${message.payload}")
                val newTitle = message.payload ?: "New Video"
                
                // Update room on Guest
                currentRoom.value?.let { room ->
                    val updatedRoom = room.copy(
                        videoTitle = newTitle,
                        status = com.together.app.model.RoomStatus.WAITING
                    )
                    repository.setCurrentRoom(updatedRoom)
                    
                    // Reset ready states
                    val updatedParticipants = updatedRoom.participants.map { it.copy(isReady = it.isHost) }
                    repository.updateParticipants(updatedParticipants)
                }
            }
            else -> {
                android.util.Log.d("RoomViewModel", "Unhandled message type: ${message.type}")
            }
        }
    }

    private val _playbackCommand = MutableSharedFlow<PlaybackCommand>()
    val playbackCommand = _playbackCommand.asSharedFlow()

    sealed class PlaybackCommand {
        data class Start(val startTime: Long) : PlaybackCommand()
    }

    private fun handleSyncStart(payload: String?) {
        android.util.Log.d("Sync", "Processing SYNC_START. Payload: $payload")
        viewModelScope.launch {
            _playbackCommand.emit(PlaybackCommand.Start(payload?.toLong() ?: 0L))
        }
    }

    fun reportPlayerReady() {
        val myId = socketManager.getClientId() ?: "Host"
        android.util.Log.d("Sync", "Reporting PLAYER_READY from $myId")
        
        // If Host, we need to handle it locally because sendMessage only broadcasts to clients
        val room = currentRoom.value
        val isHost = room?.participants?.any { it.id == myId && it.isHost } == true
        if (isHost) {
            handlePlayerReady(myId)
        }
        
        sendMessage(Message(MessageType.PLAYER_READY, myId))
    }

    private fun handlePlayerReady(participantId: String) {
        val room = currentRoom.value ?: return
        val myId = socketManager.getClientId() ?: "Host"
        val isHost = room.participants.any { it.id == myId && it.isHost }
        
        if (isHost) {
            android.util.Log.d("Sync", "Participant $participantId is ready for playback")
            readyParticipants.add(participantId)
            
            // Wait for ALL participants (Host + Guests)
            val allParticipantIds = room.participants.map { it.id }
            if (readyParticipants.containsAll(allParticipantIds)) {
                android.util.Log.d("Sync", "All participants ready. Sending SYNC_START.")
                val startTime = System.currentTimeMillis().toString()
                
                // Handle locally for Host
                handleSyncStart(startTime)
                
                sendMessage(Message(MessageType.SYNC_START, "SERVER", startTime))
            } else {
                android.util.Log.d("Sync", "Still waiting for some participants. Ready: ${readyParticipants.size}/${allParticipantIds.size}")
            }
        }
    }

    fun onVideoEnded() {
        val room = currentRoom.value ?: return
        val myId = socketManager.getClientId() ?: "Host"
        val isHost = room.participants.any { it.id == myId && it.isHost }
        
        if (isHost) {
            repository.updateRoomStatus(com.together.app.model.RoomStatus.FINISHED)
            sendMessage(Message(MessageType.VIDEO_ENDED, "SERVER"))
            viewModelScope.launch {
                _navigationEvent.emit("waiting_room/${room.roomId}")
            }
        }
    }
    
    fun resetRoomForNextVideo(keepVideo: Boolean) {
        val room = currentRoom.value ?: return
        if (keepVideo) {
            // Just reset ready states to "Ready" for everyone to replay? 
            // User says "Guest does NOT need to manually press Ready again"
            // So we might just skip to startWatching again.
        } else {
            sendMessage(Message(MessageType.REQUEST_NEXT_VIDEO_SELECTION, "SERVER"))
            repository.updateRoomStatus(com.together.app.model.RoomStatus.WAITING)
            val updatedList = room.participants.map { it.copy(isReady = it.isHost) }
            repository.updateParticipants(updatedList)
        }
    }

    private fun broadcastParticipantList(participants: List<com.together.app.model.Participant>) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(participants)
        android.util.Log.d("JOIN_TRACE", "Step 10: JOIN_SUCCESS (PARTICIPANT_LIST_UPDATED) broadcasting from Host")
        sendMessage(Message(MessageType.PARTICIPANT_LIST_UPDATED, "SERVER", json))
    }

    fun toggleReady(isReady: Boolean) {
        val myId = socketManager.getClientId() ?: "local"
        sendMessage(Message(MessageType.USER_READY, myId, isReady.toString()))
    }

    fun startServer(port: Int) {
        android.util.Log.d("RoomViewModel", "startServer requested on port $port")
        pendingRoomCode = null // Clear guest-specific state
        socketManager.startServer(port)
    }

    fun startClient(host: String, port: Int, roomCode: String? = null, roomName: String? = null) {
        android.util.Log.d("RoomViewModel", "startClient requested for $host:$port (Room: $roomCode)")
        repository.setCurrentRoom(null) // CRITICAL: Clear old room state before joining a new one
        pendingRoomCode = roomCode
        pendingRoomName = roomName
        socketManager.startClient(host, port)
    }

    fun sendMessage(message: Message): Boolean {
        android.util.Log.d("RoomViewModel", "sendMessage: ${message.type}")
        return socketManager.sendMessage(message)
    }

    fun createRoom(room: Room) {
        android.util.Log.d("RoomViewModel", "createRoom: ${room.roomName} (${room.roomId})")
        repository.createRoom(room)
    }

    fun selectVideo(video: com.together.app.model.Video) {
        val room = currentRoom.value ?: return
        val updatedRoom = room.copy(
            videoUri = video.uri,
            videoTitle = video.title,
            status = com.together.app.model.RoomStatus.WAITING
        )
        repository.createRoom(updatedRoom) // This replaces the room in repo and broadcasts
        
        // Broadcast new video info to guests
        sendMessage(Message(MessageType.REQUEST_NEXT_VIDEO_SELECTION, "SERVER", video.title))
    }
    
    fun generateRoomCode(): String {
        val code = repository.generateRoomCode()
        android.util.Log.d("RoomViewModel", "Generated room code: $code")
        return code
    }

    fun startWatching(context: Context) {
        val room = currentRoom.value ?: return
        val videoUri = room.videoUri
        android.util.Log.d("RoomViewModel", "Host preparing playback and streaming: $videoUri")
        
        readyParticipants.clear()
        
        // Start Video Server on Host if not already running
        if (videoServer == null) {
            videoServer = VideoServer(context, 8889)
        }
        videoServer?.stop()
        videoServer?.start(videoUri)

        val localIp = NetworkUtils.getLocalIpAddress(context) ?: "127.0.0.1"
        val streamUrl = "http://$localIp:8889/video"
        android.util.Log.d("STREAM_TRACE", "Host generated STREAM_URL=$streamUrl")
        
        // Broadcast STREAM_URL to all guests to start their preparation
        sendMessage(Message(MessageType.STREAM_URL, "SERVER", streamUrl))
        
        // Host also prepares
        isPreparing = true
        viewModelScope.launch {
            val encodedUri = java.net.URLEncoder.encode(videoUri.toString(), "UTF-8")
            _navigationEvent.emit("player/$encodedUri")
        }
    }

    override fun onCleared() {
        android.util.Log.d("RoomViewModel", "onCleared - stopping all socket operations")
        super.onCleared()
        socketManager.stopAll()
        videoServer?.stop()
    }
}
