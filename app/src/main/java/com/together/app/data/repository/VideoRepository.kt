package com.together.app.data.repository

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.together.app.model.Video

class VideoRepository {

    fun getVideos(context: Context): List<Video> {

        val videos = mutableListOf<Video>()

        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE
        )

        val sortOrder = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->

            val idColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)

            val titleColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            val durationColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)

            val sizeColumn =
                cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)

                val title = cursor.getString(titleColumn)

                val duration = cursor.getLong(durationColumn)

                val size = cursor.getLong(sizeColumn)

                val uri = ContentUris.withAppendedId(
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                videos.add(
                    Video(
                        id = id,
                        title = title,
                        uri = uri,
                        duration = duration,
                        size = size
                    )
                )
            }
        }

        return videos
    }
}