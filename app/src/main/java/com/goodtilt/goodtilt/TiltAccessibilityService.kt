package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.input.InputManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat
import java.lang.reflect.Method


class TiltAccessibilityService : AccessibilityService() {
    var mView: View? = null

    companion object {
        var instance: TiltAccessibilityService? = null

        fun doAction(action: Int) {
            instance?.performGlobalAction(action)
        }

    }

    override fun onServiceConnected() {
        /*
        val info  = AccessibilityServiceInfo()
        info.apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED
            packageNames = arrayOf("com.example.android.myFirstApp", "com.example.android.mySecondApp")
            flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
            feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
            notificationTimeout = 100
        }
        this.serviceInfo = info
        */
        instance = this
        Log.i("dd", "dddd")
    }


    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

}
