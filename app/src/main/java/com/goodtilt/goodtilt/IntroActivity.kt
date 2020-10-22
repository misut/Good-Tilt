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
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.const.KeyAction
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class IntroActivity : AppCompatActivity() {
    var configured = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        PreferenceManager.getDefaultSharedPreferences(this).apply {
            configured = getBoolean("configured", false)
        }

        if (configured) {
            Thread( Runnable {
                Thread.sleep(100)
                startActivity(Intent(this, MainActivity::class.java))
            }).start()
        } else {
            Thread( Runnable {
                Thread.sleep(100)
                startActivity(Intent(this, ManualActivity::class.java))
            }).start()
        }
    }
}