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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.model.Room
import com.together.app.model.RoomStatus
import com.together.app.ui.components.TogetherButton
import com.together.app.ui.components.TogetherCard
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface
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
        containerColor = TogetherBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Create Room", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (video == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Video not found", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Video Summary Card
                TogetherCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(TogetherPrimary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(video.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Selected Movie", style = MaterialTheme.typography.bodySmall, color = TogetherPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Input Fields
                Text("Room Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                
                Spacer(modifier = Modifier.height(16.dp))

                TogetherTextField(
                    value = roomName,
                    onValueChange = { roomName = it },
                    label = "Room Name",
                    placeholder = "e.g. Friday Movie Night"
                )

                Spacer(modifier = Modifier.height(16.dp))

                TogetherTextField(
                    value = hostName,
                    onValueChange = { hostName = it },
                    label = "Your Name",
                    placeholder = "Host Name"
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Room Code Display
                TogetherCard(modifier = Modifier.fillMaxWidth()) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("Room Code", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                        Text(
                            text = roomCode,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = TogetherPrimary,
                            letterSpacing = 4.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Participants Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Max Participants", style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text("${maxParticipants.toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TogetherPrimary)
                }
                
                Slider(
                    value = maxParticipants,
                    onValueChange = { maxParticipants = it },
                    valueRange = 2f..10f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = TogetherPrimary,
                        activeTrackColor = TogetherPrimary,
                        inactiveTrackColor = TogetherSurface
                    )
                )

                Spacer(modifier = Modifier.height(48.dp))

                TogetherButton(
                    text = "Create Room",
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
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun TogetherTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Color.Gray, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text(placeholder, color = Color.DarkGray) },
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = TogetherSurface,
                unfocusedContainerColor = TogetherSurface,
                disabledContainerColor = TogetherSurface,
                cursorColor = TogetherPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            )
        )
    }
}
