package com.together.app.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.together.app.ui.theme.TogetherPrimary
import com.together.app.ui.theme.TogetherSurface
import com.together.app.ui.theme.TogetherSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VideoThumbnail(
    uri: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val thumbnailState = produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.loadThumbnail(uri, Size(320, 480), null)
            } catch (e: Exception) {
                null
            }
        }
    }

    if (thumbnailState.value != null) {
        Image(
            bitmap = thumbnailState.value!!.asImageBitmap(),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(TogetherSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("🎬", fontSize = 40.sp)
        }
    }
}

@Composable
fun TogetherButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    val containerColor = if (isPrimary) TogetherPrimary else TogetherSurfaceVariant
    val contentColor = if (isPrimary) Color.White else Color.LightGray

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = TogetherSurfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = Color.Gray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TogetherCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = cardModifier
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp)),
        color = TogetherSurface.copy(alpha = 0.8f),
        content = {
            Column(
                modifier = Modifier.padding(16.dp),
                content = content
            )
        }
    )
}

@Composable
fun TogetherSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        if (actionText != null && onActionClick != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = TogetherPrimary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun MovieCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    thumbnailUri: Uri? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Gray.copy(alpha = 0.2f), TogetherSurfaceVariant)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnailUri != null) {
                VideoThumbnail(uri = thumbnailUri, modifier = Modifier.fillMaxSize())
            } else {
                Text("🎬", fontSize = 40.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = Color.White
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            maxLines = 1
        )
    }
}

@Composable
fun ParticipantCard(
    name: String,
    status: String,
    isReady: Boolean,
    isHost: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp)),
        color = if (isReady) TogetherPrimary.copy(alpha = 0.1f) else TogetherSurface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isReady) TogetherPrimary else TogetherSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = if (isHost) "Room Host" else status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReady) TogetherPrimary else Color.Gray
                )
            }
            
            if (isReady) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
                    contentDescription = "Ready",
                    tint = TogetherPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun RoomCard(
    roomName: String,
    hostName: String,
    roomCode: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TogetherCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = roomName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Hosted by $hostName",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Surface(
                color = TogetherPrimary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp),
                border = border(1.dp, TogetherPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = roomCode,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TogetherPrimary,
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

private fun border(width: androidx.compose.ui.unit.Dp, color: Color, shape: androidx.compose.ui.graphics.Shape): androidx.compose.foundation.BorderStroke {
    return androidx.compose.foundation.BorderStroke(width, color)
}
