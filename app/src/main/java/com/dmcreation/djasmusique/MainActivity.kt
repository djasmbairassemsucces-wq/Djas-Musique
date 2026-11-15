package com.dmcreation.djasmusique

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dmcreation.djasmusique.data.AppPrefs
import com.dmcreation.djasmusique.data.Library
import com.dmcreation.djasmusique.data.VideoItem
import com.dmcreation.djasmusique.playback.PlayerController
import com.dmcreation.djasmusique.ui.PermissionScreen
import com.dmcreation.djasmusique.ui.intro.IntroScreen
import com.dmcreation.djasmusique.ui.music.MiniPlayer
import com.dmcreation.djasmusique.ui.music.MusicScreen
import com.dmcreation.djasmusique.ui.music.PlayerScreen
import com.dmcreation.djasmusique.ui.settings.SettingsScreen
import com.dmcreation.djasmusique.ui.theme.DjasTheme
import com.dmcreation.djasmusique.ui.video.VideoPlayerScreen
import com.dmcreation.djasmusique.ui.video.VideoScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private fun hasAudioPermission(): Boolean {
        val p = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED
    }

    private fun requiredPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= 33) arrayOf(
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.POST_NOTIFICATIONS
        ) else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = AppPrefs(this)

        setContent {
            var themeMode by remember { mutableStateOf(prefs.themeMode) }
            var showIntro by remember { mutableStateOf(!prefs.introSeen) }
            var tab by remember { mutableIntStateOf(0) }
            var granted by remember { mutableStateOf(hasAudioPermission()) }
            var favorites by remember { mutableStateOf(prefs.favorites) }
            var recents by remember { mutableStateOf(prefs.recents) }
            var showPlayer by remember { mutableStateOf(false) }
            var video by remember { mutableStateOf<VideoItem?>(null) }
            val scope = rememberCoroutineScope()

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { granted = hasAudioPermission() }

            val toggleFav: (Long) -> Unit = { id ->
                val updated = if (id in favorites) favorites - id else favorites + id
                favorites = updated
                prefs.favorites = updated
            }

            // Barres système masquées pendant l'intro pour un vrai plein écran
            LaunchedEffect(showIntro) {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                if (showIntro) {
                    controller.systemBarsBehavior =
                        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    controller.hide(WindowInsetsCompat.Type.systemBars())
                } else {
                    controller.show(WindowInsetsCompat.Type.systemBars())
                }
            }

            LaunchedEffect(Unit) {
                PlayerController.onTrackChanged = { id ->
                    prefs.addRecent(id)
                    recents = prefs.recents
                }
                PlayerController.connect(applicationContext)
            }

            LaunchedEffect(granted) {
                if (granted) Library.refresh(applicationContext)
            }

            DjasTheme(themeMode) {
                if (showIntro) {
                    IntroScreen(onFinished = {
                        prefs.introSeen = true
                        showIntro = false
                    })
                } else if (!granted) {
                    PermissionScreen(
                        onRequest = { launcher.launch(requiredPermissions()) },
                        onOpenSettings = {
                            startActivity(
                                Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        }
                    )
                } else {
                    Box {
                        Scaffold(
                            bottomBar = {
                                Column {
                                    if (PlayerController.hasMedia) {
                                        MiniPlayer(onClick = { showPlayer = true })
                                    }
                                    NavigationBar {
                                        NavigationBarItem(
                                            selected = tab == 0,
                                            onClick = { tab = 0 },
                                            icon = { Icon(Icons.Filled.MusicNote, contentDescription = null) },
                                            label = { Text("Musique") }
                                        )
                                        NavigationBarItem(
                                            selected = tab == 1,
                                            onClick = { tab = 1 },
                                            icon = { Icon(Icons.Filled.VideoLibrary, contentDescription = null) },
                                            label = { Text("Vidéos") }
                                        )
                                        NavigationBarItem(
                                            selected = tab == 2,
                                            onClick = { tab = 2 },
                                            icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                            label = { Text("Paramètres") }
                                        )
                                    }
                                }
                            }
                        ) { inner ->
                            when (tab) {
                                0 -> MusicScreen(
                                    favorites = favorites,
                                    recents = recents,
                                    onToggleFav = toggleFav,
                                    modifier = Modifier.padding(inner)
                                )
                                1 -> VideoScreen(
                                    prefs = prefs,
                                    onPlay = { video = it },
                                    modifier = Modifier.padding(inner)
                                )
                                else -> SettingsScreen(
                                    themeMode = themeMode,
                                    onThemeChange = {
                                        themeMode = it
                                        prefs.themeMode = it
                                    },
                                    onReplayIntro = { showIntro = true },
                                    onRescan = { scope.launch { Library.refresh(applicationContext) } },
                                    modifier = Modifier.padding(inner)
                                )
                            }
                        }
                        if (showPlayer) {
                            PlayerScreen(
                                favorites = favorites,
                                onToggleFav = toggleFav,
                                onClose = { showPlayer = false }
                            )
                        }
                        video?.let { v ->
                            VideoPlayerScreen(video = v, prefs = prefs, onClose = { video = null })
                        }
                    }
                }
            }
        }
    }
}
