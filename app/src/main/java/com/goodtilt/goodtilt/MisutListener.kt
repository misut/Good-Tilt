package com.goodtilt.goodtilt

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.renderscript.Matrix4f
import android.view.MotionEvent
import android.widget.Toast
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.source.Quaternion
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class ListenerStatus(var min: Float, var max: Float) {
    IDLE(0.0f, 0.0f),
    TILT_LEFT(10.0f, 40.0f),
    TILT_RIGHT(10.0f, 40.0f),
    TILT_UP(10.0f, 40.0f),
    TILT_DOWN(10.0f, 40.0f),
    STOPOVER(0.0f, 0.0f);
}

class MisutListener(
    val result: ((event : SensorEvent) -> Unit)?,
    val action: (index: Int) -> Unit
) : SensorEventListener {
    private val NS2S = 1.0f / 1000000000.0f
    private val RAD2DGR = 180.0f / PI
    private var ts: Float = 0f

    private var isInitAngle = false
    private var baseAngle = Quaternion()

    private var status: ListenerStatus = ListenerStatus.IDLE

    fun applyPreference(context : Context){
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            ListenerStatus.TILT_LEFT.min = getInt("minimum_angle", 15).toFloat()
            ListenerStatus.TILT_LEFT.max = getInt("maximum_angle", 30).toFloat()
            ListenerStatus.TILT_RIGHT.min = -getInt("minimum_angle", 15).toFloat()
            ListenerStatus.TILT_RIGHT.max = -getInt("maximum_angle", 30).toFloat()
            ListenerStatus.TILT_UP.min = -getInt("minimum_angle", 15).toFloat()
            ListenerStatus.TILT_UP.max = -getInt("maximum_angle", 30).toFloat()
            ListenerStatus.TILT_DOWN.min = getInt("minimum_angle", 15).toFloat()
            ListenerStatus.TILT_DOWN.max = getInt("maximum_angle", 30).toFloat()
            ListenerStatus.STOPOVER.min = getInt("minimum_angle", 15).toFloat()
        }

    }

    fun initBase() {
        baseAngle = Quaternion()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(evt: SensorEvent?) {
        if(evt == null)
            return
        val dt = (evt.timestamp - ts) * NS2S
        when(evt.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                if (baseAngle.isInvalid())
                    baseAngle = Quaternion(evt.values).conjugate()
                val q2 = Quaternion(evt.values)
                val angles = (baseAngle * q2).eulerAngle()

                evt.values[0] = angles[0]
                evt.values[1] = angles[1]
                //evt.values[2] = angles[2]

                result?.invoke(evt)

                when(status) {
                    ListenerStatus.IDLE -> {
                        if(angles[0] > ListenerStatus.TILT_LEFT.min) {
                            status = ListenerStatus.TILT_LEFT
                        }
                        else if(angles[0] < ListenerStatus.TILT_RIGHT.min) {
                            status = ListenerStatus.TILT_RIGHT
                        }
                        else if(angles[1] < ListenerStatus.TILT_UP.min) {
                            status = ListenerStatus.TILT_UP
                        }
                        else if(angles[1] > ListenerStatus.TILT_DOWN.min) {
                            status = ListenerStatus.TILT_DOWN
                        }
                    }
                    ListenerStatus.TILT_LEFT -> {
                        if(angles[0] > ListenerStatus.TILT_LEFT.max) {
                            status = ListenerStatus.STOPOVER
                            action.invoke(0)
                        }
                    }
                    ListenerStatus.TILT_RIGHT -> {
                        if(angles[0] < ListenerStatus.TILT_RIGHT.max) {
                            status = ListenerStatus.STOPOVER
                            action.invoke(1)
                        }
                    }
                    ListenerStatus.TILT_UP -> {
                        if(angles[1] < ListenerStatus.TILT_UP.max) {
                            status = ListenerStatus.STOPOVER
                            action.invoke(2)
                        }
                    }
                    ListenerStatus.TILT_DOWN -> {
                        if(angles[1] > ListenerStatus.TILT_DOWN.max) {
                            status = ListenerStatus.STOPOVER
                            action.invoke(3)
                        }
                    }
                    ListenerStatus.STOPOVER -> {
                        if(angles[0] < status.min && angles[0] > -status.min &&
                            angles[1] > -status.min && angles[1] < status.min)
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
            }

            else -> {
                action(-1)
            }
        }
        ts = evt?.timestamp?.toFloat() ?: 0f
    }
}