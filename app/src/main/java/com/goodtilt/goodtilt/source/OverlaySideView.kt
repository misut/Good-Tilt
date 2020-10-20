package com.goodtilt.goodtilt.source

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import android.widget.LinearLayout

class OverlaySideView: LinearLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs : AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs : AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    fun updateFromPreference(pref : SharedPreferences){
        layoutParams.width = (resources.displayMetrics.widthPixels / 1000F * pref.getInt("area_width", 50)).toInt()
        layoutParams.height = (resources.displayMetrics.heightPixels / 1000F * pref.getInt("area_height", 500)).toInt()
        y = (resources.displayMetrics.heightPixels - layoutParams.height) / 1000F * pref.getInt("area_vertical_position", 500)
        requestLayout()
    }
}