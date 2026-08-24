package com.together.app.ui.screens.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.viewmodel.RoomViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayerScreen(
    videoUri: String,
    roomViewModel: RoomViewModel
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val isVideoValid = videoUri.isNotBlank() && videoUri != "null"
    var playbackError by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isWaitingForSync by remember { mutableStateOf(true) }

    // Hide system bars for immersive experience
    SideEffect {
        activity?.window?.let { window ->
            val windowInsetsController =
                WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.statusBars())
        }
    }

    val exoPlayer = remember {
        if (isVideoValid) {
            ExoPlayer.Builder(context)
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(videoUri))
                    addListener(object : Player.Listener {
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            android.util.Log.e("VideoPlayerScreen", "Playback error", error)
                            playbackError = true
                            isLoading = false
                        }
                        override fun onPlaybackStateChanged(state: Int) {
                            android.util.Log.d("Sync", "Playback state changed: $state")
                            isLoading = state == Player.STATE_BUFFERING || state == Player.STATE_IDLE
                            
                            if (state == Player.STATE_READY && isWaitingForSync) {
                                android.util.Log.d("Sync", "Player READY. Reporting to ViewModel.")
                                roomViewModel.reportPlayerReady()
                            }
                            
                            if (state == Player.STATE_ENDED) {
                                android.util.Log.d("Sync", "Video ended. Reporting to ViewModel.")
                                roomViewModel.onVideoEnded()
                            }
                        }
                    })
                    prepare()
                    playWhenReady = false // DO NOT PLAY UNTIL SYNC
                }
        } else null
    }

    LaunchedEffect(exoPlayer) {
        roomViewModel.playbackCommand.collectLatest { command ->
            when (command) {
                is RoomViewModel.PlaybackCommand.Start -> {
                    android.util.Log.d("Sync", "Start command received. Commencing playback.")
                    isWaitingForSync = false
                    exoPlayer?.playWhenReady = true
                }
            }
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer?.release()
            // Restore navigation bar but keep status bar hidden globally
            activity?.window?.let { window ->
                val windowInsetsController =
                    WindowCompat.getInsetsController(window, window.decorView)
                windowInsetsController.show(WindowInsetsCompat.Type.navigationBars())
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isVideoValid && exoPlayer != null && !playbackError) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(it).apply {
                        player = exoPlayer
                        useController = true
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                    }
                }
            )
            
            if (isLoading || isWaitingForSync) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = TogetherPrimary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isWaitingForSync) "Waiting for participants..." else "Loading video...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else if (playbackError) {
            PlayerErrorMessage("Connection Lost", "Unable to stream from host. Make sure you are on the same Wi-Fi.")
        } else {
            PlayerErrorMessage("Syncing Video...", "Waiting for the host to start the experience.")
        }
    }
}

@Composable
fun PlayerErrorMessage(title: String, message: String) {
    Column(
        modifier = Modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = TogetherPrimary, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
