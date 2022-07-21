package com.nicokarg.whydoyou.activities.main

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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.databinding.FragmentFirstBinding




class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    // var requestMultiplePermissions: ActivityResultLauncher<Array<String>>? = null

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

        isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS).let { usGranted ->
            isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW).let { sawGranted ->
                if (usGranted && sawGranted) {
                    Toast.makeText(
                        requireContext(), "Permissions already granted", Toast.LENGTH_SHORT
                    ).show()
                    findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
                } else {
                    if (!usGranted) binding.firstPermissionPackageUsageStats.buttonPermission.apply {
                        isEnabled = true
                        text = "Grant"
                    }
                    if (!sawGranted) binding.firstPermissionSystemAlertWindow.buttonPermission.apply {
                        isEnabled = true
                        text = "Grant"
                    }
                }
            }
        }

        binding.firstPermissionPackageUsageStats.textViewPermission.text =
            "Please grant access to your usage access"
        binding.firstPermissionPackageUsageStats.buttonPermission.setOnClickListener {
            checkPermission(
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            )
        }

        binding.firstPermissionSystemAlertWindow.textViewPermission.text =
            "Please grant access to display over other apps"
        binding.firstPermissionSystemAlertWindow.buttonPermission.setOnClickListener {
            checkPermission(
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
        binding.firstPermissionPackageUsageStats.buttonPermission.let { pusBtn ->
            binding.firstPermissionSystemAlertWindow.buttonPermission.let { sawBtn ->
                if (isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS)) pusBtn.apply {
                    isEnabled = false
                    text = "Granted"
                    Log.d(logTag,"Usage Stats granted")
                }
                if (isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW)) sawBtn.apply {
                    isEnabled = false
                    text = "Granted"
                    Log.d(logTag,"Manage Overlay granted")
                }
                binding.firstButtonGoNext.isEnabled = !pusBtn.isEnabled && !sawBtn.isEnabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }

//    // checks the current status of the background service
//    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
//        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
//        for (service in manager!!.getRunningServices(Int.MAX_VALUE)) {
//            if (serviceClass.name.equals(service.service.className)) {
//                return true
//            }
//        }
//        return false
//    }

    // Function to check and request permission.
    private fun checkPermission(action: String) {
        val intent = Intent(action)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent) // Requesting the permission
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