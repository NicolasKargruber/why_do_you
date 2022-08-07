package com.nicokarg.whydoyou.activities.main

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.snackbar.Snackbar
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.adapter.RecyclerAdapterLockApps
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.FragmentLockAppsBinding
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.viewmodel.LockAppsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: LockAppsViewModel? = null

    private val logTag = "LockAppsFragment"

    var customProgressAD: AlertDialog? = null

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
        Log.d(logTag, "Fragment onViewCreated")

        _viewModel = ViewModelProvider(requireActivity())[LockAppsViewModel::class.java]

        _viewModel!!.apply {
            lockedTotal.observe(viewLifecycleOwner) {
                binding.lockAppsTvLockedTotalCount.text = String.format("%d apps", it)
            }
            // get apps from SQLite Database
            dbHandler = DBHandler(activity)
            if (dbAppList.value == null) readApps()
        }

        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED) // adds refresh button to app bar

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(
                requireContext(),
                _viewModel!!,
                { pack, il -> _viewModel!!.updateIsLockedOfApp(pack, il) },
                requireActivity().packageName
            )
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            _viewModel!!.dbAppList.observe(viewLifecycleOwner){

                Log.d(logTag,"db apps has new value, update recyclerview ")
                adapter!!.notifyDataSetChanged()
            }
        }
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            // Add menu items here
            menuInflater.inflate(R.menu.top_app_bar_lock_apps, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            // Handle the menu selection
            return when (menuItem.itemId) {
                R.id.refresh -> {
                    Log.d(logTag,"Refresh MenuItem clicked")
                    lifecycleScope.launch {
                        refreshApps()
                    }
                    true
                }
                else -> false
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    suspend fun refreshApps() {
        withContext(Dispatchers.IO){
            (requireActivity() as MainActivity).apply {
                var progressCircle: AlertDialog? = null
                runOnUiThread {
                    progressCircle = createProgressAD()
                    progressCircle!!.show()
                }
                updateDbIfNecessary()
                //_viewModel!!.readAppsInBG()
                runOnUiThread {
                    progressCircle!!.dismiss()
                    // binding.lockAppsRecyclerView.adapter!!.notifyDataSetChanged()
                    Snackbar.make(requireView(),"List of installed apps refreshed",Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun createProgressAD(): AlertDialog {
        return AlertDialog.Builder(requireContext()).setView(R.layout.circular_progress_indicator_layout).create()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        if (systemAppsPrefChanged) binding.lockAppsRecyclerView.adapter!!.notifyDataSetChanged()
        systemAppsPrefChanged = false
        Log.d(logTag, "Fragment resumed")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }
}

