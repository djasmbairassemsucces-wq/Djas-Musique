package com.dmcreation.djasmusique.ui.video

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.dmcreation.djasmusique.data.AppPrefs
import com.dmcreation.djasmusique.data.Library
import com.dmcreation.djasmusique.data.VideoItem
import com.dmcreation.djasmusique.playback.PlayerController
import com.dmcreation.djasmusique.ui.common.VideoThumb
import com.dmcreation.djasmusique.ui.common.formatTime

@Composable
fun VideoScreen(prefs: AppPrefs, onPlay: (VideoItem) -> Unit, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val videos = Library.videos
    val filtered = remember(videos, query) {
        if (query.isBlank()) videos else videos.filter { it.title.contains(query.trim(), ignoreCase = true) }
    }
    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Rechercher une vidéo") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { query = "" }) {
                        Icon(Icons.Filled.Close, contentDescription = "Effacer")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(28.dp)
        )
        if (!Library.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (filtered.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Aucune vidéo trouvée sur ce téléphone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                items(filtered, key = { it.id }) { v ->
                    val saved = prefs.videoPosition(v.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlay(v) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VideoThumb(v, Modifier.size(width = 112.dp, height = 64.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                v.title,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                formatTime(v.durationMs) +
                                    if (saved > 5000) " · reprise à ${formatTime(saved)}" else "",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerScreen(video: VideoItem, prefs: AppPrefs, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val activity = ctx as? Activity
    var error by remember { mutableStateOf<String?>(null) }
    var landscape by remember { mutableStateOf(false) }

    val exo = remember {
        ExoPlayer.Builder(ctx).build().apply {
            setMediaItem(MediaItem.fromUri(video.uri))
            prepare()
            val saved = prefs.videoPosition(video.id)
            if (saved > 0) seekTo(saved)
            playWhenReady = true
        }
    }

    BackHandler(onBack = onClose)

    DisposableEffect(exo) {
        PlayerController.pause()
        val listener = object : Player.Listener {
            override fun onPlayerError(e: PlaybackException) {
                error = "Impossible de lire cette vidéo (format non compatible ou fichier abîmé)."
            }
        }
        exo.addListener(listener)
        val ctrl = activity?.let { WindowCompat.getInsetsController(it.window, it.window.decorView) }
        ctrl?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ctrl?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            val dur = exo.duration
            val pos = exo.currentPosition
            prefs.setVideoPosition(video.id, if (dur > 0 && pos >= dur - 3000) 0L else pos)
            exo.removeListener(listener)
            exo.release()
            ctrl?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { c ->
                PlayerView(c).apply {
                    this.player = exo
                    useController = true
                    setBackgroundColor(android.graphics.Color.BLACK)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        Row(
            Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Fermer", tint = Color.White)
            }
            IconButton(onClick = {
                landscape = !landscape
                activity?.requestedOrientation = if (landscape)
                    ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }) {
                Icon(Icons.Filled.ScreenRotation, contentDescription = "Plein écran / rotation", tint = Color.White)
            }
        }
        error?.let {
            Text(
                it,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(32.dp)
            )
        }
    }
}
