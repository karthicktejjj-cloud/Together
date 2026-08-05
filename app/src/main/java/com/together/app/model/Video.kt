package com.together.app.model

import android.net.Uri

data class Video(
    val id: Long,
    val title: String,
    val uri: Uri,
    val duration: Long,
    val size: Long
)