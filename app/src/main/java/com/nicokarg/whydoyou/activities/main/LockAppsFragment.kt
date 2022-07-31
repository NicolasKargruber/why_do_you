package com.nicokarg.whydoyou.activities.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.adapter.RecyclerAdapterLockApps
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.FragmentLockAppsBinding
import com.nicokarg.whydoyou.viewmodel.LockAppsViewModel


class LockAppsFragment : Fragment() {
    private var _binding: FragmentLockAppsBinding? = null
    private val binding get() = _binding!!
    private var _viewModel: LockAppsViewModel? = null

    private val logTag = "LockAppsFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lock_apps, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity())[LockAppsViewModel::class.java]

        _viewModel!!.apply {
            lockedTotal.observe(viewLifecycleOwner) {
                binding.lockAppsTvLockedTotal.text = String.format("Locked in total: %d", it)
            }
            // get apps from SQLite Database
            dbHandler = DBHandler(activity)
            dbAppList.value = dbHandler!!.readApps()
            dbAppList.value!!.sortByName()
        }

        binding.lockAppsRecyclerView.apply {
            adapter = RecyclerAdapterLockApps(
                _viewModel!!.dbAppList.value!!,
                { pack, il -> _viewModel!!.updateIsLockedOfApp(pack, il) },
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

