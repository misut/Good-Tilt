package com.goodtilt.goodtilt.fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.goodtilt.goodtilt.R


class PrefActionFragment() : PreferenceFragmentCompat(){
    lateinit var listener : SharedPreferences.OnSharedPreferenceChangeListener
    lateinit var alertAdapter : ArrayAdapter<ResolveInfo>

    private val actionString = arrayOf(
        "tilt_left",
        "tilt_right",
        "tilt_up",
        "tilt_down",
        "swipe_tilt_left",
        "swipe_tilt_right",
        "swipe_tilt_up",
        "swipe_tilt_down"
    )

    inner class PrefProvider : Preference.SummaryProvider<ListPreference> {
        override fun provideSummary(preference: ListPreference?): CharSequence {
            if (preference?.value?.equals("LAUNCH_APP_CONFIG")!!){
                val packageName = preferenceManager.sharedPreferences.getString("app_" + preference?.key, "") as String
                context?.packageManager?.apply {
                    val info = getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                    val name = getApplicationLabel(info)
                    return resources.getString(R.string.LAUNCH_APP) + "(" + name + ")"
                }
                return ""
            }
            else
                return ListPreference.SimpleSummaryProvider.getInstance().provideSummary(preference)
        }
    }
    val provider = PrefProvider()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preference_action, rootKey)
        updatePreference()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val pm = inflater.context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        val pkgAppsList = activity?.packageManager?.queryIntentActivities(
            mainIntent,
            PackageManager.MATCH_ALL
        )!!
        alertAdapter = object : ArrayAdapter<ResolveInfo>(
            inflater.context, R.layout.item_app, pkgAppsList
        ) {
            lateinit var holder: ViewHolder
            inner class ViewHolder(var icon: ImageView, var title: TextView)


            override fun getView(position: Int, v: View?, parent: ViewGroup): View {
                var convertView = v
                if (v == null) {
                    convertView = inflater.inflate(R.layout.item_app, null, false)
                    holder = ViewHolder(
                        convertView?.findViewById(R.id.itemIcon)!!, convertView?.findViewById(
                            R.id.itemTitle
                        )!!
                    )
                    convertView!!.tag = holder
                } else {
                    // view already defined, retrieve view holder
                    holder = convertView!!.tag as ViewHolder
                }
                holder.title.setText(pkgAppsList.get(position).loadLabel(pm).toString())
                holder.icon.setImageDrawable(pkgAppsList.get(position).loadIcon(pm))
                return convertView
            }
        }


        listener = SharedPreferences.OnSharedPreferenceChangeListener{ pref, key ->
            if (key in actionString && pref.getString(key, "NONE").equals("LAUNCH_APP")) {
                pref.edit().putString(key, "LAUNCH_APP_CONFIG").commit()
                AlertDialog.Builder(context).
                setTitle(resources.getString(R.string.LAUNCH_APP_CONFIG)).
                setAdapter(alertAdapter) { _, i ->
                    pref.edit().putString(
                        "app_" + key,
                        alertAdapter.getItem(i)?.activityInfo?.packageName
                    ).commit()
                    setPreferenceScreen(null);
                    addPreferencesFromResource(R.xml.preference_action)
                    updatePreference()

                }.setOnCancelListener {
                    pref.edit().putString("app_" + key, "").putString(key, "NONE").commit()
                }.show()
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun updatePreference(){
        actionString.forEach {findPreference<ListPreference>(it)?.summaryProvider = provider}
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