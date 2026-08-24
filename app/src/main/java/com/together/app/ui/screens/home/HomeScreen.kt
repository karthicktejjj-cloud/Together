package com.together.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.together.app.ui.components.TogetherCard
import com.together.app.ui.components.TogetherSectionHeader
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface

@Composable
fun HomeScreen(
    navController: NavHostController
) {
    Scaffold(
        containerColor = TogetherBackground,
        topBar = {
            HomeTopBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Section
            HeroSection(
                onWatchTogetherClick = {
                    navController.navigate("nearby_rooms")
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Actions
            Column(
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                TogetherSectionHeader(title = "Experience")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "My Library",
                        subtitle = "Local Videos",
                        icon = Icons.Default.Movie,
                        color = TogetherPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("videos") }
                    )
                    ActionCard(
                        title = "Join Room",
                        subtitle = "Nearby Friends",
                        icon = Icons.Default.Wifi,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("nearby_rooms") }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ActionCard(
                        title = "Favorites",
                        subtitle = "Saved Videos",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE91E63),
                        modifier = Modifier.weight(1f),
                        onClick = { /* Coming Soon */ }
                    )
                    ActionCard(
                        title = "Settings",
                        subtitle = "App Config",
                        icon = Icons.Default.Settings,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                        onClick = { /* Coming Soon */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            // Branding Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Together v1.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun HomeTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Together",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 1.sp
            )
            Text(
                text = "Watch. Experience. Connect.",
                style = MaterialTheme.typography.labelMedium,
                color = TogetherPrimary
            )
        }
        
        IconButton(
            onClick = { /* Profile */ },
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(TogetherSurface)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = Color.White
            )
        }
    }
}

@Composable
fun HeroSection(
    onWatchTogetherClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(TogetherSurface)
    ) {
        // Hero Background Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(TogetherPrimary.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = "Experience Cinema\nTogether.",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 40.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onWatchTogetherClick,
                colors = ButtonDefaults.buttonColors(containerColor = TogetherPrimary),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Watch Together", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TogetherCard(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
