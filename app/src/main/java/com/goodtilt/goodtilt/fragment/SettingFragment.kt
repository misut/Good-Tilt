package com.goodtilt.goodtilt.fragment

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.goodtilt.goodtilt.R
import kotlinx.android.synthetic.main.item_app.*


class SettingFragment() : PreferenceFragmentCompat(){
    lateinit var listener : SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var alertAdapter : ArrayAdapter<ResolveInfo>

    private val actionString = arrayOf("tilt_left", "tilt_right", "tilt_up", "tilt_down", "swipe_tilt_left", "swipe_tilt_right", "swipe_tilt_up", "swipe_tilt_down")

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

        val pm = inflater?.context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        val pkgAppsList = activity?.packageManager?.queryIntentActivities(mainIntent, PackageManager.MATCH_DEFAULT_ONLY)!!
        alertAdapter = object : ArrayAdapter<ResolveInfo>(
            inflater.context, R.layout.item_app, pkgAppsList
        ) {
            lateinit var holder: ViewHolder
            inner class ViewHolder(var icon : ImageView, var title : TextView)


            override fun getView(position: Int, v: View?, parent: ViewGroup): View {
                var convertView = v
                if (v == null) {
                    convertView = inflater.inflate(R.layout.item_app, null, false)
                    holder = ViewHolder(convertView?.findViewById(R.id.itemIcon)!!, convertView?.findViewById(R.id.itemTitle)!!)
                    convertView!!.tag = holder
                } else {
                    // view already defined, retrieve view holder
                    holder = convertView!!.tag as ViewHolder
                }
                holder.title.setText(pkgAppsList.get(position).loadLabel(pm).toString())
                holder.icon.setImageDrawable(pkgAppsList.get(position).loadIcon(pm))
                return convertView!!
            }
        }


        listener = SharedPreferences.OnSharedPreferenceChangeListener{ pref, key ->
            if (key in actionString && pref.getString(key, "NONE").equals("LAUNCH_APP")!!) {
                AlertDialog.Builder(context).
                setTitle("앱을 선택해주세요").
                setAdapter(alertAdapter) { _, i ->
                    pref.edit().putString("app_" + key, alertAdapter.getItem(i)?.activityInfo?.packageName).commit()
                }.setOnCancelListener {
                    pref.edit().putString("app_" + key, "").putString(key,"NONE").commit()
                }.show()
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }



    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences
            .registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        preferenceScreen.sharedPreferences
            .unregisterOnSharedPreferenceChangeListener(listener)
    }



}