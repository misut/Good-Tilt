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
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goodtilt.goodtilt.const.KeyAction
import com.goodtilt.goodtilt.fragment.*
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_manual.*
import kotlin.collections.ArrayList


const val ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 100
const val ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE = 101

const val MODE_TEST = 0
const val MODE_SERVICE = 1

class MainActivity : AppCompatActivity() {
    private var listening = true
    private var serviceRunning = false

    inner class MainAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            when(position) {
                0 -> return AreaFragment(false)
                1 -> return HomeFragment()
                2 -> return TiltFragment(false)
            }
            return Fragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager.adapter = MainAdapter(this)
        viewPager.setCurrentItem(1, false)
        TabLayoutMediator(bottomTap, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = resources.getText(R.string.setting_area)
                1 -> tab.text = resources.getText(R.string.setting_general)
                2 -> tab.text = resources.getText(R.string.setting_sensitivity)
            }
        }.attach()

        setSupportActionBar(mainToolbar)

        serviceSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                if (!checkOverlayPermission()) {
                    Toast.makeText(this, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    compoundButton.isChecked = false
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                    return@setOnCheckedChangeListener
                }

                if (!checkAccessibilityPermissions()) {
                    Toast.makeText(this, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    compoundButton.isChecked = false
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivityForResult(intent, ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE)
                    return@setOnCheckedChangeListener
                }
                startForegroundService(intent)
            } else {
                val intent = Intent(this, EventService::class.java)
                stopService(intent)
            }
        }
    }


    private fun checkAccessibilityPermissions(): Boolean {
        return (Settings.Secure.getInt(
                contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            ) == 1)
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }

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