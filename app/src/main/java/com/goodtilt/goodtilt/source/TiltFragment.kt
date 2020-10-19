package com.goodtilt.goodtilt.source

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.MainActivity
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.MisutListener
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.activity_main.view.tiltView
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.frag_tilt.*
import kotlinx.android.synthetic.main.frag_tilt.overlayLeft
import kotlinx.android.synthetic.main.frag_tilt.overlayRight
import kotlinx.android.synthetic.main.frag_tilt.view.*
import kotlin.math.PI
import kotlin.math.tan

class TiltFragment : Fragment(){
    private val D2R: Float = PI.toFloat()/180.0f
    private val sensorListener = MisutListener(::printResult, ::printAction)
    private lateinit var sensorManager: SensorManager
    private val listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_tilt, container, false)
        sensorManager = inflater.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorListener.applyPreference(inflater.context)
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            rootView.tiltView2.updateSetting(
                getInt("upside_sensitivity", 50)/100.0f,
                getInt("downside_sensitivity", 50)/100.0f,
                getInt("left_sensitivity", 50)/100.0f,
                getInt("right_sensitivity", 50)/100.0f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                (0.0f + getInt("tan_quad_1", 45)) * D2R,
                (90.0f + getInt("tan_quad_2", 45)) * D2R,
                (180.0f + getInt("tan_quad_3", 45)) * D2R,
                (270.0f + getInt("tan_quad_4", 45)) * D2R
            )
        }
        rootView.apply {
            overlayLeft.layoutParams.width = (resources.displayMetrics.widthPixels / 1000F * pref.getInt("area_width", 50)).toInt()
            overlayLeft.layoutParams.height = (resources.displayMetrics.heightPixels / 1000F * pref.getInt("area_height", 500)).toInt()
            overlayLeft.y = (resources.displayMetrics.heightPixels - overlayLeft.layoutParams.height) / 1000F * pref.getInt("area_vertical_position", 500)
            overlayRight.layoutParams.width = overlayLeft.layoutParams.width
            overlayRight.layoutParams.height = overlayLeft.layoutParams.height
            overlayRight.y = overlayLeft.y
            overlayLeft.requestLayout()
            overlayRight.requestLayout()

            val touchListener =   View.OnTouchListener{ view , motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    sensorListener.initBase(1, view == overlayRight)
                    changeListenerState(true)
                }else if (motionEvent.action == MotionEvent.ACTION_UP)
                    changeListenerState(false)
                true
            }
            overlayLeft.setOnTouchListener(touchListener)
            overlayRight.setOnTouchListener(touchListener)

            val manualActivity = activity as ManualActivity
            next.setOnClickListener(manualActivity.nextListener)
            prev.setOnClickListener(manualActivity.prevListener)
        }
        return rootView
    }

    fun printResult(evt : SensorEvent) {
        when(evt.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                tiltView2.onSensorEvent(evt)
            }
        }
    }

    fun printAction(action: Int) {
        this.view?.tiltInfo?.setText(action.toString())
    }

    override fun onPause() {
        changeListenerState(false)
        super.onPause()
    }

    fun changeListenerState(state: Boolean) {
        if (state) {
            //자이로스코프, 회전벡터 등록
            val sensorRott = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            sensorManager.registerListener(sensorListener, sensorRott, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager.unregisterListener(sensorListener)
            tiltView2.initPosition()
        }
    }
}