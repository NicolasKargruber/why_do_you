package com.example.timekeeper.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.timekeeper.R
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

        Log.d(logTag, "Fragment create")
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("QueryPermissionsNeeded")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(logTag, "Fragment created")

        _viewModel =
            ViewModelProvider(requireActivity()).get(MainViewModel::class.java) // get viewModel

        sharedPrefences =
            requireActivity().getSharedPreferences(CURRENT_TAB_KEY, Context.MODE_PRIVATE)
        _viewModel!!.currentSelectItemId = sharedPrefences!!.getInt("id", defaultItemId)

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

    override fun onDestroyView() {
        val editor = sharedPrefences!!.edit()
        editor.putInt("id", binding.bottomNavigation.selectedItemId)
        editor.apply()
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }
}




