package com.example.projet_android_m2.ui.minigames

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun BombDefuseMiniGame(
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    // Position du joueur
    var playerX by remember { mutableStateOf(500f) }
    val playerY = 1500f

    // Bombes
    var bombs by remember { mutableStateOf(listOf<Bomb>()) }

    // Phase du mini-jeu
    var phase by remember { mutableStateOf(1) }

    // Temps survie et stabilité
    var survivalTime by remember { mutableStateOf(0f) }
    var stableTime by remember { mutableStateOf(0f) }

    // Accéléromètre
    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }

    // Sensor Listener
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                accelX = event.values[0]
                accelY = event.values[1]

                // Déplacer le joueur selon inclinaison
                playerX -= accelX * 20
                playerX = playerX.coerceIn(0f, 1000f) // limite écran
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // Loop du jeu
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)

            if (phase == 1) {
                // Phase éviter les bombes
                survivalTime += 0.05f

                // Descente des bombes
                bombs = bombs.map { it.copy(y = it.y + 25) }

                // Spawn aléatoire des bombes
                if (Random.nextFloat() < 0.1f) {
                    bombs = bombs + Bomb(x = Random.nextInt(0, 1000).toFloat(), y = 0f)
                }

                bombs.forEach {
                    // Distance entre bombe et joueur
                    val dx = abs(it.x - playerX)
                    val dy = abs(it.y - playerY)
                    val distance = sqrt(dx * dx + dy * dy)

                    // Vibration si proche (<150 px)
                    if (distance < 150) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(
                                VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    }

                    // Collision
                    if (dx < 80 && dy < 80) {
                        onFail()
                    }
                }

                // Si survie ≥ 5s → phase 2
                if (survivalTime >= 5f) {
                    phase = 2
                    bombs = emptyList()
                }

            } else {
                // Phase garder stable
                if (abs(accelX) < 1 && abs(accelY) < 1) {
                    stableTime += 0.05f
                } else {
                    stableTime = 0f
                }

                if (stableTime >= 3f) {
                    onSuccess()
                }
            }
        }
    }

    // UI Compose
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (phase == 1) "Évitez les bombes !" else "Gardez le téléphone stable !",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        if (phase == 2) {
            LinearProgressIndicator(
                progress = stableTime / 3f,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            // Joueur
            drawCircle(
                color = Color.Blue,
                radius = 40f,
                center = Offset(playerX, playerY)
            )

            // Bombes
            bombs.forEach {
                drawCircle(
                    color = Color.Red,
                    radius = 30f,
                    center = Offset(it.x, it.y)
                )
            }
        }
    }
}

data class Bomb(
    val x: Float,
    val y: Float
)