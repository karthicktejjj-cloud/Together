package com.together.app.ui.screens.videos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.ui.components.TogetherButton
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface
import com.together.app.viewmodel.VideoViewModel
import com.together.app.viewmodel.RoomViewModel
import java.net.URLEncoder
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoDetailsScreen(
    navController: NavHostController,
    videoViewModel: VideoViewModel,
    roomViewModel: RoomViewModel,
    videoId: Long
) {
    val videos by videoViewModel.videos.collectAsState()
    val video = videos.find { it.id == videoId }
    val currentRoom by roomViewModel.currentRoom.collectAsState()

    Scaffold(
        containerColor = TogetherBackground,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(TogetherSurface.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        if (video == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Video not found", color = Color.White)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image / Artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        TogetherPrimary.copy(alpha = 0.3f),
                                        TogetherBackground
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬", fontSize = 80.sp)
                    }
                    
                    // Gradient Overlay for Title
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, TogetherBackground),
                                    startY = 500f
                                )
                            )
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .offset(y = (-40).dp)
                ) {
                    Text(
                        text = video.title,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MetadataBadge(text = formatDuration(video.duration))
                        MetadataBadge(text = video.resolution ?: "HD")
                        MetadataBadge(text = formatSize(video.size))
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "This is a local video stored on your device. You can watch this together with your friends in perfect synchronization.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        lineHeight = 24.sp
                    )

                    Spacer(modifier = Modifier.height(40.dp))

                    if (currentRoom != null) {
                        TogetherButton(
                            text = "Set as Room Video",
                            onClick = {
                                roomViewModel.selectVideo(video)
                                navController.navigate("waiting_room/${currentRoom?.roomId}") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    } else {
                        TogetherButton(
                            text = "Watch Together",
                            onClick = {
                                navController.navigate("create_room/${video.id}")
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TogetherButton(
                        text = "Play Solo",
                        isPrimary = false,
                        onClick = {
                            val encodedUri = URLEncoder.encode(video.uri.toString(), "UTF-8")
                            navController.navigate("player/$encodedUri")
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Technical Details
                    TechnicalDetails(video.path ?: "Local Storage")
                    
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun MetadataBadge(text: String) {
    Surface(
        color = TogetherSurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = Color.LightGray
        )
    }
}

@Composable
fun TechnicalDetails(path: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TogetherSurface)
            .padding(16.dp)
    ) {
        Text(
            text = "Technical Details",
            style = MaterialTheme.typography.labelLarge,
            color = TogetherPrimary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = path,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatDuration(duration: Long): String {
    val minutes = duration / 1000 / 60
    return "$minutes min"
}

fun formatSize(size: Long): String {
    val mb = size / 1024.0 / 1024.0
    return String.format(Locale.US, "%.1f MB", mb)
}
