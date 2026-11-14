package com.dmcreation.djasmusique

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.dmcreation.djasmusique.data.AppPrefs
import com.dmcreation.djasmusique.ui.home.HomeScreen
import com.dmcreation.djasmusique.ui.intro.IntroScreen
import com.dmcreation.djasmusique.ui.settings.SettingsScreen
import com.dmcreation.djasmusique.ui.theme.DjasTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val prefs = AppPrefs(this)

        setContent {
            var themeMode by remember { mutableStateOf(prefs.themeMode) }
            var showIntro by remember { mutableStateOf(!prefs.introSeen) }
            var tab by remember { mutableIntStateOf(0) }

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

            DjasTheme(themeMode) {
                if (showIntro) {
                    IntroScreen(onFinished = {
                        prefs.introSeen = true
                        showIntro = false
                    })
                } else {
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                NavigationBarItem(
                                    selected = tab == 0,
                                    onClick = { tab = 0 },
                                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                                    label = { Text("Accueil") }
                                )
                                NavigationBarItem(
                                    selected = tab == 1,
                                    onClick = { tab = 1 },
                                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                                    label = { Text("Paramètres") }
                                )
                            }
                        }
                    ) { inner ->
                        if (tab == 0) {
                            HomeScreen(Modifier.padding(inner))
                        } else {
                            SettingsScreen(
                                themeMode = themeMode,
                                onThemeChange = {
                                    themeMode = it
                                    prefs.themeMode = it
                                },
                                onReplayIntro = { showIntro = true },
                                modifier = Modifier.padding(inner)
                            )
                        }
                    }
                }
            }
        }
    }
}
