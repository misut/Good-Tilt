package com.goodtilt.goodtilt

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.Sensor
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {
    lateinit var listener : SharedPreferences.OnSharedPreferenceChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        listener = SharedPreferences.OnSharedPreferenceChangeListener{pref, string ->
            updateOverlay(pref)
        }
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        updateOverlay(preference)
    }

    fun updateOverlay(pref : SharedPreferences) {
        overlayLeft.layoutParams.width = (resources.displayMetrics.widthPixels / 1000F * pref.getInt("area_width", 50)).toInt()
        overlayLeft.layoutParams.height = (resources.displayMetrics.heightPixels / 1000F * pref.getInt("area_height", 500)).toInt()
        overlayLeft.y = (resources.displayMetrics.heightPixels - overlayLeft.layoutParams.height) / 1000F * pref.getInt("area_vertical_position", 500)
        overlayRight.layoutParams.width = overlayLeft.layoutParams.width
        overlayRight.layoutParams.height = overlayLeft.layoutParams.height
        overlayRight.y = overlayLeft.y
        overlayLeft.requestLayout()
        overlayRight.requestLayout()
    }


    override fun onPause() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        preference.unregisterOnSharedPreferenceChangeListener(listener)
        super.onPause()
    }

    override fun onResume() {
        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        preference.registerOnSharedPreferenceChangeListener(listener)
        super.onResume()
    }
}

