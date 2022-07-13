package com.example.timekeeper.activities.main

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.timekeeper.R
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.databinding.FragmentMainBinding
import com.example.timekeeper.viewmodel.MainViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: MainViewModel? = null

    private val CURRENT_TAB_KEY: String = "CURRENT_TAB_KEY"

    val defaultItemId = R.id.lock_apps

    var sharedPrefences: SharedPreferences? = null

    private val logTag = "MainFragment"


    private val lockAppsFragment = LockAppsFragment()
    private val selectGameFragment = SelectGameFragment()

    // private var activeFragment: Fragment = lockAppsFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // get viewModel

        sharedPrefences =
            requireActivity().getSharedPreferences(CURRENT_TAB_KEY, Context.MODE_PRIVATE)
        _viewModel!!.currentSelectItemId = sharedPrefences!!.getInt("id", defaultItemId)


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            _viewModel!!.currentSelectItemId = item.itemId
            when (item.itemId) {
                R.id.lock_apps -> {
                    // swapFragments(item.itemId, getString(R.string.apps),lockAppsFragment)
                    loadFragment(lockAppsFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.select_game -> {
                    // swapFragments(item.itemId, getString(R.string.games),selectGameFragment)
                    loadFragment(selectGameFragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
        _viewModel!!.apply {
            Log.d(logTag, "Hey I am $currentSelectItemId")
            binding.bottomNavigation.selectedItemId = currentSelectItemId
        }
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }

    /*override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // outState.putSparseParcelableArray(SAVED_STATE_CONTAINER_KEY, savedStateSparseArray)

        Log.d(logTag, "Put id: $currentSelectItemId")
        outState.putInt(SAVED_STATE_CURRENT_TAB_KEY, currentSelectItemId)
    }*/

    override fun onDestroyView() {
        val editor = sharedPrefences!!.edit()
        editor.putInt("id", binding.bottomNavigation.selectedItemId)
        editor.apply()
        super.onDestroyView()
        _binding = null
    }

    private fun MutableList<ApplicationInfo>.toApps(): MutableList<AppModal> {
        return this.map {
            AppModal(
                it.loadLabel(requireActivity().packageManager).toString(),
                it.loadIcon(requireActivity().packageManager),
                it.packageName,
                false
            )
        }.toMutableList()
    }

    private fun getPackages(): MutableList<ApplicationInfo> {
        val pm = requireActivity().packageManager
        val packages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA) //get a list of installed apps.
        for (packageInfo in packages) {
            Log.d(logTag, "Installed app name :" + packageInfo.loadLabel(pm))
            Log.d(logTag, "Installed package :" + packageInfo.packageName)
            Log.d(logTag, "Source dir : " + packageInfo.sourceDir)
            Log.d(
                logTag,
                "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName)
            ) // the getLaunchIntentForPackage returns an intent that you can use with startActivity()
        }

        Log.d(logTag, "Count of apps ${packages.size}")

        // return only non-system-apps
        return packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .toMutableList()
    }

    private fun lockApps(apps: MutableList<AppModal>) {
        val context = this.context
        val dpm =
            context!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminName = requireActivity().componentName
        val lockApps = apps.filter { it.isLocked }
        /*if (lockApps.isNotEmpty()) dpm.setLockTaskPackages(
            adminName,
            lockApps.map { it.packageName }.toTypedArray()
        )*/
    }
}




