package com.goodtilt.goodtilt.const

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.view.KeyEvent

const val ACTION_TYPE_NONE = 0
const val ACTION_TYPE_BUTTON = 1
const val ACTION_TYPE_MEDIA = 2
const val ACTION_TYPE_SWIPE = 3


enum class KeyAction(val type : Int, val action: Int) {
    NONE(ACTION_TYPE_NONE, 0),
    BACK(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_BACK),
    HOME(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_HOME),
    RECENT(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_RECENTS),
    NOTIFY(ACTION_TYPE_BUTTON, AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS),

    NEXT(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_NEXT),
    PLAY_PAUSE(ACTION_TYPE_MEDIA, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),

    SWIPE_UP(ACTION_TYPE_SWIPE, 1000),
    SWIPE_DOWN(ACTION_TYPE_SWIPE, 1001),
    SWIPE_LEFT(ACTION_TYPE_SWIPE, 1002),
    SWIPE_RIGHT(ACTION_TYPE_SWIPE, 1003);

    fun str(context: Context): String {
        val id = context.resources.getIdentifier(this.name, "string", context.packageName)
        return context.getString(id)
    }
}