package com.goodtilt.goodtilt.const

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo

const val ACTION_TYPE_NONE = 0
const val ACTION_TYPE_BUTTON = 1
const val ACTION_TYPE_MEDIA = 2
const val ACTION_TYPE_SWIPE = 3
const val ACTION_TYPE_APP = 4

const val ACTION_LEFT = 0
const val ACTION_RIGHT = 1
const val ACTION_UP = 2
const val ACTION_DOWN = 3
const val ACTION_HALT = 4


enum class KeyAction(val type : Int, val action: Int) {
    NONE(ACTION_TYPE_NONE, 0),
    BACK(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_BACK),
    HOME(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_HOME),
    RECENT(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_RECENTS),
    NOTIFY(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS),
    SHOT(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT),
    SPLIT(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_TOGGLE_SPLIT_SCREEN),

    NEXT(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_NEXT),
    PREV(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_PREVIOUS),
    PLAY_PAUSE(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
    CLOSE(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_CLOSE),
    REWIND(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_REWIND),


    SWIPE_UP(ACTION_TYPE_SWIPE, AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD),
    SWIPE_DOWN(ACTION_TYPE_SWIPE, AccessibilityNodeInfo.ACTION_SCROLL_FORWARD),
    SWIPE_HALT(ACTION_TYPE_SWIPE, ACTION_HALT),
    LAUNCH_APP(ACTION_TYPE_APP, 0),
    LAUNCH_APP_CONFIG(ACTION_TYPE_APP, 1);

    fun str(context: Context): String {
        val id = context.resources.getIdentifier(this.name, "string", context.packageName)
        return context.getString(id)
    }
}