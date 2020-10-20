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
import kotlinx.android.synthetic.main.frag_area.*
import kotlinx.android.synthetic.main.frag_area.view.*
import kotlinx.android.synthetic.main.frag_area.view.next
import kotlinx.android.synthetic.main.frag_area.view.prev

class AreaFragment(private val isManual : Boolean = true) : Fragment(){
    lateinit var preference : SharedPreferences
    lateinit var listener : SharedPreferences.OnSharedPreferenceChangeListener

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
        rootView.apply {
            if(isManual) {
                val manualActivity = activity as ManualActivity
                next.setOnClickListener(manualActivity.nextListener)
                prev.setOnClickListener(manualActivity.prevListener)
            } else {
                next.visibility = View.GONE
                prev.visibility = View.GONE
            }
        }
        preference = PreferenceManager.getDefaultSharedPreferences(inflater.context)
        listener = SharedPreferences.OnSharedPreferenceChangeListener{pref, string ->
            updateOverlay()
        }
        return rootView
    }

    override fun onPause() {
        preference.unregisterOnSharedPreferenceChangeListener(listener)
        super.onPause()
    }

    override fun onResume() {
        preference.registerOnSharedPreferenceChangeListener(listener)
        super.onResume()
    }
}