package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent


class TiltAccessibilityService : AccessibilityService() {
    var mView: View? = null

    companion object {
        var instance: TiltAccessibilityService? = null

        fun doAction(action: Int) {
            instance?.performGlobalAction(action)
        }

        fun mouseDraw(path : Path){
            val description = GestureDescription.StrokeDescription(path, 0, 0)
            instance?.dispatchGesture(
                GestureDescription.Builder().addStroke(description).build(),
                object : GestureResultCallback() {
                },
                null
            )
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
