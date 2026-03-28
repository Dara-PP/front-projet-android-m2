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
    onGameFinished: (score: Int) -> Unit // 1 = succès, 0 = échec
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val screenWidth = LocalContext.current.resources.displayMetrics.widthPixels.toFloat()
    var playerX by remember { mutableStateOf(screenWidth / 2) }
    val playerY = 1500f

    var bombs by remember { mutableStateOf(listOf<Bomb>()) }
    var phase by remember { mutableStateOf(1) }
    var survivalTime by remember { mutableStateOf(0f) }
    var stableTime by remember { mutableStateOf(0f) }

    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }

    // Liste des bombes en explosion (pour l’effet visuel)
    var explodingBombs by remember { mutableStateOf(listOf<Explosion>()) }

    // Capteurs
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                accelX = event.values[0]
                accelY = event.values[1]
                playerX -= accelX * 20
                playerX = playerX.coerceIn(0f, screenWidth)
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

            // Phase 1: éviter bombes
            if (phase == 1) {
                survivalTime += 0.05f
                bombs = bombs.map { it.copy(y = it.y + it.speed) }

                if (Random.nextFloat() < 0.1f) {
                    val newX = Random.nextInt(0, screenWidth.toInt()).toFloat()
                    if (bombs.none { abs(it.x - newX) < 100 }) {
                        bombs = bombs + Bomb(
                            x = newX,
                            y = 0f,
                            speed = Random.nextInt(15, 30).toFloat(),
                            radius = Random.nextInt(20, 40).toFloat()
                        )
                    }
                }

                val remainingBombs = mutableListOf<Bomb>()
                bombs.forEach {
                    val dx = abs(it.x - playerX)
                    val dy = abs(it.y - playerY)
                    val distance = sqrt(dx * dx + dy * dy)

                    if (distance < 150) {
                        val amplitude = ((150 - distance) / 150 * 255).toInt().coerceIn(1, 255)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(100, amplitude))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(100)
                        }
                    }

                    // Collision => mini-jeu échoué
                    if (dx < it.radius + 40 && dy < it.radius + 40) {
                        // Ajouter explosion
                        explodingBombs = explodingBombs + Explosion(it.x, it.y)
                        onGameFinished(0)
                    } else {
                        remainingBombs.add(it)
                    }
                }
                bombs = remainingBombs

                if (survivalTime >= 5f) {
                    phase = 2
                    bombs = emptyList()
                }

            } else {
                val magnitude = sqrt(accelX * accelX + accelY * accelY)
                if (magnitude < 1f) stableTime += 0.05f else stableTime = 0f
                if (stableTime >= 3f) onGameFinished(1) // succès
            }

            // Mise à jour des explosions
            explodingBombs = explodingBombs.map { it.copy(frame = it.frame + 1) }
                .filter { it.frame < 6 } // explosion dure 6 frames
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
                modifier = Modifier.fillMaxWidth().padding(16.dp)
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
                    radius = it.radius,
                    center = Offset(it.x, it.y)
                )
            }

            // Explosions animées
            explodingBombs.forEach {
                val alpha = 1f - it.frame / 6f
                drawCircle(
                    color = Color.Yellow.copy(alpha = alpha),
                    radius = 50f + it.frame * 10,
                    center = Offset(it.x, it.y)
                )
            }
        }
    }
}

data class Bomb(
    val x: Float,
    val y: Float,
    val speed: Float = 20f,
    val radius: Float = 30f
)

data class Explosion(
    val x: Float,
    val y: Float,
    val frame: Int = 0
)