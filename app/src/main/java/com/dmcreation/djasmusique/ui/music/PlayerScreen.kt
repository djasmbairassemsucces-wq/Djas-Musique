package com.dmcreation.djasmusique.ui.music

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import com.dmcreation.djasmusique.playback.PlayerController
import com.dmcreation.djasmusique.ui.common.AlbumArt
import com.dmcreation.djasmusique.ui.common.formatTime
import kotlinx.coroutines.delay

/** Mini-lecteur permanent en bas de l'écran. */
@Composable
fun MiniPlayer(onClick: () -> Unit) {
    val track = PlayerController.currentTrack
    Surface(
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (track != null) {
                AlbumArt(track, Modifier.size(46.dp))
            } else {
                Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(46.dp))
            }
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    PlayerController.nowTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    PlayerController.nowArtist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { PlayerController.previous() }) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Précédent")
            }
            IconButton(onClick = { PlayerController.togglePlay() }) {
                Icon(
                    if (PlayerController.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Lecture / pause"
                )
            }
            IconButton(onClick = { PlayerController.next() }) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Suivant")
            }
        }
    }
}

/** Lecteur musical complet. */
@Composable
fun PlayerScreen(
    favorites: Set<Long>,
    onToggleFav: (Long) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)
    val track = PlayerController.currentTrack
    var showTimer by remember { mutableStateOf(false) }
    var position by remember { mutableLongStateOf(0L) }
    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            if (!dragging) position = PlayerController.currentPosition()
            delay(500)
        }
    }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Fermer")
                }
                Text("Lecture en cours", fontWeight = FontWeight.SemiBold)
                IconButton(onClick = { showTimer = true }) {
                    Icon(
                        Icons.Filled.Timer,
                        contentDescription = "Minuterie",
                        tint = if (PlayerController.sleepRemainingMs > 0) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                if (track != null) {
                    AlbumArt(track, Modifier.fillMaxWidth(0.9f).aspectRatio(1f), corner = 20)
                } else {
                    Icon(Icons.Filled.MusicNote, contentDescription = null, modifier = Modifier.size(120.dp))
                }
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        PlayerController.nowTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        PlayerController.nowArtist,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                val id = PlayerController.currentTrackId
                if (id != -1L) {
                    val isFav = id in favorites
                    IconButton(onClick = { onToggleFav(id) }) {
                        Icon(
                            if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Favori",
                            tint = if (isFav) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val duration = PlayerController.durationMs
            Slider(
                value = (if (dragging) dragValue else position.toFloat()).coerceIn(0f, maxOf(1f, duration.toFloat())),
                onValueChange = { dragging = true; dragValue = it },
                onValueChangeFinished = {
                    PlayerController.seekTo(dragValue.toLong())
                    position = dragValue.toLong()
                    dragging = false
                },
                valueRange = 0f..maxOf(1f, duration.toFloat())
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(if (dragging) dragValue.toLong() else position), fontSize = 12.sp)
                Text(formatTime(duration), fontSize = 12.sp)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { PlayerController.toggleShuffle() }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Aléatoire",
                        tint = if (PlayerController.shuffle) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { PlayerController.previous() }) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Précédent", modifier = Modifier.size(36.dp))
                }
                FilledIconButton(
                    onClick = { PlayerController.togglePlay() },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (PlayerController.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Lecture / pause",
                        modifier = Modifier.size(40.dp)
                    )
                }
                IconButton(onClick = { PlayerController.next() }) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Suivant", modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { PlayerController.cycleRepeat() }) {
                    val mode = PlayerController.repeatMode
                    Icon(
                        if (mode == Player.REPEAT_MODE_ONE) Icons.Filled.RepeatOne else Icons.Filled.Repeat,
                        contentDescription = "Boucle",
                        tint = if (mode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showTimer) {
        AlertDialog(
            onDismissRequest = { showTimer = false },
            title = { Text("Minuterie d'arrêt") },
            text = {
                Column {
                    val remaining = PlayerController.sleepRemainingMs
                    if (remaining > 0) {
                        Text("Arrêt dans ${formatTime(remaining)}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(8.dp))
                    }
                    listOf(15, 30, 45, 60).forEach { min ->
                        TextButton(
                            onClick = { PlayerController.startSleepTimer(min); showTimer = false },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("$min minutes", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth()) }
                    }
                    TextButton(
                        onClick = { PlayerController.startSleepTimer(0); showTimer = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Désactiver", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth()) }
                }
            },
            confirmButton = { TextButton(onClick = { showTimer = false }) { Text("Fermer") } }
        )
    }
}
