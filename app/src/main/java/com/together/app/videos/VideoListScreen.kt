package com.together.app.videos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.together.app.viewmodel.VideoViewModel
import java.net.URLEncoder

@Composable
fun VideoListScreen(
    navController: NavHostController,
    viewModel: VideoViewModel = viewModel()
){

    val context = LocalContext.current
    val videos by viewModel.videos.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadVideos(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Local Videos",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {

            items(videos) { video ->

                Card(
                    onClick = {
                        val encodedUri = URLEncoder.encode(
                            video.uri.toString(),
                            "UTF-8"
                        )

                        navController.navigate("player/$encodedUri")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    run {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(video.title)

                            Spacer(modifier = Modifier.height(4.dp))

                            Text("${video.duration / 1000} sec")

                        }

                    }

                }

            }

        }
    }
}
