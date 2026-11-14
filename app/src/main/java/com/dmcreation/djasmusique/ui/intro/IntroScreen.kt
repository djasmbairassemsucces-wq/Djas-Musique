package com.dmcreation.djasmusique.ui.intro

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dmcreation.djasmusique.R
import com.dmcreation.djasmusique.ui.theme.Gold
import com.dmcreation.djasmusique.ui.theme.GoldLight
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Intro en 3 étapes :
 *  1. Photo plein écran (apparition + léger zoom)
 *  2. Disparition en fondu
 *  3. Textes un par un, chacun sur sa ligne, centrés au milieu de l'écran
 * Un appui sur l'écran passe l'introduction.
 */
@Composable
fun IntroScreen(onFinished: () -> Unit) {
    val finish = rememberUpdatedState(onFinished)
    val photoAlpha = remember { Animatable(0f) }
    val photoScale = remember { Animatable(1.12f) }
    val lines = remember { List(5) { Animatable(0f) } }

    LaunchedEffect(Unit) {
        // Étapes 1 et 2 : photo
        coroutineScope {
            launch { photoScale.animateTo(1f, tween(5600, easing = LinearEasing)) }
            launch {
                photoAlpha.animateTo(1f, tween(1200, easing = FastOutSlowInEasing))
                delay(2800)
                photoAlpha.animateTo(0f, tween(1300, easing = FastOutSlowInEasing))
            }
        }
        delay(300)
        // Étape 3 : textes, dans l'ordre exact
        for (a in lines) {
            a.animateTo(1f, tween(1000, easing = FastOutSlowInEasing))
            delay(450)
        }
        delay(2200)
        finish.value()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF050507), Color(0xFF14110A), Color(0xFF050507)))
            )
            .pointerInput(Unit) { detectTapGestures { finish.value() } },
        contentAlignment = Alignment.Center
    ) {
        // Textes (toutes les lignes occupent leur place dès le départ : pas de saut de mise en page)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IntroLine(lines[0].value) {
                Text(
                    "Bienvenue Dans l'univers de DM Création",
                    color = Color(0xFFEDEDED),
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 18.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Light)
                )
            }
            Spacer(Modifier.height(26.dp))
            IntroLine(lines[1].value) {
                Text(
                    "Djas Mbaïrassem",
                    color = Gold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        fontSize = 38.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
            Spacer(Modifier.height(26.dp))
            IntroLine(lines[2].value) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.width(60.dp).height(1.dp).background(Gold))
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Un Vision Un Rêve Une Création",
                        color = GoldLight,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            fontFamily = FontFamily.Serif,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            letterSpacing = 3.sp
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    Box(Modifier.width(60.dp).height(1.dp).background(Gold))
                }
            }
            Spacer(Modifier.height(26.dp))
            IntroLine(lines[3].value) {
                Text(
                    "Concevoir C'est ma passion",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                )
            }
            Spacer(Modifier.height(34.dp))
            IntroLine(lines[4].value) {
                Text(
                    "Bienvenue Dans Djas Musique.",
                    color = Gold,
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 1.sp
                    )
                )
            }
        }

        // Photo plein écran, par-dessus le fond, qui disparaît avant les textes
        if (photoAlpha.value > 0f) {
            Image(
                painter = painterResource(R.drawable.intro_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = photoScale.value
                        scaleY = photoScale.value
                    }
                    .alpha(photoAlpha.value)
            )
        }
    }
}

@Composable
private fun IntroLine(progress: Float, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * 40f
        }
    ) { content() }
}
