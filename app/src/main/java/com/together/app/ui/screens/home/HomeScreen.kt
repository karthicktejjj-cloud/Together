package com.together.app.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController

@Composable
fun HomeScreen(
    navController: NavHostController
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "🎬 Together",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Watch movies with your friends"
        )

        Spacer(modifier = Modifier.height(24.dp))

        FeatureCard(
            title = "Local Videos",
            icon = Icons.Default.Movie,
            onClick = {
                navController.navigate("videos")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "Watch Together",
            icon = Icons.Default.Wifi,
            onClick = {
                navController.navigate("nearby_rooms")
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "Favorites",
            icon = Icons.Default.Favorite,
            onClick = {
                // Coming soon
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        FeatureCard(
            title = "Settings",
            icon = Icons.Default.Settings,
            onClick = {
                // Coming soon
            }
        )
    }
}

@Composable
fun FeatureCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                imageVector = icon,
                contentDescription = title
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                fontSize = 20.sp
            )
        }
    }
}