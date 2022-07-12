package com.example.timekeeper.activities.main

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.timekeeper.R
import com.example.timekeeper.databinding.ActivityMainBinding
import com.example.timekeeper.viewmodel.MainViewModel
import com.example.timekeeper.broadcast.Restarter
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler
import com.example.timekeeper.services.YourService


class MainActivity : AppCompatActivity() {

    var mServiceIntent: Intent? = null
    private var mYourService: YourService? = null

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var _viewModel: MainViewModel? = null

    private var dbHandler: DBHandler? = null
    private var dbAppList: List<AppModal>? = null

    private val logTag = "MainActivity"

    private val PACKAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        _viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        isAccessGranted()

        // Since the app is already running in foreground,
        // we need not launch the service as a foreground service
        // to prevent itself from being terminated.
//        mYourService = YourService()
//        mServiceIntent = Intent(this, mYourService!!.javaClass)
//        if (!isMyServiceRunning(mYourService!!.javaClass)) {
//            startService(mServiceIntent) // If the service is not running, we start it by using startService().
//        }


        // SQLite Database
        // creating a new DBhandler class
        // and passing our context to it.
        dbHandler = DBHandler(this)
        dbAppList = dbHandler!!.readApps()
        val installedApps = getPackages().toApps() // get Apps

        if (dbAppList.isNullOrEmpty()) {
            dbAppList = installedApps
            dbAppList!!.forEach {
                dbHandler!!.addNewApp(it)
            }
        } else if (dbAppList != installedApps) { // apps have changed
            installedApps.forEach {
                if (dbAppList!!.any { dbApp -> dbApp.name == it.name }) dbHandler!!.updateApp(
                    it.name,
                    it
                )
                else dbHandler!!.addNewApp(it)
            }
        }
        // else do nothing


    }

    // checks the current status of the background service
    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                Log.i("Service status", "Running")
                return true
            }
        }
        Log.i("Service status", "Not running")
        return false
    }


    // Note that in onDestroy() we are dedicatedly calling stopService(),
    // so that our overridden method gets invoked.
    // If this was not done,
    // then the service would have ended automatically after app is killed
    // without invoking our modified onDestroy() method in YourService.java
    override fun onDestroy() {
        //stopService(mServiceIntent);
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, Restarter::class.java)
        this.sendBroadcast(broadcastIntent)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private fun isAccessGranted(): Boolean {
        try {
            val packageManager: PackageManager = packageManager
            val applicationInfo: ApplicationInfo =
                packageManager.getApplicationInfo(packageName, 0);
            val appsOpsManager: AppOpsManager =
                getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            var mode = 0
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mode = appsOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid,
                    applicationInfo.packageName
                )
            }
            return (mode == AppOpsManager.MODE_ALLOWED)
        } catch (e: PackageManager.NameNotFoundException) {
            return false
        }
    }

    private fun MutableList<ApplicationInfo>.toApps(): MutableList<AppModal> {
        return this.map {
            AppModal(
                it.loadLabel(this@MainActivity.packageManager).toString(),
                it.loadIcon(this@MainActivity.packageManager),
                it.packageName,
                it.getIsLocked()
            )
        }.toMutableList()
    }

    private fun ApplicationInfo.getIsLocked(): Boolean {
        val temp = dbAppList?.filter { it.name == this.name }
        return !temp.isNullOrEmpty() && temp.first().isLocked
    }

    private fun getPackages(): MutableList<ApplicationInfo> {
        val pm = this.packageManager
        val packages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA) //get a list of installed apps.
        for (packageInfo in packages) {
            Log.d(logTag, "Installed app name :" + packageInfo.loadLabel(pm))
            Log.d(logTag, "Installed package :" + packageInfo.packageName)
            Log.d(logTag, "Source dir : " + packageInfo.sourceDir)
            Log.d(
                logTag,
                "Launch Activity :" + pm.getLaunchIntentForPackage(packageInfo.packageName)
            ) // the getLaunchIntentForPackage returns an intent that you can use with startActivity()
        }


        Log.d(logTag, "Found ${packages.size} in total")
        val nsa = packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .toMutableList()

        Log.d(logTag, "Of which ${nsa.size} non system apps")
        // return only non-system-apps
        return nsa
    }

    private fun lockApps(apps: MutableList<AppModal>) {
        val context = this
        val dpm =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminName = this.componentName
        val lockApps = apps.filter { it.isLocked }
        /*if (lockApps.isNotEmpty()) dpm.setLockTaskPackages(
            adminName,
            lockApps.map { it.packageName }.toTypedArray()
        )*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}