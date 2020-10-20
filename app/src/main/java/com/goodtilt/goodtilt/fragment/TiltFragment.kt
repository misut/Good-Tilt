package com.goodtilt.goodtilt.fragment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.MisutListener
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.frag_tilt.*
import kotlinx.android.synthetic.main.frag_tilt.overlayLeft
import kotlinx.android.synthetic.main.frag_tilt.overlayRight
import kotlinx.android.synthetic.main.frag_tilt.view.*
import kotlinx.android.synthetic.main.frag_tilt.view.next
import kotlinx.android.synthetic.main.frag_tilt.view.overlayLeft
import kotlinx.android.synthetic.main.frag_tilt.view.overlayRight
import kotlinx.android.synthetic.main.frag_tilt.view.prev
import kotlinx.android.synthetic.main.frag_tilt.view.tiltView2
import kotlin.math.PI

class TiltFragment(private val isManual : Boolean = true) : Fragment(){
    private val sensorListener = MisutListener(::printResult, ::printAction)
    private lateinit var sensorManager: SensorManager

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
        rootView.apply {
            overlayLeft.updateFromPreference(pref)
            overlayRight.updateFromPreference(pref)

            val touchListener =   View.OnTouchListener{ view , motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    sensorListener.initBase(1, view == overlayRight)
                    tiltView2.rightHand = (view == overlayRight)
                    tiltView2.updatePath()
                    changeListenerState(true)
                    view.setBackgroundResource(R.color.OverlayClicked)
                }else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    changeListenerState(false)
                    view.setBackgroundResource(R.color.OverlayDefault)
                }
                true
            }
            overlayLeft.setOnTouchListener(touchListener)
            overlayRight.setOnTouchListener(touchListener)

            if (isManual) {
                val manualActivity = activity as ManualActivity
                next.setOnClickListener(manualActivity.nextListener)
                prev.setOnClickListener(manualActivity.prevListener)
             } else {
                textView1.visibility = View.GONE
                next.visibility = View.GONE
                prev.visibility = View.GONE
                prefSensFrag.visibility = View.VISIBLE
            }
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

    override fun onResume() {
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            val D2R = PI.toFloat()/180.0f
            tiltView2.updateSetting(
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
            overlayLeft.updateFromPreference(this)
            overlayRight.updateFromPreference(this)
        }

        super.onResume()
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