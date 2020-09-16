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
    private val NS2S = 1.0f / 1000000000.0f
    private var ts: Float = 0f

    private var baseAngle = Quaternion()

    private var discriminator = Discriminator()
    private var mode = 1
    private var rightHand = false
    private var activated = false

    private fun actionNormal(status: DeviceStatus) {
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
                    action?.invoke(status.actionIndex)
                }
            }
            DeviceStatus.STOPOVER -> {
            }
        }
    }

    private fun actionScroll(status: DeviceStatus) {
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
                    action?.invoke(status.actionIndex)
                }
            }
            DeviceStatus.STOPOVER -> {
                if(activated) {
                    activated = false
                    action?.invoke(status.actionIndex)
                }
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
                discriminator.updateStatus(angles)
                var status = discriminator.getStatus()
                when(mode) {
                    1 -> actionNormal(status)
                    2 -> actionScroll(status)
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