package com.nicokarg.whydoyou.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.databinding.FragmentMainBinding
import com.nicokarg.whydoyou.services.YourService
import com.nicokarg.whydoyou.viewmodel.MainViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: MainViewModel? = null

    var mServiceIntent: Intent? = null
    private var mYourService: YourService? = null

    // Shared Preferences
    private var sharedPreferences: SharedPreferences? = null

    private val defaultItemId = R.id.lock_apps


    private val logTag = "MainFragment"


    private val lockAppsFragment = LockAppsFragment()
    private val selectGameFragment = SelectGameFragment()

    // private var activeFragment: Fragment = lockAppsFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d(logTag, "Fragment create")
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(logTag, "Fragment created")

        _viewModel =
            ViewModelProvider(requireActivity())[MainViewModel::class.java] // get viewModel

        sharedPreferences =
            requireActivity().getSharedPreferences(
                resources.getString(R.string.MY_PREFS)
                , Context.MODE_PRIVATE)

        startService() // start background service (find fg-app)

        _viewModel!!.currentSelectItemId = sharedPreferences!!.getInt(
            resources.getString(R.string.CURRENT_TAB_ID), defaultItemId)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            Log.d(logTag, "Item was selected")
            _viewModel!!.currentSelectItemId = item.itemId
            when (item.itemId) {
                R.id.lock_apps -> {
                    Log.d(logTag, "Try to display lock apps")
                    loadFragment(lockAppsFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.select_game -> {
                    Log.d(logTag, "Try to display select game")
                    loadFragment(selectGameFragment)
                    return@setOnItemSelectedListener true
                }
            }
            false
        }

        _viewModel!!.apply {
            Log.d(logTag, "Initial selected item: $currentSelectItemId")
            binding.bottomNavigation.selectedItemId = currentSelectItemId
        }
    }

    private fun loadFragment(fragment: Fragment) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null).commit()
    }


    private fun startService() {
//        if (!sharedPreferences!!.getBoolean(SERVICE_RUNNING,true)) {
        // Since the app is already running in foreground,
        // we need not launch the service as a foreground service
        // to prevent itself from being terminated.
        mYourService = YourService()
        mServiceIntent = Intent(requireContext(), mYourService!!.javaClass)

        requireContext().startService(mServiceIntent) // If the service is not running, we start it by using startService().
    }

    override fun onDestroyView() {
        val editor = sharedPreferences!!.edit()
        editor.putInt("id", binding.bottomNavigation.selectedItemId)
        editor.apply()
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }
}




