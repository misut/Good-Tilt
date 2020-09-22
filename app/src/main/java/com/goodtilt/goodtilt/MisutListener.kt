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
import com.goodtilt.goodtilt.source.DeviceStatus
import com.goodtilt.goodtilt.source.Discriminator
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
    private val MS2NS = 1000000

    private var baseAngle = Quaternion()

    private var discriminator = Discriminator()
    private var mode = 1
    private var rightHand = false
    private var activated = false
    private var delay: Long = 500000000
    private var startTime: Long = 0
    private var currentTime: Long = 0

    private fun actionSingle(status: DeviceStatus) {
        when(status) {
            DeviceStatus.IDLE -> {
                activated = false
            }
            DeviceStatus.TILT_LEFT,
            DeviceStatus.TILT_RIGHT,
            DeviceStatus.TILT_UP,
            DeviceStatus.TILT_DOWN -> {
                if(!activated) {
                    activated = true
                    startTime = currentTime
                    action?.invoke(status.actionIndex)
                }
            }
            DeviceStatus.STOPOVER -> {
                if(currentTime - startTime > delay)
                    activated = false
            }
        }
    }

    private fun actionDouble(status: DeviceStatus) {
        when(status) {
            DeviceStatus.IDLE -> {
                activated = false
            }
            DeviceStatus.TILT_LEFT,
            DeviceStatus.TILT_RIGHT,
            DeviceStatus.TILT_UP,
            DeviceStatus.TILT_DOWN -> {
                if(!activated) {
                    activated = true
                    startTime = currentTime
                    action?.invoke(status.actionIndex+4)
                }
            }
            DeviceStatus.STOPOVER -> {
                if(currentTime - startTime > delay)
                    activated = false
            }
        }
    }

    fun applyPreference(context : Context){
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            discriminator.updateSetting(
                getInt("hor_sensitivity", 50)/100.0f,
                getInt("ver_sensitivity", 50)/100.0f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                1.3f
            )
            delay = getInt("delay", 500).toLong() * MS2NS
        }

    }

    //rightHand : 오른손이면 true
    //mode 1이면 일반 모드, 2이면 스크롤 모드 (계속 액션 일어나야함)
    fun initBase(mode : Int, rightHand : Boolean) {
        baseAngle = Quaternion()
        this.mode = mode
        this.rightHand = rightHand
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
    }

    override fun onSensorChanged(evt: SensorEvent?) {
        if(evt == null)
            return
        currentTime = evt.timestamp

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
                discriminator.updateStatus(angles)
                var status = discriminator.getStatus()
                when(mode) {
                    1 -> actionSingle(status)
                    2 -> actionDouble(status)
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
    }
}