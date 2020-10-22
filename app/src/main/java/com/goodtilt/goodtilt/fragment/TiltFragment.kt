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
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.MisutListener
import com.goodtilt.goodtilt.R
import com.goodtilt.goodtilt.source.DeviceStatus
import kotlinx.android.synthetic.main.frag_tilt.*
import kotlinx.android.synthetic.main.frag_tilt.overlayLeft
import kotlinx.android.synthetic.main.frag_tilt.overlayRight
import kotlinx.android.synthetic.main.frag_tilt.view.*
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min

class TiltFragment(private val isManual: Boolean = true) : Fragment() {
    private val sensorListener = MisutListener(::printResult, ::printAction)
    private lateinit var sensorManager: SensorManager
    lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var preference: SharedPreferences
    private var configStage = DeviceStatus.IDLE
    private var configCount = 0
    val D2R = PI.toFloat() / 180.0f

    private var adjustViews = arrayOfNulls<ImageView>(4)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_tilt, container, false)
        sensorManager = inflater.context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //sensorListener.applyPreference(inflater.context)
        listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            updateTiltView()
        }
        preference = PreferenceManager.getDefaultSharedPreferences(context)
        rootView.apply {
            overlayLeft.updateFromPreference(preference)
            overlayRight.updateFromPreference(preference)
            adjustViews[0] = adjustDR
            adjustViews[1] = adjustDL
            adjustViews[2] = adjustUL
            adjustViews[3] = adjustUR

            val touchListener = View.OnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    tiltView2.rightHand = (view == overlayRight)
                    tiltView2.updatePath()
                    sensorListener.initBase(1, view == overlayRight)
                    changeListenerState(true)
                    view.setBackgroundResource(R.color.overlayClicked)
                    updateArrow()
                    updateTanAdjust()
                } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) {
                    view.setBackgroundResource(R.color.overlayDefault)
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
                                else -> {}
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
            autoConfig.setOnClickListener {
                autoConfig.visibility = View.GONE
                configStage = DeviceStatus.TILT_IN
                configCount = 3
                updateConfigView(this)
                updateConfigCount(this)
            }
            val adjustListener = View.OnTouchListener { view, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN)
                    view.tag = 1
                    activity?.findViewById<ViewPager2>(R.id.viewPager)?.isUserInputEnabled = false
                false
            }
            adjustViews.forEach {
                it?.tag = 0
                it?.setOnTouchListener(adjustListener)
            }

            tiltView2.setOnTouchListener { _, motionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN || motionEvent.action == MotionEvent.ACTION_MOVE) {
                    for (i in 1..4) {
                        if (adjustViews[i - 1]?.tag as Int == 1) {
                            var tan = (atan2(
                                motionEvent.y - tiltView2.centerY,
                                (motionEvent.x - tiltView2.centerX) * if (tiltView2.rightHand) 1F else -1F
                            ) / D2R).toInt() - (i - 1) * 90 + 360
                            tan %= 360
                            tan = min(max(0, tan), 90)
                            preference.edit().putInt(
                                "tan_quad_" + i.toString(),
                                tan
                            ).apply()
                        }
                        //updateTanAdjust(i)
                        context?.let { sensorListener.applyPreference(it) }
                    }
                } else if (motionEvent.action == MotionEvent.ACTION_CANCEL || motionEvent.action == MotionEvent.ACTION_UP) {
                    adjustViews.forEach { it?.tag = 0 }
                    activity?.findViewById<ViewPager2>(R.id.viewPager)?.isUserInputEnabled = true
                }
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

    private fun updateTanAdjust(dim: Int = 0) {
        var range: IntRange
        if (dim == 0)
            range = 1..4
        else
            range = dim..dim

        for (i in range) {
            val v = adjustViews[i - 1]
            tiltView2.radPosition(i).apply {
                val params = v?.layoutParams as ConstraintLayout.LayoutParams
                params.leftMargin = first
                params.topMargin = second
                v.layoutParams = params
            }
        }
    }

    private fun updateConfigCount(v: View? = view) {
        v?.tiltCount?.setText(configCount.toString())
    }

    private fun updateArrow(v: View? = view) {
        v?.apply{
            if (configStage == DeviceStatus.TILT_UP)
                arrowUp.visibility = View.VISIBLE
            else
                arrowUp.visibility = View.INVISIBLE
            if (configStage == DeviceStatus.TILT_DOWN)
                arrowDown.visibility = View.VISIBLE
            else
                arrowDown.visibility = View.INVISIBLE
            if (tiltView2.rightHand) {
                if (configStage == DeviceStatus.TILT_IN)
                    arrowRight.visibility = View.VISIBLE
                else
                    arrowRight.visibility = View.INVISIBLE
                if (configStage == DeviceStatus.TILT_OUT)
                    arrowLeft.visibility = View.VISIBLE
                else
                    arrowLeft.visibility = View.INVISIBLE
            } else {
                if (configStage == DeviceStatus.TILT_IN)
                    arrowLeft.visibility = View.VISIBLE
                else
                    arrowLeft.visibility = View.INVISIBLE
                if (configStage == DeviceStatus.TILT_OUT)
                    arrowRight.visibility = View.VISIBLE
                else
                    arrowRight.visibility = View.INVISIBLE
            }
        }
    }

    private fun updateConfigView(v: View? = view) {
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
            else -> {}
        }
        updateArrow(v)
    }

    private fun printResult(evt: SensorEvent) {
        when (evt.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                tiltView2.onSensorEvent(evt)
            }
        }
    }

    private fun updateTiltView() {
        preference.apply {
            tiltView2.updateSetting(
                getInt("upside_sensitivity", 50) / 100.0f + 0.2f,
                getInt("downside_sensitivity", 50) / 100.0f + 0.2f,
                getInt("inside_sensitivity", 50) / 100.0f + 0.2f,
                getInt("outside_sensitivity", 50) / 100.0f + 0.2f,
                getInt("min_angle", 10).toFloat(),
                getInt("max_angle", 20).toFloat(),
                ((0.0f + getInt("tan_quad_1", 45)) * D2R),
                ((90.0f + getInt("tan_quad_2", 45)) * D2R),
                ((180.0f + getInt("tan_quad_3", 45)) * D2R),
                ((270.0f + getInt("tan_quad_4", 45)) * D2R)
            )
            overlayLeft.updateFromPreference(this)
            overlayRight.updateFromPreference(this)
        }
        updateTanAdjust()
        context?.let { sensorListener.applyPreference(it) }
    }

    fun printAction(action: Int) {
        if (configStage == DeviceStatus.IDLE) {
            when (action) {
                DeviceStatus.TILT_IN.actionIndex -> tiltCount?.setText(resources.getString(R.string.in_tilt_action))
                DeviceStatus.TILT_OUT.actionIndex -> tiltCount?.setText(resources.getString(R.string.out_tilt_action))
                DeviceStatus.TILT_UP.actionIndex -> tiltCount?.setText(resources.getString(R.string.up_tilt_action))
                DeviceStatus.TILT_DOWN.actionIndex -> tiltCount?.setText(resources.getString(R.string.down_tilt_action))
            }
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

    private fun changeListenerState(state: Boolean) {
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