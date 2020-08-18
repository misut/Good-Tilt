package com.goodtilt.goodtilt

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.preference.PreferenceManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class MisutListener(
    val result: ((event : SensorEvent) -> Unit)?,
    val action: (index: Int) -> Unit
) : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f
    private val RAD2DGR = 180.0f / PI
    private val rotationAngle = FloatArray(3) { 0.0f }
    private val rotationVector = FloatArray(4) { 0.0f }
    private val rotationMatrix = FloatArray(9) { 0.0f }
    private var ts: Float = 0f
    private var min_velocity = 0f
    private var min_angle = 0f

    fun applyPreference(context : Context){
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            min_angle = getInt("min_angle", 0) * 1f
            min_velocity = getInt("min_velocity", 0) * 1f
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(evt: SensorEvent?) {
        if(evt == null)
            return
        val dt = (evt.timestamp - ts) * NS2S
        when(evt.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                var tilt = FloatArray(3) {
                    evt.values[0];
                    evt.values[1];
                    evt.values[2];
                }
                result?.invoke(evt)
            }

            Sensor.TYPE_GYROSCOPE -> {
                var axis = FloatArray(3) {
                    evt.values[0];
                    evt.values[1];
                    evt.values[2];
                }
                for (idx in 0..2)
                    rotationAngle[idx] += axis[idx] * dt

                result?.invoke(evt)

                val omegaMagnitude: Float =
                    sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2])
                if (omegaMagnitude > 0.1) {
                    for (idx in 0..2)
                        axis[idx] /= omegaMagnitude
                }
                val thetaOverTwo: Float = omegaMagnitude * dt / 2.0f
                val sinThetaOverTwo: Float = sin(thetaOverTwo)
                val cosThetaOverTwo: Float = cos(thetaOverTwo)
                rotationVector[0] = sinThetaOverTwo * axis[0]
                rotationVector[1] = sinThetaOverTwo * axis[1]
                rotationVector[2] = sinThetaOverTwo * axis[2]
                rotationVector[3] = cosThetaOverTwo
                SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVector);
            }

            else -> {
                action(-1)
            }
        }
        ts = evt?.timestamp?.toFloat() ?: 0f
    }
}