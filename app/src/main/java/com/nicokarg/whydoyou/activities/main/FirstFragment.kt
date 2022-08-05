package com.nicokarg.whydoyou.activities.main

import android.app.AppOpsManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.databinding.FragmentFirstBinding
import com.nicokarg.whydoyou.services.YourService
import com.nicokarg.whydoyou.viewmodel.FirstViewModel


class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    private var _viewModel: FirstViewModel? = null

    private val defaultItemId = R.id.lockAppsFragment

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


        _viewModel =
            ViewModelProvider(requireActivity())[FirstViewModel::class.java] // get viewModel

        isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS).let { usGranted ->
            isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW).let { sawGranted ->
                if (usGranted && sawGranted) {
                    Log.d(logTag, "Permissions already granted")
                    goToNext()
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
            requestPermissionManually(
                Settings.ACTION_USAGE_ACCESS_SETTINGS
            )
        }

        binding.firstPermissionSystemAlertWindow.textViewPermission.text =
            "Please grant access to display over other apps"
        binding.firstPermissionSystemAlertWindow.buttonPermission.setOnClickListener {
            requestPermissionManually(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            )
        }

        binding.firstButtonGoNext.setOnClickListener { goToNext() }
    }

    private fun startService() {
        _viewModel!!.apply {
            mYourService =
                YourService() // (this class checks if service is already running and will not start it twice)
            mServiceIntent = Intent(requireActivity(), mYourService!!.javaClass)
            requireActivity().startService(mServiceIntent) // start service by using startService()
        }
    }

    private fun goToNext() {
        binding.firstButtonGoNext.isGone = true
        val sharedPreferences =
            requireActivity().getSharedPreferences(
                resources.getString(R.string.MY_PREFS),
                Context.MODE_PRIVATE
            )
        val bottomNavSelectItemId = sharedPreferences!!.getInt(
            resources.getString(R.string.CURRENT_TAB_ID), defaultItemId
        )
        startService()
        val action = // goes to previous fragment/tab when app was last closed
            if (bottomNavSelectItemId == R.id.selectGameFragment) R.id.action_firstFragment_to_selectGameFragment
            else R.id.action_firstFragment_to_lock_apps_fragment
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "Fragment resume")
        binding.firstPermissionPackageUsageStats.buttonPermission.let { pusBtn ->
            binding.firstPermissionSystemAlertWindow.buttonPermission.let { sawBtn ->
                pusBtn.apply {
                    if (isAccessGranted(AppOpsManager.OPSTR_GET_USAGE_STATS))  {
                    isEnabled = false
                    text = "Granted"
                    Log.d(logTag, "Usage Stats granted")
                } else {
                    isEnabled = true
                    text = "Grant"
                    Log.d(logTag, "Usage Stats not granted")
                } }
                sawBtn.apply {if (isAccessGranted(AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW))  {
                    isEnabled = false
                    text = "Granted"
                    Log.d(logTag, "Manage Overlay granted")
                } else{
                    isEnabled = true
                    text = "Grant"
                    Log.d(logTag, "Manage Overlay not granted")
                }
            }
                binding.firstPermissionsLayout.isGone = !pusBtn.isEnabled && !sawBtn.isEnabled
                binding.firstButtonGoNext.isVisible = !pusBtn.isEnabled && !sawBtn.isEnabled
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d(logTag, "Fragment destroyed")
    }

    // Function to check and request permission.
    private fun requestPermissionManually(action: String) {
        val intent = Intent(action)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        try { // apparently samsung s8 with Android 9 cannot do this
            startActivity(intent) // Requesting the permission
        } catch (e:ActivityNotFoundException) {
            Toast.makeText(requireContext(),"Not able to open settings, please do it manually",Toast.LENGTH_SHORT).show()
        }
    }

    private fun isAccessGranted(op: String): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appOps.unsafeCheckOpNoThrow(
                    op, Process.myUid(), requireContext().packageName
                )
            } else {
                appOps.checkOpNoThrow(op,
                    Process.myUid(), requireContext().packageName
                )
            }

        return mode == AppOpsManager.MODE_ALLOWED
    }
}