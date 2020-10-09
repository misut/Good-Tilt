package com.goodtilt.goodtilt.source

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.goodtilt.goodtilt.R

class SettingFragment : PreferenceFragmentCompat(){
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_action, rootKey)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preferenceManager.apply {
            val provider = ListPreference.SimpleSummaryProvider.getInstance()
            findPreference<ListPreference>("tilt_left")?.summaryProvider = provider
            findPreference<ListPreference>("tilt_right")?.summaryProvider = provider
            findPreference<ListPreference>("tilt_up")?.summaryProvider = provider
            findPreference<ListPreference>("tilt_down")?.summaryProvider = provider
            findPreference<ListPreference>("swipe_tilt_left")?.summaryProvider = provider
            findPreference<ListPreference>("swipe_tilt_right")?.summaryProvider = provider
            findPreference<ListPreference>("swipe_tilt_up")?.summaryProvider = provider
            findPreference<ListPreference>("swipe_tilt_down")?.summaryProvider = provider
        }


        return super.onCreateView(inflater, container, savedInstanceState)
    }


}