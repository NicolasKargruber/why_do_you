package com.example.timekeeper.activities.main

import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.timekeeper.R
import com.example.timekeeper.databinding.FragmentFirstBinding
import com.example.timekeeper.services.YourService
import com.example.timekeeper.viewmodel.PuzzleViewModel


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private var _viewModel: PuzzleViewModel? = null
    private val binding get() = _binding!!

    var mServiceIntent: Intent? = null
    private var mYourService: YourService? = null

    private val logTag = "FirstFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(logTag, "Fragment create")
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(logTag, "Fragment created")

        if (isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS) //ToDo check individually if permissions were already granted
        ) {
            if (isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW)) {
                startService()
                Toast.makeText(requireContext(), "Permissions already granted", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
            } else binding.firstPermissionSystemAlertWindow.buttonPermission.apply {
                isEnabled = true
                text = "Grant"
            }
        } else binding.firstPermissionPackageUsageStats.buttonPermission.apply {
            isEnabled = true
            text = "Grant"
        }

        binding.firstPermissionPackageUsageStats.textViewPermission.text =
            "Please grant access to your usage access"
        binding.firstPermissionPackageUsageStats.buttonPermission.setOnClickListener {
            checkPermission(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            )
        }

        binding.firstPermissionSystemAlertWindow.textViewPermission.text =
            "Please grant access to display over other apps"
        binding.firstPermissionSystemAlertWindow.buttonPermission.setOnClickListener {
            checkPermission(
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            )
        }

        binding.firstButtonGoNext.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
        }

    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "Fragment resume")
        if (isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS)&&isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW)) binding.firstButtonGoNext.isEnabled = true
    }

    private fun startService() {
        // Since the app is already running in foreground,
        // we need not launch the service as a foreground service
        // to prevent itself from being terminated.
        mYourService = YourService()
        mServiceIntent = Intent(requireContext(), mYourService!!.javaClass)
        if (!isMyServiceRunning(mYourService!!.javaClass)) {
            requireContext().startService(mServiceIntent) // If the service is not running, we start it by using startService().
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }

    // checks the current status of the background service
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager =
            requireActivity().getSystemService(AppCompatActivity.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }

    // Function to check and request permission.
    private fun checkPermission(op: String, action: String) {
        val intent = Intent(action)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri

        if (!isAccessGranted(op)) {
            startActivity(intent) // Requesting the permission
        }
    }

    private fun isAccessGranted(op: String): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                op, Process.myUid(), requireContext().packageName
            )
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}