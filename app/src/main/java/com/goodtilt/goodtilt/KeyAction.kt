package com.goodtilt.goodtilt

import android.content.Context
import android.view.KeyEvent

enum class KeyAction(val key: Int) {
    BACK(KeyEvent.KEYCODE_BACK),
    HOME(KeyEvent.KEYCODE_HOME),
    RECENT(KeyEvent.KEYCODE_APP_SWITCH),
    PLAY_PAUSE(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE),
    NEXT(KeyEvent.KEYCODE_MEDIA_NEXT);

    fun str(context: Context): String {
        val id = context.resources.getIdentifier(this.name, "string", context.packageName)
        return context.getString(id)
    }
}