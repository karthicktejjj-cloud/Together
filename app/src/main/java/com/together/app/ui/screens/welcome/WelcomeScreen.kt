package com.together.app.ui.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.ui.components.TogetherButton
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary

@Composable
fun WelcomeScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TogetherBackground)
    ) {
        // Subtle gradient background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TogetherPrimary.copy(alpha = 0.1f),
                            Color.Transparent,
                            TogetherBackground
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOGETHER",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Watch anything.\nExperience it together.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Connect with friends and family to watch your favorite local videos in perfect sync.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            TogetherButton(
                text = "Get Started",
                onClick = {
                    navController.navigate("home")
                }
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
