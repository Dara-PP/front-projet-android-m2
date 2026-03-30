package com.example.projet_android_m2.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ProximityGame(onGameFinished: (Int) -> Unit) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val isPreview = LocalInspectionMode.current

    // Variables du gameplay
    var isHandNear by remember { mutableStateOf(false) }
    var swipeCount by remember { mutableIntStateOf(0) }
    val requiredSwipes = 6
    var timeLeft by remember { mutableIntStateOf(10) }

    val progress = swipeCount.toFloat() / requiredSwipes.toFloat() // Sécurité if(>0) retirée !
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "jauge")

    DisposableEffect(Unit) {
        if (isPreview) return@DisposableEffect onDispose {}

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val distance = event.values[0]
                val maxRange = proximitySensor?.maximumRange ?: 5f

                val wasHandNear = isHandNear
                isHandNear = distance < maxRange

                if (isHandNear && !wasHandNear) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (swipeCount < requiredSwipes) {
                        swipeCount++
                    }
                }
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    LaunchedEffect(swipeCount) {
        if (swipeCount >= requiredSwipes) {
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onGameFinished(1) // Gagné
        }
    }

    LaunchedEffect(Unit) {
        if (isPreview) return@LaunchedEffect

        while (timeLeft > 0 && swipeCount < requiredSwipes) {
            delay(1000)
            timeLeft--
        }

        if (swipeCount < requiredSwipes) {
            onGameFinished(0) // Perdu
        }
    }

    // 4. L'interface graphique (RACCORD AVEC LE LOGIN)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8)), // 👈 MÊME FOND CLAIR QUE LE LOGIN
        contentAlignment = Alignment.Center
    ) {

        // Le cadenas stylisé en fond (de la couleur bleu nuit du Login)
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.5f)
                .alpha(0.04f), // Très subtil
            tint = Color(0xFF2C3E50)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Titres sans police spéciale (utilise celle par défaut)
            Text(
                text = "OBTENTION DE CARTE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2C3E50) // 👈 Bleu Nuit du Login
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Passe ta main pour synchroniser",
                color = Color.Gray,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Temps : $timeLeft s",
                color = if (timeLeft <= 3) Color.Red else Color(0xFF2C3E50),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(16.dp),
                color = Color(0xFF27AE60), // Le même vert que ton bouton "JOUER"
                trackColor = Color(0xFFE2E8F0) // Fond de jauge gris clair
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (swipeCount >= requiredSwipes) {
                Text("CARTE OBTENUE ✅", color = Color(0xFF27AE60), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            } else if (isHandNear) {
                Text("En attente de retrait...", color = Color(0xFF2C3E50), fontWeight = FontWeight.Medium, fontSize = 16.sp)
            } else {
                Text("Synchronisation : $swipeCount / $requiredSwipes", color = Color.Gray, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestProximityGame() {
    ProximityGame(onGameFinished = {})
}