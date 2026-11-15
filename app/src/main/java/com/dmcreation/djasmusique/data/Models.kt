package com.dmcreation.djasmusique.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri
) {
    fun matches(q: String): Boolean =
        title.contains(q, ignoreCase = true) ||
            artist.contains(q, ignoreCase = true) ||
            album.contains(q, ignoreCase = true)
}

data class VideoItem(
    val id: Long,
    val title: String,
    val durationMs: Long,
    val uri: Uri
)

object MediaRepository {

    private fun clean(s: String?, fallback: String): String =
        if (s.isNullOrBlank() || s == "<unknown>") fallback else s

    fun loadTracks(context: Context): List<Track> {
        val collection: Uri = if (Build.VERSION.SDK_INT >= 29)
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 30000"
        val out = ArrayList<Track>()
        try {
            context.contentResolver.query(
                collection, projection, selection, null,
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val iTitle = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val iArtist = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val iAlbum = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val iAlbumId = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    out.add(
                        Track(
                            id = id,
                            title = clean(c.getString(iTitle), "Sans titre"),
                            artist = clean(c.getString(iArtist), "Artiste inconnu"),
                            album = clean(c.getString(iAlbum), "Album inconnu"),
                            albumId = c.getLong(iAlbumId),
                            durationMs = c.getLong(iDur),
                            uri = ContentUris.withAppendedId(collection, id)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // permission absente ou erreur MediaStore : liste vide
        }
        return out
    }

    fun loadVideos(context: Context): List<VideoItem> {
        val collection: Uri = if (Build.VERSION.SDK_INT >= 29)
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        else
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DURATION
        )
        val out = ArrayList<VideoItem>()
        try {
            context.contentResolver.query(
                collection, projection, null, null,
                "${MediaStore.Video.Media.DATE_ADDED} DESC"
            )?.use { c ->
                val iId = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val iTitle = c.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)
                val iDur = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                while (c.moveToNext()) {
                    val id = c.getLong(iId)
                    out.add(
                        VideoItem(
                            id = id,
                            title = clean(c.getString(iTitle), "Vidéo"),
                            durationMs = c.getLong(iDur),
                            uri = ContentUris.withAppendedId(collection, id)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // ignoré
        }
        return out
    }
}

/** Bibliothèque en mémoire, partagée par tous les écrans. */
object Library {
    var tracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var videos by mutableStateOf<List<VideoItem>>(emptyList())
        private set
    var loaded by mutableStateOf(false)
        private set
    private var byId: Map<Long, Track> = emptyMap()

    fun track(id: Long): Track? = byId[id]

    suspend fun refresh(context: Context) {
        val app = context.applicationContext
        val t = withContext(Dispatchers.IO) { MediaRepository.loadTracks(app) }
        val v = withContext(Dispatchers.IO) { MediaRepository.loadVideos(app) }
        byId = t.associateBy { it.id }
        tracks = t
        videos = v
        loaded = true
    }
}
