package com.dmcreation.djasmusique.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmcreation.djasmusique.data.Library
import com.dmcreation.djasmusique.data.ThemeMode

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    onReplayIntro: () -> Unit,
    onRescan: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text("Paramètres", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))

        SectionCard("Thème") {
            ThemeRow("Suivre le système", themeMode == ThemeMode.SYSTEM) { onThemeChange(ThemeMode.SYSTEM) }
            ThemeRow("Mode clair", themeMode == ThemeMode.LIGHT) { onThemeChange(ThemeMode.LIGHT) }
            ThemeRow("Mode sombre", themeMode == ThemeMode.DARK) { onThemeChange(ThemeMode.DARK) }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard("Bibliothèque") {
            Text("${Library.tracks.size} musique(s) · ${Library.videos.size} vidéo(s) détectée(s)")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onRescan) { Text("Analyser à nouveau le téléphone") }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard("Introduction") {
            Text("Revoir l'introduction animée de DM Création.")
            Spacer(Modifier.height(10.dp))
            Button(onClick = onReplayIntro) { Text("Revoir l'introduction") }
        }
        Spacer(Modifier.height(16.dp))

        SectionCard("DM CRÉATION") {
            Text("Maison de création derrière DJAS MUSIQUE. Une vision, un rêve, une création.")
        }
        Spacer(Modifier.height(16.dp))

        SectionCard("Le créateur") {
            Text("Djas Mbaïrassem — concevoir, c'est ma passion.")
        }
        Spacer(Modifier.height(16.dp))

        SectionCard("À propos") {
            Text("DJAS MUSIQUE · version 0.2.0 (étape 2)")
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun ThemeRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label)
    }
}
