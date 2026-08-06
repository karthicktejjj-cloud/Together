package com.together.app.ui.screens.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.model.Room
import com.together.app.model.RoomStatus
import com.together.app.viewmodel.RoomViewModel
import com.together.app.viewmodel.VideoViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    navController: NavHostController,
    videoViewModel: VideoViewModel,
    roomViewModel: RoomViewModel,
    videoId: Long
) {
    val videos by videoViewModel.videos.collectAsState()
    val video = videos.find { it.id == videoId }

    var roomName by remember { mutableStateOf("") }
    var hostName by remember { mutableStateOf("") }
    var maxParticipants by remember { mutableFloatStateOf(5f) }
    val roomCode = remember { roomViewModel.generateRoomCode() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Room") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (video == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Video not found")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Video Info Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.Gray, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                            Text(
                                text = "Selected Video",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Form Fields
                OutlinedTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = { Text("Room Name") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. Movie Night with Friends") }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = hostName,
                    onValueChange = { hostName = it },
                    label = { Text("Your Name (Host)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g. John Doe") }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Room Code Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Room Code", style = MaterialTheme.typography.labelLarge)
                        Text(
                            text = roomCode,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Max Participants
                Text(
                    text = "Max Participants: ${maxParticipants.toInt()}",
                    style = MaterialTheme.typography.titleSmall
                )
                Slider(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    valueRange = 2f..10f,
                    steps = 7
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons
                Button(
                    onClick = {
                        val roomId = UUID.randomUUID().toString()
                        val hostParticipant = com.together.app.model.Participant(
                            id = "Host",
                            name = hostName.ifBlank { "Host" },
                            isHost = true,
                            isReady = true
                        )
                        val newRoom = Room(
                            roomId = roomId,
                            roomCode = roomCode,
                            roomName = roomName.ifBlank { "${hostName.ifBlank { "Host" }}'s Room" },
                            hostName = hostName.ifBlank { "Host" },
                            videoUri = video.uri,
                            videoTitle = video.title,
                            createdTime = System.currentTimeMillis(),
                            participants = listOf(hostParticipant),
                            maxParticipants = maxParticipants.toInt(),
                            status = RoomStatus.WAITING
                        )
                        roomViewModel.createRoom(newRoom)
                        navController.navigate("waiting_room/$roomId")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Start Room", fontSize = 18.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
