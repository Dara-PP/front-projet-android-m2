package com.example.projet_android_m2.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun ProximityGame(onGameFinished: (Int) -> Unit) {
    val context = LocalContext.current

    // 🔥 L'ASTUCE EST ICI : On détecte si on est dans l'aperçu Android Studio
    val isPreview = LocalInspectionMode.current

    var isHandNear by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var timeLeft by remember { mutableIntStateOf(10) } // 10 secondes pour réussir

    // 1. Initialisation et écoute du capteur
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
                isHandNear = distance < maxRange
            }
            override fun onAccuracyChanged(s: Sensor?, a: Int) {}
        }

        sensorManager.registerListener(listener, proximitySensor, SensorManager.SENSOR_DELAY_UI)

        // Nettoyage
        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // 2. Logique de la jauge qui se remplit
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
            delay(1000)
            timeLeft--
        }

        delay(500)
        if (progress >= 1f) {
            onGameFinished(1) // Gagné
        } else {
            onGameFinished(0) // Perdu
        }
    }

    // 4. L'interface graphique (très simple)
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DÉVERROUILLAGE SÉCURISÉ", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("Couvre le haut du téléphone avec ta main !", modifier = Modifier.padding(bottom = 30.dp))

        // Affichage du chrono qui devient rouge s'il reste peu de temps
        Text(
            text = "Temps restant : $timeLeft s",
            color = if (timeLeft <= 3) Color.Red else Color.Black,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(0.8f).height(15.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Petit feedback visuel
        if (isHandNear) {
            Text("✅ Analyse en cours...", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
        } else {
            Text("En attente...", color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestProximityGame() {
    // une action "onGameFinished". On lui donne donc une action vide : {}
    ProximityGame(onGameFinished = {})
}