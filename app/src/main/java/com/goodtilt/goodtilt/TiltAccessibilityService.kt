package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.gesture.GestureStore
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.annotation.RequiresApi
import com.goodtilt.goodtilt.const.ACTION_HALT
import com.goodtilt.goodtilt.const.ACTION_UP


class TiltAccessibilityService : AccessibilityService() {

    companion object {
        private const val GESTURE_IDLE = -1
        private const val GESTURE_UP = 0
        private const val GESTURE_DOWN = 1

        private const val swipeSize = 60.0f;

        var instance: TiltAccessibilityService? = null

        fun doAction(action: Int) {
            instance?.performGlobalAction(action)
        }

        fun isGesturing() : Boolean {
            return instance?.gestureState != GESTURE_IDLE
        }

        fun halt(){
            instance?.haltGesture()
        }

        fun touchOutside() : Boolean{
            instance?.apply{
                touchCount++
                //Log.i("OUTSIDE!", "$gestureCount $touchCount")
                if (gestureCount < touchCount) {
                    haltGesture()
                    return true
                }
            }
            return false
        }

        //Int 0: left, 1: right, 2: up, 3:down -1:none
        fun mouseDraw(dir : Int) : Boolean{
            instance?.apply{
                if (dir == ACTION_HALT) {
                    haltGesture()
                    return true
                }
                val isIdle = gestureState == GESTURE_IDLE
                if (isIdle) {
                    gestureState = dir
                    if((lastInfo != null && lastInfo!!.performAction(gestureState))|| swipeNodeInfo(rootInActiveWindow)){
                        gestureState = GESTURE_IDLE
                        Log.i("GESTURE", "NODEINFO SCROLL")
                    } else {
                        Log.i("GESTURE", "DISPATCH SCROLL")
                        gestureCount = 0
                        touchCount = 0
                        gestureEvent()
                    }
                }
            }
            return false
        }
    }

    private var gestureState = GESTURE_IDLE
    private var gestureCount = 0
    private var touchCount = 0
    private var lastInfo : AccessibilityNodeInfo? = null

    val gestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            if(gestureState != GESTURE_IDLE) {
                gestureEvent()
            }
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            //Log.i("GESTURE_FAILED", gestureCount.toString())
            gestureCount = touchCount
            if (gestureState != GESTURE_IDLE)
                gestureEvent()
        }
    }

    private var swipePosX = 0.0F
    private var swipePosY = 0.0F

    fun haltGesture(){
        gestureCount = 0
        touchCount = 0
        gestureState = GESTURE_IDLE
    }

    fun gestureEvent() {
        val path = Path()
        path.moveTo(swipePosX, swipePosY)
        if (gestureState == GESTURE_UP)
            path.rLineTo(0F, -swipeSize )
        else
            path.rLineTo(0F, swipeSize)
        //핸드폰마다 다를지도?
        val description = GestureDescription.StrokeDescription(path, 5 , 1, false)
        gestureCount++
        //Log.i("GESTURE START", "$gestureCount $touchCount")
        dispatchGesture(
            GestureDescription.Builder().addStroke(description).build(), instance?.gestureResultCallback, null)
    }

    fun swipeNodeInfo(info: AccessibilityNodeInfo?) : Boolean{
        if (info == null) return false
        if(info.isScrollable()){
            info.performAction(gestureState)
            return true
        }

        for (i in 0 until info.childCount) {
            if (swipeNodeInfo(info.getChild(i)))
                return true
        }
        return false
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
        swipePosX = resources.displayMetrics.widthPixels / 2.0f
        swipePosY = resources.displayMetrics.heightPixels / 2.0f
        instance = this
    }

    override fun onInterrupt() {
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        when (event.eventType) {
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                lastInfo = event.source
            }
            else -> {
                lastInfo = null
            }
        }
    }

}
