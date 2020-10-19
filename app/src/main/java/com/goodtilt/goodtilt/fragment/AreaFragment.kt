package com.goodtilt.goodtilt.fragment
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.goodtilt.goodtilt.ManualActivity
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.frag_area.view.*

class AreaFragment : Fragment(){
    lateinit var preference : SharedPreferences
    inner class SeekBarListener(val pref : String) : SeekBar.OnSeekBarChangeListener{
        override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
            with(preference.edit()) {
                putInt(pref, p1)
                commit()
            }
            updateOverlay()
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {
        }

        override fun onStopTrackingTouch(p0: SeekBar?) {
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun updateOverlay() {
        overlayLeft.updateFromPreference(preference)
        overlayRight.updateFromPreference(preference)
    }

    override fun onStart() {
        updateOverlay()
        super.onStart()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.frag_area, container, false)
        val manualActivity = activity as ManualActivity
        rootView.apply {
            next.setOnClickListener(manualActivity.nextListener)
            prev.setOnClickListener(manualActivity.prevListener)
            seekBarWidth.setOnSeekBarChangeListener(SeekBarListener("area_width"))
            seekBarHeight.setOnSeekBarChangeListener(SeekBarListener("area_height"))
            seekBarPosition.setOnSeekBarChangeListener(SeekBarListener("area_vertical_position"))
        }
        preference = PreferenceManager.getDefaultSharedPreferences(inflater.context)
        return rootView
    }
}