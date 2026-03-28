package com.example.projet_android_m2.ui.game.signalGame

import android.hardware.SensorManager

object OrientationCalculator {
    fun calculateOrientation(
        accelValues: FloatArray,
        magnetValues: FloatArray
    ): Triple<Float, Float, Float>? {
        val r = FloatArray(9)
        val orientation = FloatArray(3)
        val success = SensorManager.getRotationMatrix(r, null, accelValues, magnetValues)
        if(!success) return null
        SensorManager.getOrientation(r, orientation)
        val azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        return Triple(azimuth, pitch, roll)
    }

}