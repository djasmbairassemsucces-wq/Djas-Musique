package com.dmcreation.djasmusique.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.dmcreation.djasmusique.data.Library
import com.dmcreation.djasmusique.data.Track

/** Pont entre l'interface et le service de lecture. L'état est observable par Compose. */
object PlayerController : Player.Listener {
    private var controller: MediaController? = null
    private var connecting = false
    private var pending: (() -> Unit)? = null
    private val handler = Handler(Looper.getMainLooper())

    var currentTrackId by mutableLongStateOf(-1L)
        private set
    var currentTrack by mutableStateOf<Track?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var hasMedia by mutableStateOf(false)
        private set
    var shuffle by mutableStateOf(false)
        private set
    var repeatMode by mutableIntStateOf(Player.REPEAT_MODE_OFF)
        private set
    var durationMs by mutableLongStateOf(0L)
        private set
    var nowTitle by mutableStateOf("")
        private set
    var nowArtist by mutableStateOf("")
        private set
    var sleepRemainingMs by mutableLongStateOf(0L)
        private set

    var onTrackChanged: ((Long) -> Unit)? = null

    fun connect(context: Context) {
        if (controller != null || connecting) return
        connecting = true
        val app = context.applicationContext
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        val future = MediaController.Builder(app, token).buildAsync()
        future.addListener({
            try {
                val c = future.get()
                controller = c
                c.addListener(this@PlayerController)
                syncState()
            } catch (e: Exception) {
                controller = null
            }
            connecting = false
            pending?.invoke()
            pending = null
        }, ContextCompat.getMainExecutor(app))
    }

    private fun Track.toMediaItem(): MediaItem {
        val t = this
        return MediaItem.Builder()
            .setMediaId(t.id.toString())
            .setUri(t.uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(t.title)
                    .setArtist(t.artist)
                    .setAlbumTitle(t.album)
                    .setArtworkUri(Uri.parse("content://media/external/audio/albumart/${t.albumId}"))
                    .build()
            )
            .build()
    }

    fun playQueue(context: Context, tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        val action = {
            controller?.let { c ->
                c.setMediaItems(tracks.map { it.toMediaItem() }, startIndex, 0L)
                c.prepare()
                c.play()
            }
            Unit
        }
        if (controller == null) {
            pending = action
            connect(context)
        } else {
            action()
        }
    }

    fun togglePlay() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun pause() { controller?.pause() }
    fun next() { controller?.seekToNext() }
    fun previous() { controller?.seekToPrevious() }
    fun seekTo(ms: Long) { controller?.seekTo(ms) }
    fun currentPosition(): Long = controller?.currentPosition ?: 0L

    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    // ----- minuterie d'arrêt automatique -----
    private val tick = object : Runnable {
        override fun run() {
            sleepRemainingMs -= 1000
            if (sleepRemainingMs <= 0) {
                sleepRemainingMs = 0
                controller?.pause()
            } else {
                handler.postDelayed(this, 1000)
            }
        }
    }

    fun startSleepTimer(minutes: Int) {
        handler.removeCallbacks(tick)
        if (minutes <= 0) {
            sleepRemainingMs = 0
            return
        }
        sleepRemainingMs = minutes * 60_000L
        handler.postDelayed(tick, 1000)
    }

    // ----- écoute des changements du lecteur -----
    override fun onEvents(player: Player, events: Player.Events) {
        syncState()
    }

    private fun syncState() {
        val c = controller ?: return
        isPlaying = c.isPlaying
        shuffle = c.shuffleModeEnabled
        repeatMode = c.repeatMode
        durationMs = if (c.duration > 0) c.duration else 0L
        val item = c.currentMediaItem
        hasMedia = item != null
        nowTitle = item?.mediaMetadata?.title?.toString() ?: ""
        nowArtist = item?.mediaMetadata?.artist?.toString() ?: ""
        val id = item?.mediaId?.toLongOrNull() ?: -1L
        if (id != currentTrackId) {
            currentTrackId = id
            currentTrack = Library.track(id)
            if (id != -1L) onTrackChanged?.invoke(id)
        }
    }
}
