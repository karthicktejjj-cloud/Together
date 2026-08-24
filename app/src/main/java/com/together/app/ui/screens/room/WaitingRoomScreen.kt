package com.together.app.ui.screens.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.viewmodel.RoomViewModel
import com.together.app.network.discovery.NsdAdvertiser
import com.together.app.network.socket.SocketManager
import com.together.app.ui.components.ParticipantCard
import com.together.app.ui.components.TogetherButton
import com.together.app.ui.components.TogetherCard
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface

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

    LaunchedEffect(Unit) {
        roomViewModel.navigationEvent.collect { route ->
            navController.navigate(route)
        }
    }

    DisposableEffect(roomId) {
        val myId = SocketManager.getInstance().getClientId()
        val currentRoom = room
        if (currentRoom != null && currentRoom.roomId == roomId) {
            val isHost = currentRoom.participants.any { it.id == myId && it.isHost }
            
            if (isHost) {
                roomViewModel.startServer(8888)
                nsdAdvertiser.registerService(
                    port = 8888, 
                    roomCode = currentRoom.roomCode,
                    hostName = currentRoom.hostName,
                    roomName = currentRoom.roomName
                )
            }
        }
        onDispose {
            nsdAdvertiser.unregisterService()
        }
    }

    Scaffold(
        containerColor = TogetherBackground,
        topBar = {
            TopAppBar(
                title = { Text("Virtual Lobby", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (room == null || room?.roomId != roomId) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Room not found", color = Color.White)
            }
        } else {
            val currentRoom = room!!
            val myId = SocketManager.getInstance().getClientId()
            val isHost = currentRoom.participants.any { it.id == myId && it.isHost }
            val myParticipant = currentRoom.participants.find { it.id == myId }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Room Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentRoom.roomName,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Watching: ${currentRoom.videoTitle}",
                        style = MaterialTheme.typography.titleSmall,
                        color = TogetherPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Code Display
                    TogetherCard(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Invite Friends with Code", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(
                                text = currentRoom.roomCode,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 8.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Participants List
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Participants (${currentRoom.participants.size}/${currentRoom.maxParticipants})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(currentRoom.participants) { participant ->
                            val isMe = participant.id == myId
                            ParticipantCard(
                                name = if (isMe) "${participant.name} (You)" else participant.name,
                                status = if (participant.isReady) "Ready to watch" else "Choosing snacks...",
                                isReady = participant.isReady,
                                isHost = participant.isHost
                            )
                        }
                    }
                }

                // Bottom Action Area
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = TogetherSurface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isHost) {
                            if (currentRoom.status == com.together.app.model.RoomStatus.FINISHED) {
                                TogetherButton(
                                    text = "Play Same Video Again",
                                    onClick = { roomViewModel.startWatching(context) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TogetherButton(
                                    text = "Choose Another Video",
                                    isPrimary = false,
                                    onClick = { navController.navigate("home") } // Navigate to home to pick video
                                )
                            } else {
                                val allReady = currentRoom.participants.filter { !it.isHost }.all { it.isReady } && currentRoom.participants.size > 1
                                
                                TogetherButton(
                                    text = if (currentRoom.participants.size <= 1) "Waiting for friends..." else if (!allReady) "Waiting for ready..." else "Start Movie Night",
                                    enabled = allReady,
                                    onClick = { roomViewModel.startWatching(context) }
                                )
                            }
                        } else {
                            if (currentRoom.status == com.together.app.model.RoomStatus.FINISHED) {
                                Text(
                                    text = "Waiting for Host to decide...",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            } else {
                                TogetherButton(
                                    text = if (myParticipant?.isReady == true) "I'm Not Ready" else "I'm Ready!",
                                    isPrimary = myParticipant?.isReady != true,
                                    onClick = { myParticipant?.let { roomViewModel.toggleReady(!it.isReady) } }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = if (isHost) "You will start the video for everyone" else "Waiting for the host to start",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
