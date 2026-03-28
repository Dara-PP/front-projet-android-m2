package com.example.projet_android_m2.ui.game

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sqrt

private const val SHAKE_THRESHOLD = 12.0f
private const val GOLDEN_SHAKE_THRESHOLD = 17.0f

data class FallingApple(val id: Int, val xOffset: Dp, val isGolden: Boolean = false)

@Composable
fun ShakeTreeGame(
    onGameFinished: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var score by remember { mutableStateOf(0) }
    var timeRemaining by remember { mutableStateOf(10) }
    var isGameActive by remember { mutableStateOf(false) }

    // var pour le rendu visuel
    var treeTilt by remember { mutableFloatStateOf(0f) }
    val smoothTreeTilt by animateFloatAsState(targetValue = treeTilt, animationSpec = tween(100), label = "treeTilt")
    val apples = remember { mutableStateListOf<FallingApple>() }
    var appleCounter by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                if (event != null) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    // maj de l'inclinaison de l'arbre selon axe x
                    treeTilt = (x * -4f).coerceIn(-45f, 45f)

                    if (isGameActive) {
                        val currentForce = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

                        if (currentForce > SHAKE_THRESHOLD) {
                            // check si grosse secousse pr la pomme dorée
                            val isGolden = currentForce > GOLDEN_SHAKE_THRESHOLD
                            score += if (isGolden) 3 else 1

                            // pop une pomme avec pos random
                            val newApple = FallingApple(appleCounter++, ((-80..80).random()).dp, isGolden)
                            apples.add(newApple)

                            // retour haptique
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            // suppr la pomme de la liste apres 1s pr pas surcharger
                            coroutineScope.launch {
                                delay(1000)
                                apples.remove(newApple)
                            }
                        }
                    }
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        accelerometer?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose { sensorManager.unregisterListener(sensorListener) }
    }

    LaunchedEffect(isGameActive) {
        if (isGameActive) {
            score = 0
            timeRemaining = 10
            apples.clear()
            while (timeRemaining > 0) {
                delay(1000)
                timeRemaining--
            }
            isGameActive = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFFF0F8FF)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Secoue le Pommier !", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(8.dp))

        // jauge de tps
        LinearProgressIndicator(
            progress = { timeRemaining / 10f },
            modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).clip(RoundedCornerShape(6.dp)),
            color = if (timeRemaining < 4) Color.Red else Color(0xFF4CAF50),
            trackColor = Color.LightGray
        )
        Text("Temps : ${timeRemaining}s", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))

        Spacer(modifier = Modifier.height(40.dp))

        Box(contentAlignment = Alignment.TopCenter, modifier = Modifier.height(250.dp).fillMaxWidth()) {
            Text(
                text = "🌳",
                fontSize = 140.sp,
                modifier = Modifier.rotate(smoothTreeTilt).padding(top = 20.dp)
            )

            // anim de chute pr chq pomme
            apples.forEach { apple ->
                var yOffset by remember { mutableStateOf((-20).dp) }
                var alpha by remember { mutableFloatStateOf(1f) }

                LaunchedEffect(apple.id) {
                    launch {
                        androidx.compose.animation.core.animate(initialValue = -20f, targetValue = 250f, animationSpec = tween(800, easing = FastOutLinearInEasing)) { value, _ ->
                            yOffset = value.dp
                        }
                    }
                    launch {
                        androidx.compose.animation.core.animate(initialValue = 1f, targetValue = 0f, animationSpec = tween(800)) { value, _ ->
                            alpha = value
                        }
                    }
                }

                Text(
                    text = if (apple.isGolden) "✨" else "🍎",
                    fontSize = if (apple.isGolden) 45.sp else 35.sp,
                    modifier = Modifier.offset(x = apple.xOffset, y = yOffset).alpha(alpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.background(Color(0xFFFFF9C4), RoundedCornerShape(16.dp)).padding(horizontal = 32.dp, vertical = 16.dp)
        ) {
            Text(text = "Score : $score 🍎", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (!isGameActive && timeRemaining == 10) {
            Button(
                onClick = { isGameActive = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.height(60.dp).fillMaxWidth(0.7f)
            ) {
                Text("DÉMARRER LA RÉCOLTE", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
        else if (!isGameActive && timeRemaining == 0) {
            Text(text = "RÉCOLTE TERMINÉE !", fontSize = 24.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Black)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
            ) {
                Button(
                    onClick = { isGameActive = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                ) {
                    Text("REJOUER")
                }

                Button(
                    onClick = { onGameFinished(score) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE64A19))
                ) {
                    Text("QUITTER")
                }
            }
        }
        else {
            Text(text = "👉 SECOUE LE TÉLÉPHONE ! 👈", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE64A19))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TestShakeTreeGame() {
    ShakeTreeGame()
}