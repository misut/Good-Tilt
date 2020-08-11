package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.NotificationCompat


class EventService : Service() {
    var overlayView : View? = null
    var keyEvent = KeyAction.HOME
    private val sensorListener = MisutListener(null,::onActionOccur)
    private lateinit var sensorManager: SensorManager

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        changeListenerState(true)

        val wm : WindowManager =  getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val inflate =
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,  // Android O 이상인 경우 TYPE_APPLICATION_OVERLAY 로 설정
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        val mView = inflate.inflate(R.layout.view_overlay, null)

        val btn_img = mView.findViewById<LinearLayout>(R.id.testArea)
        btn_img.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(view: View?, motionEvent: MotionEvent): Boolean {
                generateEvent(keyEvent.key)
                return false
            }
        })
        wm.addView(mView, params) // 윈도우에 layout 을 추가 한다.
        overlayView = mView
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val key= intent?.getStringExtra("keyEvent")
        if(key != null)
            keyEvent = KeyAction.valueOf(key)

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
                .setContentText(keyEvent.str(this) + "버튼 기능 작동중...")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT).build()
            startForeground(1, notification)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun onActionOccur(index : Int) {
        Toast.makeText(this,index.toString(), Toast.LENGTH_SHORT).show()
        //index에 따라서 generateEvent(KEYCODE_BACK) 등등을 여기 넣는다
    }

    fun generateEvent(keyCode: Int) : Boolean{
        when (keyCode) {
            KeyEvent.KEYCODE_BACK -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_BACK)
            KeyEvent.KEYCODE_HOME -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_HOME)
            KeyEvent.KEYCODE_APP_SWITCH -> TiltAccessibilityService.doAction(AccessibilityService.GLOBAL_ACTION_RECENTS)
            else -> { // Audio Key
                val mAudioManager = getSystemService(AUDIO_SERVICE) as AudioManager
                val keyEvent1 = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
                val keyEvent2 = KeyEvent(KeyEvent.ACTION_UP, keyCode)
                mAudioManager.dispatchMediaKeyEvent(keyEvent1)
                mAudioManager.dispatchMediaKeyEvent(keyEvent2)
            }
        }
        return true
    }

    fun changeListenerState(state : Boolean){
        if(state){
            sensorManager.registerListener(sensorListener,
                sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL
            )
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
