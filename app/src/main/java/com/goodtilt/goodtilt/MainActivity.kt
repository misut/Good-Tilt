package com.goodtilt.goodtilt

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.goodtilt.goodtilt.const.KeyAction
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList


const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100
const val ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE = 101

const val MODE_TEST = 0
const val MODE_SERVICE = 1

class MainActivity : AppCompatActivity() {
    private var listening = true
    private var serviceRunning = false

    private lateinit var sensorManager: SensorManager
    private lateinit var sensorRott: Sensor
    private lateinit var sensorGyro: Sensor
    private val sensorListener = MisutListener(::printResult, ::printAction)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorListener.applyPreference(this)

        sensorRott = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        sensorGyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        val items = ArrayList<String>()
        for (act in KeyAction.values()) {
            items.add(act.str(this))
        }
        checkOverlayPermission()
        checkAccessibilityPermissions()

        overlayButton.setOnClickListener {
            if (!checkOverlayPermission()) {
                val uri: Uri = Uri.fromParts("package", packageName, null)
                val intent =
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            }
        }

        accessButton.setOnClickListener {
            if (!checkAccessibilityPermissions()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivityForResult(intent, ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE)
            }
        }

        serviceButton.setOnClickListener {
            if (!serviceRunning) {
                if (!checkOverlayPermission()) {
                    Toast.makeText(this, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    overlayButton.isEnabled = true
                    return@setOnClickListener
                }

                if (!checkAccessibilityPermissions()) {
                    Toast.makeText(this, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(this, EventService::class.java)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                serviceButton.text = "서비스 종료"
            } else {
                val intent = Intent(this, EventService::class.java)
                stopService(intent)
                serviceButton.text = "서비스 시작"
            }
            serviceRunning = !serviceRunning
        }

        modeButton.setOnClickListener {
            listening = !listening
            changeListenerState(listening)
            if (listening)
                textViewMod.text = "Listening"
            else
                textViewMod.text = "Stopped"
        }

        positionButton.setOnClickListener {
            tiltView.setDefaultPosition()
        }

        setSupportActionBar(mainToolbar)
    }

    fun printResult(evt : SensorEvent) {
        when(evt.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                tiltView.onSensorEvent(evt)
                textAccelerX.text = evt.values[0].toString();
                textAccelerY.text = evt.values[1].toString();
                textAccelerZ.text = evt.values[2].toString();
            }

            Sensor.TYPE_GYROSCOPE -> {
                textGyroX.text = evt.values[0].toString();
                textGyroY.text = evt.values[1].toString();
                textGyroZ.text = evt.values[2].toString();
            }
        }
    }

    fun printAction(action: Int) {
        val log = logList.text.toString()
        logList.text = String.format("%d 액션 발생\n%s", action, log)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menuSetting->{
                startActivity(Intent(this, SettingActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        changeListenerState(listening)
    }

    override fun onPause() {
        super.onPause()
        changeListenerState(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        changeListenerState(false)
    }

    fun changeListenerState(state: Boolean) {
        if (state) {
            //자이로스코프, 회전벡터 등록
            sensorManager.registerListener(sensorListener, sensorRott, SensorManager.SENSOR_DELAY_GAME)
            sensorManager.registerListener(sensorListener, sensorGyro, SensorManager.SENSOR_DELAY_GAME)
        } else {
            sensorManager.unregisterListener(sensorListener)
        }
    }

    fun checkAccessibilityPermissions(): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)   // 마시멜로우 이상일 경우
            result = (Settings.Secure.getInt(
                contentResolver,
                android.provider.Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1)
        accessButton.isEnabled = !result
        return result
    }

    fun checkOverlayPermission(): Boolean {
        var result = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)   // 마시멜로우 이상일 경우
            result = Settings.canDrawOverlays(this)
        overlayButton.isEnabled = !result
        return result
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (!checkOverlayPermission())
                    Toast.makeText(this, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
            }
            ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                if (!checkAccessibilityPermissions()) {
                    Toast.makeText(this, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}