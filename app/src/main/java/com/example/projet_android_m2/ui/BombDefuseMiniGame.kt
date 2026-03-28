package com.example.projet_android_m2.ui.minigames

import android.content.Context
import android.hardware.*
import android.os.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun BombDefuseMiniGame(
    onGameFinished: (Int) -> Unit
) {
    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

    var playerX by remember { mutableStateOf(screenWidth / 2) }
    val playerY = 1500f

    var bombs by remember { mutableStateOf(listOf<Bomb>()) }
    var explosions by remember { mutableStateOf(listOf<Explosion>()) }

    var phase by remember { mutableStateOf(1) }
    var survivalTime by remember { mutableStateOf(0f) }
    var stableTime by remember { mutableStateOf(0f) }

    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }

    var lastX by remember { mutableStateOf(0f) }
    var lastY by remember { mutableStateOf(0f) }

    var gameState by remember { mutableStateOf(GameState.INTRO) }

    fun vibrateStrong() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 200), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(300)
        }
    }

    fun startGame() {
        playerX = screenWidth / 2
        bombs = emptyList()
        explosions = emptyList()
        phase = 1
        survivalTime = 0f
        stableTime = 0f
        gameState = GameState.PLAYING
    }

    // 📱 capteurs
    DisposableEffect(Unit) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                accelX = event.values[0]
                accelY = event.values[1]

                if (gameState == GameState.PLAYING) {
                    playerX -= accelX * 20
                    playerX = playerX.coerceIn(0f, screenWidth)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        onDispose { sensorManager.unregisterListener(listener) }
    }

    // 🔁 GAME LOOP
    LaunchedEffect(gameState) {
        if (gameState != GameState.PLAYING) return@LaunchedEffect

        while (gameState == GameState.PLAYING) {
            delay(50)

            if (phase == 1) {
                survivalTime += 0.05f

                bombs = bombs.map { it.copy(y = it.y + it.speed) }

                if (Random.nextFloat() < 0.1f) {
                    val newX = Random.nextInt(0, screenWidth.toInt()).toFloat()
                    bombs = bombs + Bomb(x = newX, y = 0f)
                }

                bombs.forEach {
                    val dx = abs(it.x - playerX)
                    val dy = abs(it.y - playerY)

                    if (dx < it.radius + 40 && dy < it.radius + 40) {
                        explosions = explosions + Explosion(playerX, playerY)
                        vibrateStrong()
                        gameState = GameState.LOSE
                        onGameFinished(0)
                    }
                }

                if (survivalTime >= 20f) {
                    phase = 2
                    bombs = emptyList()
                }

            } else {
                val movement = abs(accelX - lastX) + abs(accelY - lastY)

                lastX = accelX
                lastY = accelY

                if (movement < 0.5f) stableTime += 0.05f
                else stableTime = 0f

                if (stableTime >= 10f) {
                    vibrateStrong()
                    gameState = GameState.WIN
                    onGameFinished(1)
                }
            }

            explosions = explosions
                .map { it.copy(radius = it.radius + 12f) }
                .filter { it.radius < 200f }
        }
    }

    // 🎨 UI
    Box(Modifier.fillMaxSize()) {

        when (gameState) {

            GameState.INTRO -> {
                IntroScreen(onStart = { startGame() })
            }

            GameState.PLAYING -> {
                Column {
                    Text(
                        text = if (phase == 1)
                            "💣 Survis 20s (${20 - survivalTime.toInt()}s)"
                        else
                            "📱 Stable 10s (${10 - stableTime.toInt()}s)",
                        modifier = Modifier.padding(16.dp)
                    )

                    Canvas(Modifier.fillMaxSize()) {

                        drawContext.canvas.nativeCanvas.drawText(
                            "😎",
                            playerX,
                            playerY,
                            android.graphics.Paint().apply {
                                textSize = 80f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )

                        bombs.forEach {
                            drawCircle(Color.Red, it.radius, Offset(it.x, it.y))
                        }

                        explosions.forEach {
                            drawCircle(
                                Color.Yellow.copy(alpha = 0.5f),
                                it.radius,
                                Offset(it.x, it.y)
                            )
                        }
                    }
                }
            }

            GameState.LOSE -> {
                EndScreen("💀 BOOM !", Color.Red, { startGame() }, { onGameFinished(0) })
            }

            GameState.WIN -> {
                EndScreen("🎉 Désamorcé !", Color.Green, { startGame() }, { onGameFinished(1) })
            }
        }
    }
}

@Composable
fun IntroScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "💣 Bomb Defuse",
            color = Color.Black,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        Text(
            "🎮 Instructions :\n\n" +
                    "• Incline ton téléphone pour bouger \n\n" +
                    "• Évite les bombes pendant 20 secondes \n\n" +
                    "• Ensuite, reste IMMOBILE 10 secondes \n",

            color = Color.LightGray,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(20.dp)
        )

        Spacer(Modifier.height(30.dp))

        Button(onClick = onStart) {
            Text("▶️ START")
        }
    }
}

@Composable
fun EndScreen(
    title: String,
    color: Color,
    onRetry: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, color = color)

        Spacer(Modifier.height(20.dp))

        Button(onClick = onRetry) { Text("🔄 Rejouer") }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onExit) { Text("🏠 Menu") }
    }
}

enum class GameState {
    INTRO, PLAYING, WIN, LOSE
}

data class Bomb(
    val x: Float,
    val y: Float,
    val speed: Float = Random.nextInt(15, 30).toFloat(),
    val radius: Float = 30f
)

data class Explosion(
    val x: Float,
    val y: Float,
    val radius: Float = 10f
)