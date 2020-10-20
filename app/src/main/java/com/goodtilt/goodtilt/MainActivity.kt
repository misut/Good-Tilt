package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.goodtilt.goodtilt.fragment.AreaFragment
import com.goodtilt.goodtilt.fragment.HomeFragment
import com.goodtilt.goodtilt.fragment.TiltFragment
import com.goodtilt.goodtilt.R
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.menu_switch.view.*


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
    }


    private fun checkAccessibilityPermissions(service: Class<out AccessibilityService>): Boolean {
        var am: AccessibilityManager = applicationContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        var enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for(enabledService in enabledServices) {
            var serviceInfo = enabledService.resolveInfo.serviceInfo
            if(serviceInfo.packageName.equals(applicationContext.packageName) && serviceInfo.name.equals(service.name))
                return true
        }
        return false
    }

    private fun checkOverlayPermission(): Boolean {
        return Settings.canDrawOverlays(this)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        val item = menu?.findItem(R.id.menu_switch_item)!!
        item.setActionView(R.layout.menu_switch)
        item.actionView.serviceSwitch.setOnCheckedChangeListener { compoundButton, checked ->
            if (checked) {
                if (!checkOverlayPermission()) {
                    Toast.makeText(this, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    compoundButton.isChecked = false
                    val uri: Uri = Uri.fromParts("package", packageName, null)
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)
                    startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                    return@setOnCheckedChangeListener
                }

                if (!checkAccessibilityPermissions(TiltAccessibilityService::class.java)) {
                    Toast.makeText(this, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                    compoundButton.isChecked = false
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivityForResult(
                        intent,
                        ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE
                    )
                    return@setOnCheckedChangeListener
                }
                compoundButton.setText("사용 중")
                startForegroundService(Intent(this, EventService::class.java))
            } else {
                val intent = Intent(this, EventService::class.java)
                compoundButton.setText("사용 중지")
                stopService(intent)
            }
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE -> {
                if (!checkOverlayPermission())
                    Toast.makeText(this, "오버레이 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
            }
            ACTION_MANAGE_ACCESSIBILITY_PERMISSION_REQUEST_CODE -> {
                if (!checkAccessibilityPermissions(TiltAccessibilityService::class.java)) {
                    Toast.makeText(this, "접근성 권한을 허용해주세요", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}