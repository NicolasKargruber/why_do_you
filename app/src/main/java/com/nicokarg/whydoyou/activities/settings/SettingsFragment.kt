package com.nicokarg.whydoyou.activities.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.main.LockAppsFragment

class SettingsFragment : PreferenceFragmentCompat() {
    private val logTag: String = "SettingsFragment"
    val prefSystemAppsKey:String by lazy { getString(R.string.key_system_apps_switch) }
    var prefListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Load the preferences from an XML resource
        setPreferencesFromResource(R.xml.preferences, rootKey)


        prefListener =
            SharedPreferences.OnSharedPreferenceChangeListener() { prefs, key ->
                Log.d(logTag, "pref changed: $key")
                if (key.equals(prefSystemAppsKey)) {
                    LockAppsFragment.systemAppsPrefChanged = true // tell LockAppsFragment that it changed
                    Log.d(logTag, "Show system apps pref changed")
                }
            }
//        val pref = PreferenceManager.getDefaultSharedPreferences(requireActivity())
//        pref.registerOnSharedPreferenceChangeListener (prefListener)
    }

    override fun onResume() {
        super.onResume()
        preferenceScreen.sharedPreferences!!.registerOnSharedPreferenceChangeListener(prefListener)
    }
}
