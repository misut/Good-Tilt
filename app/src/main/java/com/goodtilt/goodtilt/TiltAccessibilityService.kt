package com.goodtilt.goodtilt

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.gesture.GestureStore
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.RequiresApi


class TiltAccessibilityService : AccessibilityService() {

    companion object {
        private const val GESTURE_IDLE = -1
        private const val GESTURE_LEFT = 0
        private const val GESTURE_RIGHT = 1
        private const val GESTURE_UP = 2
        private const val GESTURE_DOWN = 3

        var instance: TiltAccessibilityService? = null

        fun doAction(action: Int) {
            instance?.performGlobalAction(action)
        }

        //Int 0: left, 1: right, 2: up, 3:down -1:none
        @RequiresApi(Build.VERSION_CODES.O)
        fun mouseDraw(dir : Int) : Boolean{
            /*
            if (instance?.gestureState == GESTURE_IDLE){
                instance?.dispatchGesture(
                    GestureDescription.Builder().addStroke(instance?.description[instance?.gestureState]!!).build(),
                    this,
                    null
                )
            } else {
                instance?.gestureNextState = dir;
            }

            */
            instance?.gestureCount = 300
            instance?.gestureState = dir
            instance?.gestureEvent()
            return false
        }
    }

    private var gestureState = GESTURE_IDLE
    private var gestureCount = 0
    private var description = arrayOfNulls<GestureDescription.StrokeDescription>(4)
    val gestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            gestureCount--
            Log.i("GESTURE_COMPLETE", gestureCount.toString())
            if(gestureCount <= 0) {
                Log.i("TOUCH END", gestureState.toString());
                gestureCount = 0
                if(gestureState == GESTURE_IDLE)
                    return
                gestureState = GESTURE_IDLE
                gestureEvent(false)
                return
            }
            gestureEvent()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onCancelled(gestureDescription: GestureDescription?) {
            Log.i("GESTURE_FAILED", gestureCount.toString())
            if (gestureState != GESTURE_IDLE)
                gestureEvent()
            else
                return super.onCancelled(gestureDescription)
        }
    }

    private var swipePosX = 0.0F
    private var swipePosY = 0.0F

    fun gestureEvent(willContinue: Boolean = true) {
        val path = Path()
        path.moveTo(swipePosX, swipePosY)
        if (willContinue)
            path.lineTo(swipePosX , swipePosY - 30.0f)//핸드폰마다 다를지도?
        val description = GestureDescription.StrokeDescription(path, 0, 1, willContinue)
        dispatchGesture(
            GestureDescription.Builder().addStroke(description).build(), instance?.gestureResultCallback, null)
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

    }

}
