package com.goodtilt.goodtilt.const

import android.content.Context
import android.view.KeyEvent

val ACTION_TYPE_NONE = 0
val ACTION_TYPE_BUTTON = 1
val ACTION_TYPE_SWIPE = 2


enum class KeyAction(val type : Int, val action: Int) {
    NONE(ACTION_TYPE_NONE, 0),
    BACK(ACTION_TYPE_BUTTON, KeyEvent.KEYCODE_BACK),
    HOME(ACTION_TYPE_BUTTON, KeyEvent.KEYCODE_HOME),
    RECENT(ACTION_TYPE_BUTTON, KeyEvent.KEYCODE_APP_SWITCH),
    PLAY_PAUSE(ACTION_TYPE_BUTTON, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
    NEXT(ACTION_TYPE_BUTTON, KeyEvent.KEYCODE_MEDIA_NEXT),
    SWIPE_UP(ACTION_TYPE_SWIPE, 1000),
    SWIPE_DOWN(ACTION_TYPE_SWIPE, 1001),
    SWIPE_LEFT(ACTION_TYPE_SWIPE, 1002),
    SWIPE_RIGHT(ACTION_TYPE_SWIPE, 1003);

    fun str(context: Context): String {
        val id = context.resources.getIdentifier(this.name, "string", context.packageName)
        return context.getString(id)
    }
}