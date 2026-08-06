package com.together.app.ui.screens.room

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.viewmodel.RoomViewModel
import com.together.app.network.discovery.NsdAdvertiser
import com.together.app.network.socket.SocketManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaitingRoomScreen(
    navController: NavHostController,
    roomViewModel: RoomViewModel,
    roomId: String
) {
    val room by roomViewModel.currentRoom.collectAsState()
    val context = LocalContext.current
    val nsdAdvertiser = remember { NsdAdvertiser(context) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        roomViewModel.navigationEvent.collect { route ->
            android.util.Log.d("WaitingRoomScreen", "Navigation event received: $route")
            navController.navigate(route)
        }
    }

    DisposableEffect(roomId) {
        val myId = SocketManager.getInstance().getClientId()
        val currentRoom = room
        if (currentRoom != null && currentRoom.roomId == roomId) {
            val isHost = currentRoom.participants.any { it.id == myId && it.isHost }
            
            if (isHost) {
                android.util.Log.d("WaitingRoomScreen", "Device identified as HOST. Starting server.")
                // Start socket server ONLY if host
                roomViewModel.startServer(8888)

                nsdAdvertiser.registerService(
                    port = 8888, 
                    roomCode = currentRoom.roomCode,
                    hostName = currentRoom.hostName,
                    roomName = currentRoom.roomName
                )
            } else {
                android.util.Log.d("WaitingRoomScreen", "Device identified as GUEST ($myId).")
            }
        }
        onDispose {
            nsdAdvertiser.unregisterService()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Waiting Room") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (room == null || room?.roomId != roomId) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Room not found")
            }
        } else {
            val currentRoom = room!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentRoom.roomName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Watching: ${currentRoom.videoTitle}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Room Code", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = currentRoom.roomCode,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 8.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Participants (${currentRoom.participants.size}/${currentRoom.maxParticipants})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(currentRoom.participants) { participant ->
                        val myId = SocketManager.getInstance().getClientId()
                        val isMe = participant.id == myId
                        
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = if (isMe) "${participant.name} (You)" else participant.name,
                                    fontWeight = if (isMe) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Person, 
                                    contentDescription = null,
                                    tint = if (participant.isReady) Color(0xFF4CAF50) else Color.Gray
                                )
                            },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (participant.isHost) {
                                        SuggestionChip(
                                            onClick = { },
                                            label = { Text("Host") }
                                        )
                                    } else {
                                        Text(
                                            text = if (participant.isReady) "Ready" else "Not Ready",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (participant.isReady) Color(0xFF4CAF50) else Color.Gray
                                        )
                                    }
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val myId = SocketManager.getInstance().getClientId()
                val isHost = currentRoom.participants.any { it.id == myId && it.isHost }
                val allReady = currentRoom.participants.filter { !it.isHost }.all { it.isReady } && currentRoom.participants.size > 1
                val myParticipant = currentRoom.participants.find { it.id == myId }

                if (isHost) {
                    Button(
                        onClick = { 
                            android.util.Log.d("WaitingRoomScreen", "Start Watching clicked by host")
                            roomViewModel.startWatching(context) 
                        },
                        enabled = allReady,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (currentRoom.participants.size <= 1) "Waiting for Guests..." else if (!allReady) "Waiting for Guests to be Ready" else "Start Watching",
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Button(
                        onClick = { 
                            myParticipant?.let { roomViewModel.toggleReady(!it.isReady) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = if (myParticipant?.isReady == true) ButtonDefaults.buttonColors(containerColor = Color.Gray) else ButtonDefaults.buttonColors()
                    ) {
                        Text(
                            text = if (myParticipant?.isReady == true) "I'm Not Ready" else "I'm Ready!",
                            fontSize = 18.sp
                        )
                    }
                }
                
                Text(
                    text = "Waiting for other participants...",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
