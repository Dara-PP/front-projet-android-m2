package com.example.projet_android_m2.ui.game.signalGame

import kotlin.math.abs

object SignalCalc {
    fun computeSignal(
        azimuth: Float, pitch: Float,
        targetAz: Float, targetP: Float
    ): Int {

        val azDiff = angularDifference(azimuth, targetAz)
        val pitchDiff = abs(pitch - targetP)

        val maxAzDiff = 60f
        val maxPitchDiff = 30f

        val azSignal = ((maxAzDiff - azDiff) / maxAzDiff * 50).coerceIn(0f, 50f)
        val pitchSignal = ((maxPitchDiff - pitchDiff) / maxPitchDiff * 50).coerceIn(0f, 50f)

        return (azSignal + pitchSignal).toInt()
    }

    fun angularDifference(a: Float, b: Float): Float {
        val diff = abs(a - b) % 360
        return if (diff > 180) 360 - diff else diff
    }
}


