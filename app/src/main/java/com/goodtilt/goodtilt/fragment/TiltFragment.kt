package com.goodtilt.goodtilt.fragment

import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.MisutListener
import com.goodtilt.goodtilt.R
import com.goodtilt.goodtilt.source.DeviceStatus
import kotlinx.android.synthetic.main.frag_tilt.*
import kotlinx.android.synthetic.main.frag_tilt.overlayLeft
import kotlinx.android.synthetic.main.frag_tilt.overlayRight
import kotlinx.android.synthetic.main.frag_tilt.view.*
import kotlin.math.PI

class TiltFragment(private val isManual: Boolean = true) : Fragment() {
    private val sensorListener = MisutListener(::printResult, ::printAction)
    private lateinit var sensorManager: SensorManager
    lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var preference: SharedPreferences
    private var configStage = DeviceStatus.IDLE
    private var configCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_tilt, container, false)
        sensorManager = inflater.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //sensorListener.applyPreference(inflater.context)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { pref, string ->
            updateTiltView()
        }
        preference = PreferenceManager.getDefaultSharedPreferences(context)
        rootView.apply {
            overlayLeft.updateFromPreference(preference)
            overlayRight.updateFromPreference(preference)

            val touchListener = View.OnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    tiltView2.rightHand = (view == overlayRight)
                    tiltView2.updatePath()
                    sensorListener.initBase(1, view == overlayRight)
                    changeListenerState(true)
                    view.setBackgroundResource(R.color.OverlayClicked)
                    updateArrow()
                } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    view.setBackgroundResource(R.color.OverlayDefault)
                    if (configStage != DeviceStatus.IDLE) {
                        sensorListener.updatePreference(
                            context,
                            tiltView2.getCoord(),
                            configStage
                        )
                        configCount -= 1
                        if (configCount == 0) {
                            when (configStage) {
                                DeviceStatus.TILT_IN -> configStage = DeviceStatus.TILT_OUT
                                DeviceStatus.TILT_OUT -> configStage = DeviceStatus.TILT_UP
                                DeviceStatus.TILT_UP -> configStage = DeviceStatus.TILT_DOWN
                                DeviceStatus.TILT_DOWN -> configStage = DeviceStatus.IDLE
                            }
                            updateConfigView()
                            configCount = 3
                        }
                        updateConfigCount()
                        tiltView2.updatePath()
                    }
                    changeListenerState(false)
                }
                true
            }
            overlayLeft.setOnTouchListener(touchListener)
            overlayRight.setOnTouchListener(touchListener)
            autoConfig.setOnClickListener { view ->
                autoConfig.visibility = View.GONE
                configStage = DeviceStatus.TILT_IN
                configCount = 3
                updateConfigView(this)
                updateConfigCount(this)
                true
            }

            if (isManual) {
                val manualActivity = activity as ManualActivity
                next.setOnClickListener(manualActivity.nextListener)
                prev.setOnClickListener(manualActivity.prevListener)
                configStage = DeviceStatus.TILT_IN
                configCount = 3
            } else {
                next.visibility = View.GONE
                prev.visibility = View.GONE
                prefSensFrag.visibility = View.VISIBLE
            }
            updateConfigView(this)
            updateConfigCount(this)
        }
        return rootView
    }

    fun updateConfigCount(v: View? = view) {
        v?.tiltCount?.setText(configCount.toString())
    }

    fun updateArrow(v: View? = view) {
        if (configStage == DeviceStatus.TILT_UP)
            v?.arrowUp?.visibility = View.VISIBLE
        else
            v?.arrowUp?.visibility = View.INVISIBLE
        if (configStage == DeviceStatus.TILT_DOWN)
            v?.arrowDown?.visibility = View.VISIBLE
        else
            v?.arrowDown?.visibility = View.INVISIBLE
        if (v?.tiltView2?.rightHand!!){
            if (configStage == DeviceStatus.TILT_IN)
                v?.arrowRight?.visibility = View.VISIBLE
            else
                v?.arrowRight?.visibility = View.INVISIBLE
            if (configStage == DeviceStatus.TILT_OUT)
                v?.arrowLeft?.visibility = View.VISIBLE
            else
                v?.arrowLeft?.visibility = View.INVISIBLE
        } else {
            if (configStage == DeviceStatus.TILT_IN)
                v?.arrowLeft?.visibility = View.VISIBLE
            else
                v?.arrowLeft?.visibility = View.INVISIBLE
            if (configStage == DeviceStatus.TILT_OUT)
                v?.arrowRight?.visibility = View.VISIBLE
            else
                v?.arrowRight?.visibility = View.INVISIBLE
        }
    }

    fun updateConfigView(v: View? = view) {
        when (configStage) {
            DeviceStatus.IDLE -> {
                v?.tiltCount?.alpha = 0F
                v?.autoConfig?.visibility = View.VISIBLE
                v?.tiltInfo?.setText("")
            }
            DeviceStatus.TILT_IN -> {
                v?.tiltCount?.alpha = 1F
                v?.autoConfig?.visibility = View.INVISIBLE
                v?.tiltInfo?.setText(resources.getString(R.string.tilt_config_in))
            }
            DeviceStatus.TILT_OUT -> {
                v?.tiltCount?.alpha = 1F
                v?.autoConfig?.visibility = View.INVISIBLE
                v?.tiltInfo?.setText(resources.getString(R.string.tilt_config_out))
            }
            DeviceStatus.TILT_UP -> {
                v?.tiltCount?.alpha = 1F
                v?.autoConfig?.visibility = View.INVISIBLE
                v?.tiltInfo?.setText(resources.getString(R.string.tilt_config_up))
            }
            DeviceStatus.TILT_DOWN -> {
                v?.tiltCount?.alpha = 1F
                v?.autoConfig?.visibility = View.INVISIBLE
                v?.tiltInfo?.setText(resources.getString(R.string.tilt_config_down))
            }
        }
        updateArrow(v)
    }

    fun printResult(evt: SensorEvent) {
        when (evt.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                tiltView2.onSensorEvent(evt)
            }
        }
    }

    fun updateTiltView() {
        PreferenceManager.getDefaultSharedPreferences(context).apply {
            val D2R = PI.toFloat() / 180.0f
            tiltView2.updateSetting(
                getInt("upside_sensitivity", 50) / 100.0f,
                getInt("downside_sensitivity", 50) / 100.0f,
                getInt("inside_sensitivity", 50) / 100.0f,
                getInt("outside_sensitivity", 50) / 100.0f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                ((0.0f + getInt("tan_quad_1", 45)) * D2R).toFloat(),
                ((90.0f + getInt("tan_quad_2", 45)) * D2R).toFloat(),
                ((180.0f + getInt("tan_quad_3", 45)) * D2R).toFloat(),
                ((270.0f + getInt("tan_quad_4", 45)) * D2R).toFloat()
            )
            overlayLeft.updateFromPreference(this)
            overlayRight.updateFromPreference(this)
        }
        context?.let { sensorListener.applyPreference(it) }
    }

    fun printAction(action: Int) {
        if (configStage == DeviceStatus.IDLE){
            when(action){
                DeviceStatus.TILT_IN.actionIndex-> tiltCount?.setText(resources.getString(R.string.left_tilt_action))
                DeviceStatus.TILT_OUT.actionIndex-> tiltCount?.setText(resources.getString(R.string.right_tilt_action))
                DeviceStatus.TILT_UP.actionIndex-> tiltCount?.setText(resources.getString(R.string.up_tilt_action))
                DeviceStatus.TILT_DOWN.actionIndex-> tiltCount?.setText(resources.getString(R.string.down_tilt_action))
            }
            val fadeOut = AnimationUtils.loadAnimation(context, R.anim.fade_out)
            tiltCount.apply {
                alpha = 1F
                animate().alpha(0f).setDuration(1500L).start()
            }

        }
    }

    override fun onPause() {
        changeListenerState(false)
        preference.unregisterOnSharedPreferenceChangeListener(listener)
        super.onPause()
    }

    override fun onResume() {
        updateTiltView()
        overlayLeft.updateFromPreference(preference)
        overlayRight.updateFromPreference(preference)
        preference.registerOnSharedPreferenceChangeListener(listener)
        super.onResume()
    }

    fun changeListenerState(state: Boolean) {
        if (state) {
            //자이로스코프, 회전벡터 등록
            val sensorRott = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            sensorManager.registerListener(
                sensorListener,
                sensorRott,
                SensorManager.SENSOR_DELAY_GAME
            )
        } else {
            sensorManager.unregisterListener(sensorListener)
            tiltView2.initPosition()
        }
    }
}