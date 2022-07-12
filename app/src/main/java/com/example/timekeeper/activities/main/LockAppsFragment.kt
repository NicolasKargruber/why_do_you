package com.example.timekeeper.activities.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timekeeper.adapter.RecyclerAdapterLockApps
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler
import com.example.timekeeper.databinding.FragmentLockAppsBinding
import com.example.timekeeper.viewmodel.MainViewModel


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var _viewModel: MainViewModel? = null

    private var dbHandler: DBHandler? = null
    private var dbAppList: List<AppModal>? = null

    private val logTag = "LockAppsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLockAppsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        // get apps from SQLite Database
        dbHandler = DBHandler(activity)
        dbAppList = dbHandler!!.readApps()
        dbAppList!!.sortByName()

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(dbAppList!!) { app, b ->
                app.isLocked = b
                dbHandler!!.updateApp(app.name,app)
            }
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }

//        Log.d(logTag, "Count of apps ${_viewModel!!.appList.size}")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun List<AppModal>.sortByName() {
        this.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }
}

