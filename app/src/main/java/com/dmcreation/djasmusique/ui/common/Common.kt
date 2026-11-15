package com.dmcreation.djasmusique.ui.common

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.dmcreation.djasmusique.data.Track
import com.dmcreation.djasmusique.data.VideoItem
import androidx.compose.foundation.shape.RoundedCornerShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun formatTime(ms: Long): String {
    val total = (ms / 1000).coerceAtLeast(0)
    val h = total / 3600
    val m = (total % 3600) / 60
    val s = total % 60
    return if (h > 0) String.format("%d:%02d:%02d", h, m, s) else String.format("%d:%02d", m, s)
}

object ArtCache {
    private val cache = LruCache<Long, Bitmap>(60)
    private val videoCache = LruCache<Long, Bitmap>(40)

    fun key(t: Track): Long = if (t.albumId > 0) t.albumId else -t.id

    fun get(t: Track): Bitmap? = cache.get(key(t))

    fun load(context: Context, t: Track): Bitmap? {
        val k = key(t)
        cache.get(k)?.let { return it }
        val bmp: Bitmap? = try {
            if (Build.VERSION.SDK_INT >= 29) {
                context.contentResolver.loadThumbnail(t.uri, Size(400, 400), null)
            } else {
                val u = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), t.albumId)
                context.contentResolver.openInputStream(u)?.use { BitmapFactory.decodeStream(it) }
            }
        } catch (e: Exception) {
            null
        }
        if (bmp != null) cache.put(k, bmp)
        return bmp
    }

    fun getVideo(v: VideoItem): Bitmap? = videoCache.get(v.id)

    fun loadVideo(context: Context, v: VideoItem): Bitmap? {
        videoCache.get(v.id)?.let { return it }
        val bmp: Bitmap? = try {
            if (Build.VERSION.SDK_INT >= 29) {
                context.contentResolver.loadThumbnail(v.uri, Size(320, 180), null)
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Video.Thumbnails.getThumbnail(
                    context.contentResolver, v.id, MediaStore.Video.Thumbnails.MINI_KIND, null
                )
            }
        } catch (e: Exception) {
            null
        }
        if (bmp != null) videoCache.put(v.id, bmp)
        return bmp
    }
}

@Composable
fun AlbumArt(track: Track, modifier: Modifier = Modifier, corner: Int = 8) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = ArtCache.get(track), key1 = ArtCache.key(track)) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { ArtCache.load(context, track) }
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun VideoThumb(video: VideoItem, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = ArtCache.getVideo(video), key1 = video.id) {
        if (value == null) {
            value = withContext(Dispatchers.IO) { ArtCache.loadVideo(context, video) }
        }
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        val bmp = bitmap
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(Icons.Filled.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}
