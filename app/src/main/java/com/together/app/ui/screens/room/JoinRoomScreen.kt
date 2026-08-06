package com.together.app.ui.screens.room

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.together.app.viewmodel.RoomViewModel
import com.together.app.network.socket.Message
import com.together.app.network.socket.MessageType
import com.together.app.network.socket.SocketManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinRoomScreen(
    navController: NavHostController,
    roomViewModel: RoomViewModel,
    roomCode: String,
    ipAddress: String,
    port: Int
) {
    var name by remember { mutableStateOf("") }
    var isJoining by remember { mutableStateOf(false) }
    val currentRoom by roomViewModel.currentRoom.collectAsState()

    LaunchedEffect(isJoining) {
        if (isJoining) {
            android.util.Log.d("JoinRoomScreen", "Join button clicked. Starting client connection to $ipAddress:$port")
            roomViewModel.startClient(ipAddress, port, roomCode = roomCode)
            // Wait a bit for connection
            kotlinx.coroutines.delay(1000)
            val clientId = SocketManager.getInstance().getClientId() ?: "Guest"
            android.util.Log.d("JOIN_TRACE", "Step 3: JOIN_ROOM message created for $name ($clientId)")
            android.util.Log.d("JoinRoomScreen", "Sending JOIN_ROOM message for $name ($clientId)")
            roomViewModel.sendMessage(Message(MessageType.JOIN_ROOM, clientId, name))
            android.util.Log.d("JOIN_TRACE", "Step 5: Waiting for JOIN_SUCCESS (PARTICIPANT_LIST_UPDATED)")
        }
    }

    // Observe currentRoom to navigate when joined
    LaunchedEffect(currentRoom) {
        if (currentRoom != null && isJoining) {
            android.util.Log.d("JOIN_TRACE", "Step 12: Navigate to WaitingRoomScreen (Room ID: ${currentRoom?.roomId})")
            android.util.Log.d("JoinRoomScreen", "Room joined successfully (Room ID: ${currentRoom?.roomId}). Navigating to WaitingRoomScreen.")
            navController.navigate("waiting_room/${currentRoom?.roomId}") {
                popUpTo("home") { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Room") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Join Room: $roomCode",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                if (!isJoining) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Your Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = { isJoining = true },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Join Now")
                    }
                } else {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Connecting to $ipAddress...")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
            }
        }
    }
}
