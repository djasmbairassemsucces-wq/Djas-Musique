package com.dmcreation.djasmusique.ui.music

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmcreation.djasmusique.data.Library
import com.dmcreation.djasmusique.data.Track
import com.dmcreation.djasmusique.playback.PlayerController
import com.dmcreation.djasmusique.ui.common.AlbumArt
import com.dmcreation.djasmusique.ui.common.formatTime

@Composable
fun MusicScreen(
    favorites: Set<Long>,
    recents: List<Long>,
    onToggleFav: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var tab by remember { mutableIntStateOf(0) }
    var openGroup by remember { mutableStateOf<String?>(null) }
    val tabs = listOf("Titres", "Albums", "Artistes", "Favoris", "Récents")
    val all = Library.tracks
    val filtered = remember(all, query) {
        if (query.isBlank()) all else all.filter { it.matches(query.trim()) }
    }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Rechercher titre, artiste, album") },
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
        ScrollableTabRow(selectedTabIndex = tab, edgePadding = 12.dp) {
            tabs.forEachIndexed { i, name ->
                Tab(
                    selected = tab == i,
                    onClick = { tab = i; openGroup = null },
                    text = { Text(name) }
                )
            }
        }
        if (!Library.loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            when (tab) {
                0 -> TrackList(filtered, favorites, onToggleFav, "Aucune musique trouvée sur ce téléphone.")
                1 -> GroupedTracks(
                    tracks = filtered,
                    keyOf = { it.album },
                    subtitleOf = { list -> "${list.first().artist} · ${list.size} titre(s)" },
                    openGroup = openGroup,
                    onOpen = { openGroup = it },
                    favorites = favorites,
                    onToggleFav = onToggleFav
                )
                2 -> GroupedTracks(
                    tracks = filtered,
                    keyOf = { it.artist },
                    subtitleOf = { list -> "${list.size} titre(s)" },
                    openGroup = openGroup,
                    onOpen = { openGroup = it },
                    favorites = favorites,
                    onToggleFav = onToggleFav
                )
                3 -> TrackList(
                    filtered.filter { it.id in favorites }, favorites, onToggleFav,
                    "Aucun favori. Appuie sur le cœur d'un titre pour l'ajouter."
                )
                else -> {
                    val recentTracks = recents.mapNotNull { Library.track(it) }
                        .filter { query.isBlank() || it.matches(query.trim()) }
                    TrackList(recentTracks, favorites, onToggleFav, "Rien d'écouté récemment.")
                }
            }
        }
    }
}

@Composable
private fun TrackList(
    tracks: List<Track>,
    favorites: Set<Long>,
    onToggleFav: (Long) -> Unit,
    emptyText: String
) {
    val ctx = LocalContext.current
    if (tracks.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text(emptyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        val currentId = PlayerController.currentTrackId
        LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
            itemsIndexed(tracks, key = { _, t -> t.id }) { index, t ->
                TrackRow(
                    track = t,
                    isCurrent = t.id == currentId,
                    isFav = t.id in favorites,
                    onClick = { PlayerController.playQueue(ctx, tracks, index) },
                    onFav = { onToggleFav(t.id) }
                )
            }
        }
    }
}

@Composable
private fun TrackRow(
    track: Track,
    isCurrent: Boolean,
    isFav: Boolean,
    onClick: () -> Unit,
    onFav: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlbumArt(track, Modifier.size(52.dp))
        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                track.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                "${track.artist} · ${formatTime(track.durationMs)}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onFav) {
            Icon(
                if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                contentDescription = "Favori",
                tint = if (isFav) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupedTracks(
    tracks: List<Track>,
    keyOf: (Track) -> String,
    subtitleOf: (List<Track>) -> String,
    openGroup: String?,
    onOpen: (String?) -> Unit,
    favorites: Set<Long>,
    onToggleFav: (Long) -> Unit
) {
    if (openGroup != null) {
        val list = tracks.filter { keyOf(it) == openGroup }
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onOpen(null) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                Text(openGroup, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            TrackList(list, favorites, onToggleFav, "Aucun titre.")
        }
    } else {
        val groups = remember(tracks) { tracks.groupBy(keyOf).toList() }
        if (groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Rien à afficher.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 8.dp)) {
                items(groups, key = { it.first }) { group ->
                    val name = group.first
                    val list = group.second
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpen(name) }
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumArt(list.first(), Modifier.size(56.dp))
                        Column(
                            Modifier
                                .weight(1f)
                                .padding(horizontal = 12.dp)
                        ) {
                            Text(
                                name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                subtitleOf(list),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
