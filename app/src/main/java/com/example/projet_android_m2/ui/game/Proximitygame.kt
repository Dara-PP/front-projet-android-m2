package com.example.projet_android_m2.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ProximityGame(onGameFinished: (Int) -> Unit) {
    val context = LocalContext.current

    // faire vibrer le téléphone !
    val haptic = LocalHapticFeedback.current

    // aperçu Android Studio
    val isPreview = LocalInspectionMode.current

    var isHandNear by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var timeLeft by remember { mutableIntStateOf(5) } // 5 secondes pour réussir

    // ameliorer le chargement de la jauge
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "jauge")

    // 1. Initialisation
    DisposableEffect(Unit) {
        if (isPreview) {
            // Si on est dans l'aperçu, on désactive la recherche du capteur pour ne pas planter
            return@DisposableEffect onDispose {}
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val distance = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5f
                val wasHandNear = isHandNear
                isHandNear = distance < maxRange

                // Si la main vient juste d'approcher, on fait une petite vibration !
                if (isHandNear && !wasHandNear) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)

        // Nettoyage
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // 2. Comment la jauge qui se remplit
    LaunchedEffect(isHandNear) {
        if (isPreview) return@LaunchedEffect // On bloque l'animation dans l'aperçu

        // Tant que la main est proche et que le temps n'est pas écoulé
        while (isHandNear && progress < 1f && timeLeft > 0) {
            delay(100) // on attend 100ms
            progress += 0.05f // on ajoute 5%
        }
    }

    // 3. compte à rebours global
    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect // On bloque le chrono dans l'aperçu

        while (timeLeft > 0 && progress < 1f) {
            delay(500)
            timeLeft--
        }

        delay(500)
        if (progress >= 1f) {
            // Grosse vibration de victoire !
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFinished(1) // Gagné
        } else {
            onGameFinished(0) // Perdu
        }
    }

    // 4. L'interface graphique
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "DÉVERROUILLAGE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif, // 👈 LA POLICE QUI CHANGE
            color = Color(0xFF2C3E50) // Le bleu nuit de ton Login
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Sous-titre explicatif
        Text(
            text = "Vite,couvre le haut du téléphone .",
            color = Color.Gray,
            fontSize = 14.sp,
            fontFamily = FontFamily.Serif // 👈 Cohérence
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Affichage du chrono
        Text(
            text = "Temps restant : $timeLeft s",
            color = if (timeLeft <= 3) Color.Red else Color(0xFF2C3E50),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif // 👈 Cohérence
        )

        Spacer(modifier = Modifier.height(16.dp))

        // La jauge de progression aux couleurs nouvelles couleurs
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(12.dp),
            color = Color(0xFF27AE60),
            trackColor = Color(0xFFE0E0E0)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feedback visuel
        if (isHandNear) {
            Text(
                text = "Analyse en cours...",
                color = Color(0xFF27AE60),
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif // 👈 Cohérence
            )
        } else {
            Text(
                text = "En attente...",
                color = Color.Gray,
                fontFamily = FontFamily.Serif // 👈 Cohérence
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestProximityGame() {
    ProximityGame(onGameFinished = {})
}