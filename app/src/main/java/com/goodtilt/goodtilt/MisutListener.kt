package com.goodtilt.goodtilt

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.util.Log
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.source.DeviceStatus
import com.goodtilt.goodtilt.source.Discriminator
import com.goodtilt.goodtilt.source.Quaternion
import java.util.*
import kotlin.math.*

class MisutListener(
    val result: ((event : SensorEvent) -> Unit)?,
    val action: (index: Int) -> Unit
) : SensorEventListener {
    private val MS2NS = 1000000
    private val D2R = PI/180.0f

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
            DeviceStatus.TILT_IN,
            DeviceStatus.TILT_OUT,
            DeviceStatus.TILT_UP,
            DeviceStatus.TILT_DOWN -> {
                if(!activated) {
                    activated = true
                    startTime = currentTime
                    action?.invoke(status.actionIndex)
                }
            }
            DeviceStatus.STOPOVER -> {
            }
        }
    }

    private fun actionDouble(status: DeviceStatus) {
        when(status) {
            DeviceStatus.IDLE -> {
                activated = false
            }
            DeviceStatus.TILT_IN,
            DeviceStatus.TILT_OUT,
            DeviceStatus.TILT_UP,
            DeviceStatus.TILT_DOWN -> {
                if(!activated) {
                    activated = true
                    startTime = currentTime
                    action?.invoke(status.actionIndex+4)
                }
            }
            DeviceStatus.STOPOVER -> {
            }
        }
    }

    fun applyPreference(context : Context){
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            discriminator.updateSetting(
                getInt("upside_sensitivity", 40)/100.0f+0.2f,
                getInt("downside_sensitivity", 30)/100.0f+0.2f,
                getInt("inside_sensitivity", 40)/100.0f+0.2f,
                getInt("outside_sensitivity", 30)/100.0f+0.2f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                ((0.0f + getInt("tan_quad_1", 45)) * D2R).toFloat(),
                ((90.0f + getInt("tan_quad_2", 45)) * D2R).toFloat(),
                ((180.0f + getInt("tan_quad_3", 45)) * D2R).toFloat(),
                ((270.0f + getInt("tan_quad_4", 45)) * D2R).toFloat()
            )
            delay = getInt("delay", 500).toLong() * MS2NS
        }
    }

    fun updatePreference(context: Context, pos: FloatArray, status: DeviceStatus) {
        discriminator.feed(pos, status, rightHand)
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply () {
            putInt("upside_sensitivity", (discriminator.u*100.0f).toInt()-20)
            putInt("downside_sensitivity", (discriminator.d*100.0f).toInt()-20)
            putInt("inside_sensitivity", (discriminator.i*100.0f).toInt()-20)
            putInt("outside_sensitivity", (discriminator.o*100.0f).toInt()-20)
            commit()
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

                result?.invoke(evt)
                discriminator.updateStatus(angles, rightHand)
                var status = discriminator.getStatus()
                when(mode) {
                    1 -> actionSingle(status)
                    2 -> actionDouble(status)
                }
                if(activated && currentTime-startTime > delay)
                    activated = false
            }

            else -> {
                action(-1)
            }
        }
    }
}