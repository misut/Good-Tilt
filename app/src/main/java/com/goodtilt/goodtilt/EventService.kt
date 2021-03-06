package com.goodtilt.goodtilt

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.const.*

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
    var appList = arrayOfNulls<String>(8)
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
        var isVibrate = false
        var isTransparent = false
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

            getString("app_tilt_left", "")?.let { appList[0] = it }
            getString("app_tilt_right", "")?.let { appList[1] = it }
            getString("app_tilt_up", "")?.let { appList[2] = it }
            getString("app_tilt_down", "")?.let { appList[3] = it }
            getString("app_swipe_tilt_left", "")?.let { appList[4] = it }
            getString("app_swipe_tilt_right", "")?.let { appList[5] = it }
            getString("app_swipe_tilt_up", "")?.let { appList[6] = it }
            getString("app_swipe_tilt_down", "")?.let { appList[7] = it }
            //getInt("overlay_x", swipePosX.toInt())?.let { params.x = it}
            //getInt("overlay_y", swipePosY.toInt())?.let { params.y = it}
            getBoolean("transparent", false).let { isTransparent = it }
            getBoolean("vibration", false).let { isVibrate = it }

            getInt("area_width", 50).let { area_width = it }
            getInt("area_height", 500).let { area_height = it }
            getInt("area_vertical_position", 500).let { area_vertical_pos = it }
        }

        val strId = getString(R.string.channel_id)
        val strTitle = getString(R.string.app_name)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var channel = notificationManager.getNotificationChannel(strId)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)
        if (channel == null) {
            channel = NotificationChannel(strId, strTitle, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }
        val notification: Notification = NotificationCompat.Builder(this, strId)
            .setSmallIcon(R.mipmap.good_tilt)
            .setContentTitle(resources.getString(R.string.notif_title))
            .setContentText(resources.getString(R.string.notif_content))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_MIN).build()
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
                        view.setBackgroundColor(resources.getColor(R.color.colorAccent, theme))
                    floatingView.setImageResource(R.drawable.ic_baseline_done_all_24)
                } else { //Normal Press
                    btnPressTime = System.currentTimeMillis()
                    changeListenerState(LISTENER_SINGLE, view == overlayView[1])
                    if (!isTransparent)
                        view.setBackgroundColor(resources.getColor(R.color.overlayClicked, theme))
                    floatingView.setImageResource(R.drawable.ic_baseline_done_24)
                }
                floatingParams.x = motionEvent.rawX.toInt()
                floatingParams.y = motionEvent.rawY.toInt() - 250
                wm.updateViewLayout(floatingView, floatingParams)
            } else if (motionEvent.action == MotionEvent.ACTION_UP || motionEvent.action == MotionEvent.ACTION_CANCEL) { //||
                changeListenerState(LISTENER_IDLE)
                floatingView.setImageResource(0)
                if (!isTransparent)
                    view.setBackgroundColor(resources.getColor(R.color.overlayDefault, theme))
            } else if (motionEvent.action == MotionEvent.ACTION_OUTSIDE && TiltAccessibilityService.isGesturing()) {
                if (TiltAccessibilityService.touchOutside()) {
                    vibrate()
                    changeListenerState(LISTENER_IDLE)
                    if (!isTransparent)
                        view.setBackgroundColor(resources.getColor(R.color.overlayDefault, theme))
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
        params.y =
            ((resources.displayMetrics.heightPixels - params.height) / 1000F * area_vertical_pos).toInt()


        floatingParams = WindowManager.LayoutParams(
            60, 60,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )


        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        for (i in 0..1) {
            overlayView[i] = inflate.inflate(R.layout.view_overlay, null)
            overlayView[i]?.setOnTouchListener(touchListener)
            if (!isTransparent)
                overlayView[i]?.setBackgroundColor(
                    resources.getColor(
                        R.color.overlayDefault,
                        theme
                    )
                )
        }
        floatingView = inflate.inflate(R.layout.singie_image, null) as ImageView

        params.gravity = Gravity.START or Gravity.TOP
        wm.addView(overlayView[0], params) // 윈도우에 layout 을 추가 한다.
        params.gravity = Gravity.END or Gravity.TOP
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        floatingParams.gravity = Gravity.START or Gravity.TOP
        wm.addView(overlayView[1], params)
        wm.addView(floatingView, floatingParams)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun onActionOccur(index: Int) {
        vibrate();
        val keyAction = actionList[index]
        when (keyAction.type) {
            ACTION_TYPE_NONE -> return
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
            ACTION_TYPE_APP -> {
                val intent = packageManager.getLaunchIntentForPackage(appList[index]!!)
                if (intent != null) {
                    startActivity(intent)
                }
            }
        }
    }

    fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrateEffect == null) return
        vibrator.vibrate(vibrateEffect)
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
