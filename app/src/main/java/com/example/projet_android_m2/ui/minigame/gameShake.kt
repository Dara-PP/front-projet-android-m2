package com.example.projet_android_m2.ui.minigame

import android.Manifest
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sqrt
import com.example.projet_android_m2.R

@Composable
fun gameShake(
    onSuccess: () -> Unit,
    onFail: () -> Unit
) {
    val context: Context = LocalContext.current
    val sensorManager: SensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val accelerometer: Sensor? = remember { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }

    val vibrator: Vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    // MediaPlayer pour le son d'énergie
    val mediaPlayer: MediaPlayer? = remember {
        MediaPlayer.create(context, R.raw.test) // Assurez-vous que shake_sound.mp3 est dans res/raw/
    }

    var progress: Float by remember { mutableStateOf(0f) }
    var timer: Int by remember { mutableStateOf(10) } // 10 secondes pour réussir
    var gameOver: Boolean by remember { mutableStateOf(false) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            @RequiresPermission(Manifest.permission.VIBRATE)
            override fun onSensorChanged(event: SensorEvent) {
                val x: Float = event.values[0]
                val y: Float = event.values[1]
                val z: Float = event.values[2]

                val gForce: Float = sqrt(x * x + y * y + z * z)
                val shakeThreshold: Float = 15f // force minimum pour compter comme secousse

                if (gForce > shakeThreshold && !gameOver) {
                    progress += 0.1f

                    // Vibration
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }

                    // Son d'énergie
                    mediaPlayer?.let {
                        it.seekTo(0)
                        it.start()
                    }

                    // Succès si jauge remplie
                    if (progress >= 1f) {
                        gameOver = true
                        onSuccess()
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
    }

    DisposableEffect(Unit) {
        accelerometer?.let {
            sensorManager.registerListener(sensorEventListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        onDispose {
            sensorManager.unregisterListener(sensorEventListener)
            mediaPlayer?.release()
        }
    }

    // Interface utilisateur
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.DarkGray),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Secoue le téléphone pour capturer la carte ! 💨",
                color = Color.White,
                fontSize = 20.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            LinearProgressIndicator(
                progress = progress.coerceAtMost(1f),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(20.dp),
                color = Color.Cyan
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Temps restant : $timer s",
                color = Color.White,
                fontSize = 18.sp
            )
        }
    }

    LaunchedEffect(Unit) {
        while (!gameOver && timer > 0) {
            delay(1000)
            timer -= 1
        }
        if (!gameOver && progress < 1f) {
            gameOver = true
            onFail()
        }
    }
}