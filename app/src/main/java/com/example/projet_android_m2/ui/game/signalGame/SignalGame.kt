package com.example.projet_android_m2.ui.game.signalGame

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlin.random.Random

class SignalGame(
    private val context: Context,
    private val onGameFinished: (Int) -> Unit
) : SensorEventListener {

    companion object {
        var azimuth by mutableFloatStateOf(0f)
            private set
        var pitch by mutableFloatStateOf(0f)
            private set
        var roll by mutableFloatStateOf(0f)
            private set
        var maxTime by mutableIntStateOf(120)
            private set
        var timeLeft by mutableIntStateOf(120)
            private set
        var isPlaying by mutableStateOf(false)
            private set
        var isWin by mutableStateOf(false)
            private set
    }

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer  = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private var accelValues  = FloatArray(3)
    private var magnetValues = FloatArray(3)
    private var targetAzimuth = 0f
    private var targetPitch   = 0f
    private var timer: CountDownTimer? = null
    private var stopScheduled = false

    var signalStrength by mutableIntStateOf(0)
        private set

    fun start() {
        if (isPlaying) return
        isWin         = false
        isPlaying     = true
        stopScheduled = false
        signalStrength = 0
        timeLeft      = maxTime
        targetAzimuth = Random.nextFloat() * 360f
        targetPitch   = -30f + Random.nextFloat() * 60f

        accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        magnetometer?.let  { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }

        timer?.cancel()
        timer = object : CountDownTimer(maxTime * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = (millisUntilFinished / 1000).toInt()
            }
            override fun onFinish() {
                timeLeft = 0
                stop()
            }
        }.start()
    }

    fun stop() {
        if (!isPlaying) return
        sensorManager.unregisterListener(this)
        timer?.cancel()
        timer     = null
        isPlaying = false
        onGameFinished(if (isWin) 1 else 0)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER  -> accelValues  = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> magnetValues = event.values.clone()
        }
        if (isPlaying) updateOrientation()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun updateOrientation() {
        val result = OrientationCalculator.calculateOrientation(accelValues, magnetValues)
        result?.let { (az, pi, ro) ->
            azimuth = az
            pitch   = pi
            roll    = ro
            signalStrength = SignalCalc.computeSignal(azimuth, pitch, targetAzimuth, targetPitch)

            if (signalStrength >= 95 && !stopScheduled) {
                stopScheduled  = true
                signalStrength = 100
                isWin          = true
                timer?.cancel()
                sensorManager.unregisterListener(this)

                // On attend que l'animation soit fini pour quitter
                Handler(Looper.getMainLooper()).postDelayed({
                    stop()
                }, 3000)
            }
        }
    }
}

@Composable
fun SignalGame(onGameFinished: (Int) -> Unit) {
    val context   = LocalContext.current
    val game      = remember { SignalGame(context, onGameFinished) }
    var useCamera by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { game.start() }

    // Fermer/nettoyer le jeu correctement
    DisposableEffect(Unit) {
        onDispose { game.stop() }
    }

    SignalGameScreen(
        signalGame     = game,
        useCamera      = useCamera,
        onToggleCamera = { useCamera = !useCamera }
    )
}