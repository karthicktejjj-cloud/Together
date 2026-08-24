package com.together.app.ui.screens.splash

import android.app.Activity
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import com.together.app.ui.theme.TogetherBackground
import com.together.app.ui.theme.TogetherPrimary
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun SplashScreen(navController: NavHostController) {
    val context = LocalContext.current
    val activity = context as? Activity
    
    // Animation States
    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.9f) }
    val textAlpha = remember { Animatable(0f) }
    val taglineAlpha = remember { Animatable(0f) }
    val decorativeAlpha = remember { Animatable(0f) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowScale"
    )

    // Immersive Mode Management
    LaunchedEffect(Unit) {
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // Start Animations
        logoAlpha.animateTo(1f, tween(1000, easing = EaseOutExpo))
        logoScale.animateTo(1f, tween(1000, easing = EaseOutBack))
        
        delay(300)
        textAlpha.animateTo(1f, tween(800))
        
        delay(200)
        taglineAlpha.animateTo(1f, tween(800))
        
        delay(200)
        decorativeAlpha.animateTo(1f, tween(800))
        
        delay(1000)
        
        // Restore Navigation Bar but keep Status Bar hidden
        activity?.window?.let { window ->
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
        
        navController.navigate("welcome") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TogetherBackground),
        contentAlignment = Alignment.Center
    ) {
        // Bottom Wave Animation
        ParticleWaveEffect(modifier = Modifier.align(Alignment.BottomCenter))

        // Center Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 60.dp)
        ) {
            // Glow behind logo
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(glowScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(TogetherPrimary.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
                
                TogetherLogo(
                    modifier = Modifier
                        .size(100.dp)
                        .scale(logoScale.value)
                        .alpha(logoAlpha.value)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // App Name "Together" with Heart
            TogetherAppName(modifier = Modifier.alpha(textAlpha.value))

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline
            Tagline(modifier = Modifier.alpha(taglineAlpha.value))

            Spacer(modifier = Modifier.height(32.dp))

            // Decorative Element
            TogetherDecorativeElement(modifier = Modifier.alpha(decorativeAlpha.value))
        }
    }
}

@Composable
fun Tagline(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val style = MaterialTheme.typography.labelLarge.copy(
            color = Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        
        Text(text = "WATCH", style = style)
        Text(text = " \u2022 ", style = style, color = TogetherPrimary)
        Text(text = "EXPERIENCE", style = style)
        Text(text = " \u2022 ", style = style, color = TogetherPrimary)
        Text(text = "CONNECT", style = style)
    }
}

@Composable
fun TogetherLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val primaryBrush = Brush.verticalGradient(
            colors = listOf(TogetherPrimary, TogetherPrimary.copy(alpha = 0.7f))
        )

        // Draw the two people/heart shape
        val path = Path().apply {
            // Left curve
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(w * 0.1f, h * 0.75f, w * 0.1f, h * 0.35f, w * 0.45f, h * 0.35f)
            
            // Right curve
            moveTo(w * 0.5f, h * 0.85f)
            cubicTo(w * 0.9f, h * 0.75f, w * 0.9f, h * 0.35f, w * 0.55f, h * 0.35f)
        }
        
        drawPath(
            path = path,
            brush = primaryBrush,
            style = Stroke(width = w * 0.08f, cap = StrokeCap.Round)
        )

        // Draw heads (circles)
        drawCircle(
            brush = primaryBrush,
            radius = w * 0.07f,
            center = Offset(w * 0.4f, h * 0.28f)
        )
        drawCircle(
            brush = primaryBrush,
            radius = w * 0.07f,
            center = Offset(w * 0.6f, h * 0.28f)
        )

        // Play Button (Triangle)
        val trianglePath = Path().apply {
            moveTo(w * 0.47f, h * 0.48f)
            lineTo(w * 0.57f, h * 0.55f)
            lineTo(w * 0.47f, h * 0.62f)
            close()
        }
        drawPath(trianglePath, primaryBrush)
    }
}

@Composable
fun TogetherAppName(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "T",
            style = MaterialTheme.typography.displayMedium,
            color = TogetherPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
        
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = TogetherPrimary,
            modifier = Modifier.size(32.dp).padding(top = 4.dp)
        )
        
        Text(
            text = "gether",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun TogetherDecorativeElement(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.width(200.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(Color.Transparent, TogetherPrimary.copy(alpha = 0.5f)))
        ))
        
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = TogetherPrimary,
            modifier = Modifier.size(12.dp).padding(horizontal = 4.dp)
        )
        
        Box(modifier = Modifier.weight(1f).height(1.dp).background(
            Brush.horizontalGradient(listOf(TogetherPrimary.copy(alpha = 0.5f), Color.Transparent))
        ))
    }
}

@Composable
fun ParticleWaveEffect(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Canvas(modifier = modifier.fillMaxWidth().height(180.dp)) {
        val w = size.width
        val h = size.height
        
        // Draw multiple layers of subtle digital waves
        for (layer in 0..2) {
            val layerAlpha = 0.1f / (layer + 1)
            val speed = 1f + layer * 0.5f
            val amplitude = 10f + layer * 5f
            
            for (i in 0 until 120 step 3) {
                val x = (i / 120f) * w
                val y = h * (0.6f + layer * 0.1f) + sin(phase * speed + i * 0.1f) * amplitude
                
                drawCircle(
                    color = TogetherPrimary.copy(alpha = layerAlpha),
                    radius = (1.5f - layer * 0.3f).dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
