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
import com.example.timekeeper.viewmodel.LockAppsViewModel


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: LockAppsViewModel? = null

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

        _viewModel = ViewModelProvider(requireActivity()).get(LockAppsViewModel::class.java)

        _viewModel!!.apply {
            // get apps from SQLite Database
            dbHandler = DBHandler(activity)
            dbAppList = dbHandler!!.readApps()
            dbAppList!!.sortByName()
        }

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(
                _viewModel!!.dbAppList!!,
                { app -> _viewModel!!.dbHandler!!.updateIsLockedOfApp(app) },
                requireActivity().packageName
            )
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(
                DividerItemDecoration(
                    context,
                    (layoutManager as LinearLayoutManager).orientation
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun MutableList<AppModal>.sortByName() {
        this.sortWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }
}

