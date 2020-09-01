package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.const.KeyAction

const val DOUBLE_CLICK_DELAY = 300

class EventService : Service() {
    lateinit var overlayView : View
    var actionList = arrayOf(KeyAction.NONE, KeyAction.NONE, KeyAction.NONE, KeyAction.NONE)
    private var btnPressTime = 0L
    private var placementState = false

    private val sensorListener = MisutListener(null, ::onActionOccur)
    private lateinit var sensorManager: SensorManager
    private var swipePosX = 0.0f
    private var swipePosY = 0.0f
    private var isVibrate = false

    private var dX = 0.0f
    private var dY = 0.0f

    private lateinit var wm : WindowManager
    var params = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,  // Android O 이상인 경우 TYPE_APPLICATION_OVERLAY 로 설정
        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    )

    override fun onCreate() {
        super.onCreate()

        swipePosX = resources.displayMetrics.widthPixels / 2.0f
        swipePosY = resources.displayMetrics.heightPixels / 2.0f

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorListener.applyPreference(this)
        changeListenerState(true)

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            getString("tilt_left", "NONE")?.let { actionList[0] = KeyAction.valueOf(it) }
            getString("tilt_right", "NONE")?.let { actionList[1] = KeyAction.valueOf(it) }
            getString("tilt_up", "NONE")?.let { actionList[2] = KeyAction.valueOf(it) }
            getString("tilt_down", "NONE")?.let { actionList[3] = KeyAction.valueOf(it) }
            getInt("overlay_x", swipePosX.toInt())?.let { params.x = it}
            getInt("overlay_y", swipePosY.toInt())?.let { params.y = it}
            getBoolean("vibration", false)?.let {isVibrate = it}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }

        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val inflate = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        params.gravity = Gravity.TOP or Gravity.START
        overlayView = inflate.inflate(R.layout.view_overlay, null)

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        var vibrateEffect : VibrationEffect? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrateEffect = VibrationEffect.createOneShot(100, 50)
        }

        val btn_img = overlayView.findViewById<LinearLayout>(R.id.testArea)

        btn_img.setOnTouchListener(object : View.OnTouchListener {
            fun vibrate() {
                if (!isVibrate) return
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(vibrateEffect)
                } else {
                    vibrator.vibrate(100)
                }
            }

            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    //Double Click Occur
                    if (System.currentTimeMillis() < btnPressTime + DOUBLE_CLICK_DELAY) {
                        placementState = true
                        dX = params.x - motionEvent.rawX
                        dY = params.y - motionEvent.rawY
                    } else { //Normal Press
                        btnPressTime = System.currentTimeMillis()
                        vibrate()
                        changeListenerState(true)
                    }
                    //generateEvent(actionList[0])
                } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                    vibrate()
                    if(placementState) {
                        placementState = false
                        val editor = PreferenceManager.getDefaultSharedPreferences(applicationContext).edit()
                        editor.putInt("overlay_x", params.x)
                        editor.putInt("overlay_y", params.y)
                        editor.commit()
                    } else {
                        changeListenerState(false)
                    }
                } else if (placementState && motionEvent.action == MotionEvent.ACTION_MOVE) {
                    params.x = (motionEvent.rawX + dX).toInt()
                    params.y = (motionEvent.rawY + dY).toInt()
                    wm.updateViewLayout(overlayView, params)
                }
                return true
            }
        })
        wm.addView(overlayView, params) // 윈도우에 layout 을 추가 한다.
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun onActionOccur(index : Int) {
        generateEvent(actionList[index])
    }

    fun generateEvent(keyAction: KeyAction) : Boolean{
        when (keyAction) {
            KeyAction.BACK -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_BACK)
            KeyAction.HOME -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_HOME)
            KeyAction.RECENT -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            KeyAction.SWIPE_DOWN, KeyAction.SWIPE_UP , KeyAction.SWIPE_LEFT , KeyAction.SWIPE_RIGHT -> {
                //아직 아래방향밖에 안만듦
                val path = Path()
                path.moveTo(swipePosX, swipePosY)
                path.lineTo(swipePosX, 0.0f)
                Log.i("Retrun Swipe", TiltAccessibilityService.mouseDraw(path).toString())
            }
            else -> { // Audio Key
                val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val keyEvent1 = KeyEvent(KeyEvent.ACTION_DOWN, keyAction.action)
                val keyEvent2 = KeyEvent(KeyEvent.ACTION_UP, keyAction.action)
                mAudioManager.dispatchMediaKeyEvent(keyEvent1)
                mAudioManager.dispatchMediaKeyEvent(keyEvent2)
            }
        }
        return true
    }

    fun changeListenerState(state : Boolean){
        if(state){
            sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
                SensorManager.SENSOR_DELAY_GAME
            )
            sensorManager.registerListener(
                sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_GAME
            )
            sensorListener.initBase()
        } else {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    override fun onRebind(intent: Intent?) {
        changeListenerState(true)
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        changeListenerState(false)
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        changeListenerState(false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true); // Foreground service 종료
        }

        if(overlayView != null) {
            val wm : WindowManager =  getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.removeView(overlayView); // View 초기화
        }
    }

}
