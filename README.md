# DJAS MUSIQUE — DM CRÉATION
Créateur : Djas Mbaïrassem. Kotlin + Jetpack Compose (+ Media3 à partir de l'étape 2).

## Obtenir l'APK avec GitHub (sans Android Studio)
1. Crée un dépôt GitHub vide.
2. Envoie TOUT le contenu de ce dossier (y compris le dossier `.github`).
3. Onglet **Actions** > workflow **Build APK** > attends la coche verte (3-6 min).
4. Ouvre l'exécution > section **Artifacts** > télécharge `DJAS-MUSIQUE-apk` (zip) > extrais `app-debug.apk`.
5. Copie l'APK sur le téléphone et installe-le (autorise « sources inconnues »).

Si la compilation échoue : ouvre l'étape rouge, copie les dernières lignes d'erreur et envoie-les à Claude.

## Compiler en local
Android Studio : File > Open sur ce dossier > Run. Ou : `gradle assembleDebug` (Gradle 8.9, JDK 17).

## Modifier
- Textes/animations de l'intro : `ui/intro/IntroScreen.kt`
- Couleurs/thèmes : `ui/theme/Theme.kt`
- Photo d'intro : `res/drawable-nodpi/intro_photo.jpg` ; icône : `res/mipmap-*` et `drawable-nodpi/ic_launcher_foreground.png`
