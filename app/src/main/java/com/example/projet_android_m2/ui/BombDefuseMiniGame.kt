package com.example.fapecargo.minigames

import android.content.Context
import android.hardware.*
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
import kotlin.random.Random

@Composable
fun BombDefuseMiniGame(
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {

    val context = LocalContext.current
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var playerX by remember { mutableStateOf(500f) }
    val playerY = 1500f

    var bombs by remember { mutableStateOf(listOf<Bomb>()) }

    var phase by remember { mutableStateOf(1) }

    var survivalTime by remember { mutableStateOf(0f) }
    var stableTime by remember { mutableStateOf(0f) }

    var accelX by remember { mutableStateOf(0f) }
    var accelY by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {

                accelX = event.values[0]
                accelY = event.values[1]

                playerX -= accelX * 20
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(
            listener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    LaunchedEffect(Unit) {

        while (true) {

            delay(50)

            if (phase == 1) {

                survivalTime += 0.05f

                bombs = bombs.map {
                    it.copy(y = it.y + 25)
                }

                if (Random.nextFloat() < 0.1f) {
                    bombs = bombs + Bomb(
                        x = Random.nextInt(0, 1000).toFloat(),
                        y = 0f
                    )
                }

                bombs.forEach {

                    if (
                        abs(it.x - playerX) < 80 &&
                        abs(it.y - playerY) < 80
                    ) {
                        onFail()
                    }
                }

                if (survivalTime >= 5f) {
                    phase = 2
                }

            } else {

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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Text(
            text = if (phase == 1)
                "Évitez les bombes !"
            else
                "Gardez le téléphone stable !",
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

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {

            drawCircle(
                color = Color.Blue,
                radius = 40f,
                center = Offset(playerX, playerY)
            )

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