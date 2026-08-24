package com.together.app.ui.screens.room

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.together.app.ui.components.RoomCard
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface
import com.together.app.viewmodel.DiscoveryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyRoomsScreen(
    navController: NavHostController,
    viewModel: DiscoveryViewModel = viewModel()
) {
    val rooms by viewModel.discoveredRooms.collectAsState()

    DisposableEffect(Unit) {
        viewModel.startDiscovery()
        onDispose {
            viewModel.stopDiscovery()
        }
    }

    Scaffold(
        containerColor = TogetherBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nearby Rooms", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        viewModel.stopDiscovery()
                        viewModel.startDiscovery()
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (rooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    ScanningAnimation()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 32.dp, top = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Found ${rooms.size} rooms near you",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    items(rooms) { room ->
                        RoomCard(
                            roomName = room.roomName,
                            hostName = room.hostName,
                            roomCode = room.roomCode,
                            onClick = {
                                navController.navigate("join_room/${room.roomCode}/${room.ipAddress}/${room.port}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScanningAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "scanning")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(TogetherPrimary.copy(alpha = alpha))
                    .align(Alignment.Center)
                    .scale(scale)
            )
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = TogetherSurface,
                border = androidx.compose.foundation.BorderStroke(2.dp, TogetherPrimary)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("📡", fontSize = 32.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Searching for rooms...",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Make sure your friends are on the same Wi-Fi",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

private fun Modifier.scale(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer(scaleX = scale, scaleY = scale)
)
