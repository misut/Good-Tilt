package com.goodtilt.goodtilt.fragment

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.MisutListener
import com.goodtilt.goodtilt.R
import com.goodtilt.goodtilt.TiltAccessibilityService
import com.goodtilt.goodtilt.const.KeyAction
import kotlinx.android.synthetic.main.frag_guide.*
import kotlinx.android.synthetic.main.frag_guide.view.*
import kotlinx.android.synthetic.main.frag_guide.view.next
import kotlinx.android.synthetic.main.frag_guide.view.overlayLeft
import kotlinx.android.synthetic.main.frag_guide.view.overlayRight
import kotlinx.android.synthetic.main.frag_guide.view.prev
import kotlinx.android.synthetic.main.frag_guide.view.tiltView2
import kotlin.math.PI

class GuideFragment : Fragment(){
    private val sensorListener = MisutListener(::printResult, ::printAction)
    private lateinit var sensorManager: SensorManager
    private var guideStep = 0
    private var actionCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_guide, container, false)
        sensorManager = inflater.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorListener.applyPreference(inflater.context)
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            val D2R = PI.toFloat()/180.0f
            rootView.tiltView2.updateSetting(
                getInt("upside_sensitivity", 50)/100.0f,
                getInt("downside_sensitivity", 50)/100.0f,
                getInt("inside_sensitivity", 50)/100.0f,
                getInt("outside_sensitivity", 50)/100.0f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                (0.0f + getInt("tan_quad_1", 45)) * D2R,
                (90.0f + getInt("tan_quad_2", 45)) * D2R,
                (180.0f + getInt("tan_quad_3", 45)) * D2R,
                (270.0f + getInt("tan_quad_4", 45)) * D2R
            )
        }
        rootView.apply {
            overlayLeft.updateFromPreference(pref)
            overlayRight.updateFromPreference(pref)

            val touchListener =   View.OnTouchListener{ view , motionEvent ->
                if(motionEvent.action == MotionEvent.ACTION_DOWN) {
                    actionCount = 0
                    if (guideStep == 0) {
                        guideStep = 1
                        textView3.visibility = View.VISIBLE
                        imageView3.visibility = View.VISIBLE
                        imageView2.setImageResource(R.drawable.ic_baseline_check_24)
                    }
                    view.setBackgroundResource(R.color.OverlayClicked)
                    sensorListener.initBase(1, view == overlayRight)
                    tiltView2.rightHand = (view == overlayRight)
                    tiltView2.updatePath()
                    changeListenerState(true)
                }else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    if (guideStep == 1) {
                        guideStep = 0
                        textView3.visibility = View.INVISIBLE
                        imageView2.setImageResource(R.drawable.ic_baseline_close_24)
                    }
                    view.setBackgroundResource(R.color.OverlayDefault)
                    changeListenerState(false)
                }
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
        val vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrateEffect = VibrationEffect.createOneShot(100, 50)
        vibrator.vibrate(vibrateEffect)
        actionCount += 1
        if (guideStep == 1) {
            guideStep = 2
            textView4.visibility = View.VISIBLE
            imageView3.setImageResource(R.drawable.ic_baseline_check_24)
        } else if (guideStep == 2 && actionCount > 1) {
            guideStep = 3
            imageView4.setImageResource(R.drawable.ic_baseline_check_24)
        }
        TiltAccessibilityService.doAction(KeyAction.NOTIFY.action)
    }

    override fun onPause() {
        changeListenerState(false)
        super.onPause()
    }

    override fun onResume() {
        val preference = PreferenceManager.getDefaultSharedPreferences(context)
        overlayLeft.updateFromPreference(preference)
        overlayRight.updateFromPreference(preference)
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