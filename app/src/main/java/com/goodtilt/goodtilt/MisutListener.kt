package com.goodtilt.goodtilt

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.widget.Toast
import androidx.preference.PreferenceManager
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ListenerStatus {
    IDLE, TILT_LEFT, TILT_RIGHT, TILT_UP, TILT_DOWN, STOPOVER;
}

class MisutListener(
    val result: ((event : SensorEvent) -> Unit)?,
    val action: (index: Int) -> Unit
) : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f
    private val RAD2DGR = 180.0f / PI
    private var ts: Float = 0f
    private var min_velocity = 0f
    private var min_angle = 0f

    private var status: ListenerStatus = ListenerStatus.IDLE
    private var tilt_limit = FloatArray(4) { 1.0f; 4.0f; 1.0f; 4.0f } // Order: Horizontal_Low, Horizontal_High, Vertical_Low, Vertical_High
/*
    private val rotationAngle = FloatArray(3) { 0.0f }
    private val rotationVector = FloatArray(4) { 0.0f }
    private val rotationMatrix = FloatArray(9) { 0.0f }
*/
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
            Sensor.TYPE_GRAVITY -> {
                var tilt = FloatArray(3) {
                    evt.values[0];
                    evt.values[1];
                    evt.values[2];
                }
                result?.invoke(evt)

                when(status) {
                    ListenerStatus.IDLE -> {
                        if(evt.values[0] < -tilt_limit[0])
                            status = ListenerStatus.TILT_LEFT
                        else if(evt.values[0] > tilt_limit[0])
                            status = ListenerStatus.TILT_RIGHT
                        else if(evt.values[1] < -tilt_limit[2])
                            status = ListenerStatus.TILT_UP
                        else if(evt.values[1] > tilt_limit[2])
                            status = ListenerStatus.TILT_DOWN
                    }
                    ListenerStatus.TILT_LEFT -> {
                        if(evt.values[0] < -tilt_limit[1]) {
                            action(0)
                            status = ListenerStatus.STOPOVER
                        }
                    }
                    ListenerStatus.TILT_RIGHT -> {
                        if(evt.values[0] > tilt_limit[1]) {
                            action(1)
                            status = ListenerStatus.STOPOVER
                        }
                    }
                    ListenerStatus.TILT_UP -> {
                        if(evt.values[1] < -tilt_limit[3]) {
                            action(2)
                            status = ListenerStatus.STOPOVER
                        }
                    }
                    ListenerStatus.TILT_DOWN -> {
                        if(evt.values[1] > tilt_limit[3]) {
                            action(3)
                            status = ListenerStatus.STOPOVER
                        }
                    }
                    ListenerStatus.STOPOVER -> {
                        if(evt.values[0] in -tilt_limit[0]..tilt_limit[0] && evt.values[1] in -tilt_limit[2]..tilt_limit[2])
                            status = ListenerStatus.IDLE
                    }
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                var axis = FloatArray(3) {
                    evt.values[0];
                    evt.values[1];
                    evt.values[2];
                }
                result?.invoke(evt)

/*
                for (idx in 0..2)
                    rotationAngle[idx] += axis[idx] * dt
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
 */
            }

            else -> {
                action(-1)
            }
        }
        ts = evt?.timestamp?.toFloat() ?: 0f
    }
}