package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.animation.TimeAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.Image
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.renderscript.ScriptGroup
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.const.*
import kotlinx.android.synthetic.main.activity_setting.*

class EventService : Service() {
    private var overlayView = arrayOfNulls<View>(2)
    private lateinit var floatingView: ImageView
    var actionList = arrayOf(
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.NONE,
        KeyAction.SWIPE_HALT
    )
    private var btnPressTime = 0L
    private var placementState = false

    private val sensorListener = MisutListener(null, ::onActionOccur)
    private var swipePosX = 0.0f
    private var swipePosY = 0.0f

    private var dX = 0.0f
    private var dY = 0.0f

    private var vibrateEffect: VibrationEffect? = null
    private lateinit var wm: WindowManager

    private var listenerState = LISTENER_IDLE

    companion object {
        const val LISTENER_IDLE = 0
        const val LISTENER_SINGLE = 1
        const val LISTENER_DOUBLE = 2
        const val DOUBLE_CLICK_DELAY = 300
    }

    private lateinit var floatingParams: WindowManager.LayoutParams

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        var isVibrate = false;
        var isTransparent = false;
        swipePosX = resources.displayMetrics.widthPixels / 2.0f
        swipePosY = resources.displayMetrics.heightPixels / 2.0f
        sensorListener.applyPreference(this)

        var area_width = 0
        var area_height = 0
        var area_vertical_pos = 0

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            getString("tilt_left", "NONE")?.let { actionList[0] = KeyAction.valueOf(it) }
            getString("tilt_right", "NONE")?.let { actionList[1] = KeyAction.valueOf(it) }
            getString("tilt_up", "NONE")?.let { actionList[2] = KeyAction.valueOf(it) }
            getString("tilt_down", "NONE")?.let { actionList[3] = KeyAction.valueOf(it) }
            getString("swipe_tilt_left", "NONE")?.let { actionList[4] = KeyAction.valueOf(it) }
            getString("swipe_tilt_right", "NONE")?.let { actionList[5] = KeyAction.valueOf(it) }
            getString("swipe_tilt_up", "NONE")?.let { actionList[6] = KeyAction.valueOf(it) }
            getString("swipe_tilt_down", "NONE")?.let { actionList[7] = KeyAction.valueOf(it) }
            //getInt("overlay_x", swipePosX.toInt())?.let { params.x = it}
            //getInt("overlay_y", swipePosY.toInt())?.let { params.y = it}
            getBoolean("transparent", false)?.let { isTransparent = it }
            getBoolean("vibration", false)?.let { isVibrate = it }

            getInt("area_width", 50)?.let {area_width = it}
            getInt("area_height", 500)?.let {area_height = it}
            getInt("area_vertical_position", 500)?.let {area_vertical_pos = it}
        }

        val strId = getString(R.string.channel_id)
        val strTitle = getString(R.string.app_name)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel = notificationManager.getNotificationChannel(strId)
        if (channel == null) {
            channel = NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, strId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Event Generator is working")
            .setContentText(actionList[0].str(this) + "버튼 기능 작동중...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
        startForeground(1, notification)

        if (isVibrate) {
            vibrateEffect = VibrationEffect.createOneShot(100, 50)
        }

        val touchListener = View.OnTouchListener { view, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                //Double Click
                if (System.currentTimeMillis() < btnPressTime + DOUBLE_CLICK_DELAY) {
                    btnPressTime = System.currentTimeMillis()
                    changeListenerState(LISTENER_DOUBLE, view == overlayView[1])
                    if (!isTransparent)
                        view.setBackgroundColor(Color.GREEN);
                    floatingView.setImageResource(R.drawable.ic_baseline_done_all_24)
                    floatingParams.x = motionEvent.rawX.toInt()
                    floatingParams.y = motionEvent.rawY.toInt() - 250
                    wm.updateViewLayout(floatingView, floatingParams)
                } else { //Normal Press
                    btnPressTime = System.currentTimeMillis()
                    changeListenerState(LISTENER_SINGLE, view == overlayView[1])
                    if (!isTransparent)
                        view.setBackgroundColor(Color.RED);
                    floatingView.setImageResource(R.drawable.ic_baseline_done_24)
                    floatingParams.x = motionEvent.rawX.toInt()
                    floatingParams.y = motionEvent.rawY.toInt() - 250
                    wm.updateViewLayout(floatingView, floatingParams)
                }
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) { //||
                changeListenerState(LISTENER_IDLE)
                floatingView.setImageResource(0)
                if (!isTransparent)
                    view.setBackgroundColor(Color.YELLOW);
            } else if (motionEvent.action == MotionEvent.ACTION_OUTSIDE && TiltAccessibilityService.isGesturing()) {
                if (TiltAccessibilityService.touchOutside()) {
                    vibrate()
                    changeListenerState(LISTENER_IDLE)
                    if (!isTransparent)
                        view.setBackgroundColor(Color.YELLOW);
                    floatingView.setImageResource(0)
                }
            } else if (motionEvent.action == MotionEvent.ACTION_MOVE) {
                floatingParams.x = motionEvent.rawX.toInt()
                floatingParams.y = motionEvent.rawY.toInt() - 250
                wm.updateViewLayout(floatingView, floatingParams)
            }
            true
        }

        var params = WindowManager.LayoutParams(
            (resources.displayMetrics.widthPixels / 1000F * area_width).toInt(),
            (resources.displayMetrics.heightPixels / 1000F * area_height).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        //HERE
        params.y = ((resources.displayMetrics.heightPixels - params.height)/ 1000F * area_vertical_pos).toInt()


        floatingParams = WindowManager.LayoutParams(
            0, 0,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )


        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (i in 0..1) {
            overlayView[i] = inflate.inflate(R.layout.view_overlay, null)
            overlayView[i]?.setOnTouchListener(touchListener);
            if (!isTransparent) {
                overlayView[i]?.setBackgroundColor(Color.YELLOW)
            }
        }
        floatingView = inflate.inflate(R.layout.singie_image, null) as ImageView

        params.gravity = Gravity.START or Gravity.TOP
        wm.addView(overlayView[0], params) // 윈도우에 layout 을 추가 한다.
        params.gravity = Gravity.END or Gravity.TOP
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        floatingParams.gravity =  Gravity.START or Gravity.TOP
        wm.addView(overlayView[1], params)
        wm.addView(floatingView, floatingParams)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun onActionOccur(index: Int) {
        vibrate();
        generateEvent(actionList[index])
    }

    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateEffect == null) return
        vibrator.vibrate(vibrateEffect)
    }

    fun generateEvent(keyAction: KeyAction): Boolean {
        when (keyAction.type) {
            ACTION_TYPE_NONE -> return false
            ACTION_TYPE_BUTTON -> TiltAccessibilityService.doAction(keyAction.action)
            ACTION_TYPE_MEDIA -> {
                val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val keyEvent1 = KeyEvent(KeyEvent.ACTION_DOWN, keyAction.action)
                val keyEvent2 = KeyEvent(KeyEvent.ACTION_UP, keyAction.action)
                mAudioManager.dispatchMediaKeyEvent(keyEvent1)
                mAudioManager.dispatchMediaKeyEvent(keyEvent2)
            }
            ACTION_TYPE_SWIPE -> {
                Log.i(
                    "Retrun Swipe",
                    TiltAccessibilityService.mouseDraw(keyAction.action)
                        .toString() + " " + keyAction.action
                )
            }
        }
        return true
    }

    fun changeListenerState(state: Int, rightHand: Boolean = false) {
        if (listenerState == state)
            return
        if (TiltAccessibilityService.isGesturing())
            TiltAccessibilityService.halt()
        val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (state == LISTENER_IDLE) {
            sensorManager.unregisterListener(sensorListener)
        } else {
            if (listenerState == LISTENER_IDLE)
                sensorManager.registerListener(
                    sensorListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                    SensorManager.SENSOR_DELAY_GAME
                )
            sensorListener.initBase(state, rightHand)
        }
        listenerState = state
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        changeListenerState(LISTENER_IDLE)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        changeListenerState(LISTENER_IDLE)
        stopForeground(true); // Foreground service 종료

        val wm: WindowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        for (i in 0..1)
            if (overlayView[i] != null)
                wm.removeView(overlayView[i]); // View 초기화
    }

}
