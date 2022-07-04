package com.example.timekeeper

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.timekeeper.data.App
import com.example.timekeeper.databinding.FragmentMainBinding
import com.example.timekeeper.model.MainViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val logTag = "MainFragment"
    private var _viewModel: MainViewModel? = null

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

        _viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        // get Apps
        _viewModel!!.apply {
            if (appList.isEmpty()) appList = getPackages().toApps() //set on first call of app
            sortAppsByName()
        }

        Log.d(logTag, "Count of apps ${_viewModel!!.appList.size}")
        lockApps(_viewModel!!.appList)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.lock_apps -> selectedFragment = LockAppsFragment()
                R.id.select_game -> selectedFragment = SelectGameFragment()
            }
            requireActivity().supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, selectedFragment!!)
                .commit()
            true
        }

        binding.bottomNavigation.selectedItemId = R.id.lock_apps
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun MutableList<ApplicationInfo>.toApps(): MutableList<App> {
        return this.map {
            App(
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

    private fun lockApps(apps: MutableList<App>) {
        val context = this.context
        val dpm = context!!.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminName = requireActivity().componentName
        val lockApps = apps.filter { it.isLocked }
        if (lockApps.isNotEmpty()) dpm.setLockTaskPackages(
            adminName,
            lockApps.map { it.packageName }.toTypedArray()
        )
    }


}