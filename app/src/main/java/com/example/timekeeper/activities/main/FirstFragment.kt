package com.example.timekeeper.activities.main

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timekeeper.R
import com.example.timekeeper.databinding.FragmentFirstBinding
import com.example.timekeeper.viewmodel.PuzzleViewModel


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var _viewModel: PuzzleViewModel? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val logTag = "FirstFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonPermission.setOnClickListener {
            checkPermission(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            )
        }

        binding.buttonPermission.performClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isAccessGranted(op: String): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                op,
                Process.myUid(), requireContext().packageName
            )
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Function to check and request permission.
    private fun checkPermission(op: String, action: String) {

        val intent = Intent(action)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri

        if (isAccessGranted(op)) startActivity(intent) // Requesting the permission
        else {
            Toast.makeText(requireContext(), "Permission already granted", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
        }
    }
}