package com.nicokarg.whydoyou.activities.main

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.adapter.RecyclerAdapterLockApps
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.FragmentLockAppsBinding
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.viewmodel.LockAppsViewModel


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: LockAppsViewModel? = null

    private val logTag = "LockAppsFragment"


    companion object {
        var systemAppsPrefChanged = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {}
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lock_apps, container, false)
        return binding.root

    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(logTag,"Fragment onViewCreated")

        _viewModel = ViewModelProvider(requireActivity())[LockAppsViewModel::class.java]

        _viewModel!!.apply {
            lockedTotal.observe(viewLifecycleOwner) {
                binding.lockAppsTvLockedTotalCount.text = String.format("%d apps", it)
            }
            // get apps from SQLite Database
            dbHandler = DBHandler(activity)
            if (dbAppList.value==null) {
                dbAppList.value = dbHandler!!.readApps()
                dbAppList.value!!.sortByName()
            }
        }

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(
                requireContext(),
                _viewModel!!.dbAppList.value!!,
                { pack, il -> _viewModel!!.updateIsLockedOfApp(pack, il) },
                requireActivity().packageName
            )
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (systemAppsPrefChanged) binding.lockAppsRecyclerView.adapter!!.notifyDataSetChanged()
        systemAppsPrefChanged = false
        Log.d(logTag,"Fragment resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag,"Fragment destroyed")
    }

    private fun MutableList<AppModal>.sortByName() {
        this.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }
}

