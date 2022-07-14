package com.example.timekeeper.activities.main

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.timekeeper.R
import com.example.timekeeper.broadcast.Restarter
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler
import com.example.timekeeper.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

//    private var _viewModel: MainViewModel? = null

    private var dbHandler: DBHandler? = null
    private var dbAppList: List<AppModal>? = null

    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

//        _viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // SQLite Database
        // creating a new DBHandler class
        // and passing our context to it.
        dbHandler = DBHandler(this)
        dbAppList = dbHandler!!.readApps()
        val installedApps = getPackages().toApps() // get Apps

        if (dbAppList.isNullOrEmpty()) {
            installedApps.forEach {
                dbHandler!!.addNewApp(it)
            }
            dbAppList = installedApps
        } else if (dbAppList != installedApps) { // apps have changed
            installedApps.forEach {
                //TODO Bitmap seems to change and isLocked is varying
                if (dbAppList!!.any { dbApp -> dbApp.packageName == it.packageName }) dbHandler!!.updateApp(
                    it.name,
                    it
                )
                else dbHandler!!.addNewApp(it)
            }
            dbAppList!!.forEach{
                if (installedApps.none { instApp -> instApp.packageName == it.packageName }) dbHandler!!.deleteApp(it.name)
            }
        }
        // else do nothing
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
        Log.d(logTag, "Found ${packages.size} packages in total")
        val apps = packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null } // returns system apps and user apps
            .toMutableList()
        Log.d(logTag, "Of which ${apps.size} apps have an intent")
        val nsa = apps.filter { it.flags != ApplicationInfo.FLAG_SYSTEM } // non system apps
        return apps
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
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