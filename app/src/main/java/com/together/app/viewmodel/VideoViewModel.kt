package com.together.app.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.together.app.data.repository.VideoRepository
import com.together.app.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class VideoViewModel : ViewModel() {

    private val repository = VideoRepository()

    private val _videos = MutableStateFlow<List<Video>>(emptyList())
    val videos: StateFlow<List<Video>> = _videos

    fun loadVideos(context: Context) {
        _videos.value = repository.getVideos(context)
    }
}