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
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermission(false)

        binding.buttonPermission.setOnClickListener {
            checkPermission()
        }

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

    private fun isAccessGranted(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(), requireContext().packageName
            )
        } else {
            TODO("VERSION.SDK_INT < Q")
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Function to check and request permission.
    private fun checkPermission(openSettings: Boolean = true) {
        val action = Settings.ACTION_USAGE_ACCESS_SETTINGS
        val intent = Intent(action)
        val uri: Uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri

        if (!isAccessGranted()) {
            if (openSettings) startActivity(intent) // Requesting the permission
        } else {
            startService()
            Toast.makeText(requireContext(), "Permission already granted", Toast.LENGTH_SHORT)
                .show()
            findNavController().navigate(R.id.action_FirstFragment_to_MainFragment)
        }
    }
}