package com.together.app.viewmodel

import android.app.Application
import android.net.nsd.NsdServiceInfo
import androidx.lifecycle.AndroidViewModel
import com.together.app.model.DiscoveredRoom
import com.together.app.network.discovery.NsdDiscovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiscoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val _discoveredRooms = MutableStateFlow<List<DiscoveredRoom>>(emptyList())
    val discoveredRooms: StateFlow<List<DiscoveredRoom>> = _discoveredRooms.asStateFlow()

    private val nsdDiscovery = NsdDiscovery(
        context = application,
        onRoomFound = { serviceInfo ->
            val room = parseServiceInfo(serviceInfo)
            if (room != null) {
                _discoveredRooms.update { rooms ->
                    if (rooms.none { it.roomId == room.roomId }) {
                        rooms + room
                    } else {
                        rooms
                    }
                }
            }
        },
        onRoomLost = { serviceInfo ->
            _discoveredRooms.update { rooms ->
                rooms.filter { it.roomId != serviceInfo.serviceName }
            }
        }
    )

    fun startDiscovery() {
        _discoveredRooms.value = emptyList()
        nsdDiscovery.startDiscovery()
    }

    fun stopDiscovery() {
        nsdDiscovery.stopDiscovery()
    }

    private fun parseServiceInfo(info: NsdServiceInfo): DiscoveredRoom? {
        return try {
            val roomCode = info.attributes["roomCode"]?.decodeToString() ?: ""
            val hostName = info.attributes["hostName"]?.decodeToString() ?: "Unknown"
            val roomName = info.attributes["roomName"]?.decodeToString() ?: "Watch Party"
            val deviceName = info.attributes["deviceName"]?.decodeToString() ?: "Android Device"
            
            DiscoveredRoom(
                roomId = info.serviceName,
                roomCode = roomCode,
                roomName = roomName,
                hostName = hostName,
                deviceName = deviceName,
                ipAddress = info.host?.hostAddress ?: "",
                port = info.port
            )
        } catch (e: Exception) {
            null
        }
    }

    override fun onCleared() {
        super.onCleared()
        nsdDiscovery.stopDiscovery()
    }
}
