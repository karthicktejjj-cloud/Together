package com.together.app.videos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.together.app.ui.components.MovieCard
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherSurface
import com.together.app.viewmodel.VideoViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    navController: NavHostController,
    viewModel: VideoViewModel = viewModel()
) {
    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVideos(context)
    }

    Scaffold(
        containerColor = TogetherBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "My Library",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("🎬", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No videos found",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(videos) { video ->
                    MovieCard(
                        title = video.title,
                        subtitle = formatDuration(video.duration),
                        thumbnailUri = video.uri,
                        onClick = {
                            navController.navigate("details/${video.id}")
                        }
                    )
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
