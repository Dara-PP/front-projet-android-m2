package com.example.projet_android_m2.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt

private const val SHAKE_THRESHOLD = 15.0f

@Composable
fun ShakeTreeGame(
    onGameFinished: (Int) -> Unit = {}
) {
    val context = LocalContext.current

    var score by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(10) }
    var isGameActive by remember { mutableStateOf(false) }

    var lastForce by remember { mutableStateOf(0f) }
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null && isGameActive) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val currentForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                    lastForce = currentForce

                    if (currentForce > SHAKE_THRESHOLD) {
                        score++
                        println("SECOUSSE DÉTECTÉE ! Force: $currentForce, Score: $score")
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }

        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            score = 0
            timeRemaining = 10
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            isGameActive = false
            //onGameFinished(score)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Jeu de David : Secoue le Pommier !",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "🌳",
            fontSize = 100.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Score : $score 🍎", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(text = "Temps : ${timeRemaining}s", fontSize = 24.sp, color = if (timeRemaining < 3) Color.Red else Color.Black)


        Spacer(modifier = Modifier.height(32.dp))

        if (!isGameActive && timeRemaining == 10) {
            Button(onClick = { isGameActive = true }) {
                Text("DÉMARRER")
            }
        }
        else if (!isGameActive && timeRemaining == 0) {
            Text(text = "PARTIE TERMINÉE !", fontSize = 20.sp, color = Color.Blue, fontWeight = FontWeight.Bold)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Button(onClick = {
                    isGameActive = true
                }) {
                    Text("REJOUER")
                }

                Button(onClick = {
                    onGameFinished(score)
                }) {
                    Text("QUITTER")
                }
            }
        }
        else {
            Text(text = "👉 SECOUE TON TÉLÉPHONE ! 👈", fontSize = 18.sp)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TestShakeTreeGame() {
    ShakeTreeGame()
}