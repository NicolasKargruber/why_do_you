package com.example.timekeeper

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timekeeper.adapter.RecyclerAdapterLockApps
import com.example.timekeeper.databinding.FragmentLockAppsBinding
import com.example.timekeeper.model.MainViewModel


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val logTag = "LockAppsFragment"
    private var _viewModel: MainViewModel? = null

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

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(_viewModel!!)
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
}