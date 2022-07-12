package com.example.timekeeper.activities.main

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
import com.example.timekeeper.R
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler
import com.example.timekeeper.databinding.FragmentMainBinding
import com.example.timekeeper.viewmodel.MainViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainFragment : Fragment() {

    private val SAVED_STATE_CURRENT_TAB_KEY: String = "SAVED_STATE_CURRENT_TAB_KEY"
    // private val SAVED_STATE_CONTAINER_KEY: String = "SAVED_STATE_CONTAINER_KEY"

    // private var savedStateSparseArray: SparseArray<Fragment.SavedState>? = null
    private var currentSelectItemId: Int = R.id.lock_apps

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

//    private var dbHandler: DBHandler? = null
//    private var dbAppList: List<AppModal>? = null

    private val logTag = "MainFragment"
    private var _viewModel: MainViewModel? = null


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

        if (savedInstanceState != null) {
//            savedStateSparseArray = savedInstanceState
//                .getSparseParcelableArray(SAVED_STATE_CONTAINER_KEY)
            currentSelectItemId = savedInstanceState
                .getInt(SAVED_STATE_CURRENT_TAB_KEY)
            Log.d(logTag, "Get id: $currentSelectItemId")
        }

        _viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // get viewModel


       /* _viewModel!!.apply {
            if (appList.isEmpty()) //do on first call of app
                appList = getPackages().toApps() // get Apps
            sortAppsByName() // sort them
        }*/

        // Log.d(logTag, "Count of apps ${_viewModel!!.appList.size}")
        // lockApps(_viewModel!!.appList)


        binding.bottomNavigation.setOnItemSelectedListener { item ->
            currentSelectItemId = item.itemId
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

        Log.d(logTag, "Hey I am $currentSelectItemId")
        binding.bottomNavigation.selectedItemId = currentSelectItemId
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // outState.putSparseParcelableArray(SAVED_STATE_CONTAINER_KEY, savedStateSparseArray)

        Log.d(logTag, "Put id: $currentSelectItemId")
        outState.putInt(SAVED_STATE_CURRENT_TAB_KEY, currentSelectItemId)
    }

    override fun onDestroyView() {
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




