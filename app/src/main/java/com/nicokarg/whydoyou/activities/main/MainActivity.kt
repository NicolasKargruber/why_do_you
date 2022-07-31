package com.nicokarg.whydoyou.activities.main

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.settings.SettingsActivity
import com.nicokarg.whydoyou.broadcast.Restarter
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityMainBinding
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.viewmodel.MainActivityViewModel
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var _viewModel: MainActivityViewModel? = null
//
//    private var dbHandler: DBHandler? = null
//    private var dbAppList: List<AppModal>? = null

    private val logTag = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.apply {
            setSupportActionBar(this) // title
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration) // back button

        _viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        _viewModel!!.apply {

            // SQLite Database
            dbHandler = DBHandler(this@MainActivity) // creating a new DBHandler class
            dbAppList = dbHandler!!.readApps()
            val installedApps = getPackages().toApps() // get installed Apps
            var updateAppAtIndex = listOf(0) // index of installed apps
            var removeAppAtIndex = listOf(0) // index of database apps

            if (dbAppList.isNullOrEmpty()) {
                installedApps.forEach {
                    dbHandler!!.addNewApp(it)
                }
                dbAppList = installedApps
            } else {
                updateAppAtIndex = appIndicesToUpdate(dbAppList!!, installedApps)
                removeAppAtIndex = appIndicesToRemove(dbAppList!!, installedApps)
            }

            if (updateAppAtIndex.sum() > 0) { // installed apps list has updated
                updateAppAtIndex.forEachIndexed { index, i ->
                    installedApps[index].let { instApp ->
                        when (i) {
                            1 -> dbHandler!!.addNewApp(instApp)
                            2 -> dbHandler!!.updateApp(instApp)
                            // else app remained the same (i==0)
                        }
                    }
                }
            }
            if (removeAppAtIndex.sum() > 0) { // got apps to delete: installed apps list has updated
                removeAppAtIndex.forEachIndexed { index, i ->
                    dbAppList!![index].let { dbApp ->
                        if (i == 1) dbHandler!!.deleteApp(dbApp.packageName)
                    }
                }
            }
            // else do nothing as apps have not changed
        }
    }

    private fun appIndicesToUpdate(
        dbApps: List<AppModal>,
        instApps: MutableList<AppModal>
    ): List<Int> {
        val updateIndexList = mutableListOf<Int>()
        // if (dbApps.size != instApps.size) return updateIndexList
        instApps.forEach { instApp ->
            val apps = dbApps.filter { dbApp -> dbApp.packageName == instApp.packageName }
            if (apps.isEmpty()) updateIndexList.add(1)
            else if (!appsAreEqual(
                    apps.first(),
                    instApp
                )
            ) updateIndexList.add(2) // apps should hold only one app
            else updateIndexList.add(0)
        }
        return updateIndexList // index of installed apps
    }

    private fun appIndicesToRemove(
        dbApps: List<AppModal>,
        instApps: MutableList<AppModal>
    ): List<Int> {
        val removeIndexList = mutableListOf<Int>()
        dbApps.forEach {
            if (instApps.none { instApp -> instApp.packageName == it.packageName }) removeIndexList.add(
                -1
            )
            else removeIndexList.add(0)
        }
        return removeIndexList // index of database apps
    }

    private fun appsAreEqual(dbApp: AppModal, instApp: AppModal): Boolean {
        return instApp.name == dbApp.name && instApp.icon.first == dbApp.icon.first
    }

    // Note that in onDestroy() we are delicately calling stopService(),
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
            Log.d(logTag, "This is the icon id: ${it.icon}")
            AppModal(
                it.loadLabel(this@MainActivity.packageManager).toString(),
                Pair(
                    it.icon,
                    it.loadIcon(this@MainActivity.packageManager)
                ), // icon id and drawable
                it.packageName,
                it.getIsLocked(),
                dateOneDayAgo
            )
        }.toMutableList()
    }

    private val dateOneDayAgo: Date
        get() {
            val calendar: Calendar = Calendar.getInstance() // this would default to now
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            return calendar.time
        }

    // finds app in list of apps of DB and returns isLocked of app
    private fun ApplicationInfo.getIsLocked(): Boolean {
        val temp = _viewModel!!.dbAppList?.filter { it.packageName == this.packageName }
        return if (temp.isNullOrEmpty()) false // app not found
        else temp.first().isLocked
    }

    private fun getPackages(): MutableList<ApplicationInfo> {
        val pm = this.packageManager
        val packages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA) //get a list of installed apps.

        Log.d(logTag, "Found ${packages.size} packages in total")
        val apps =
            packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null } // returns system apps and user apps
                .toMutableList()
        Log.d(logTag, "Of which ${apps.size} apps have an intent")
        val nsa = apps.filter { it.flags != ApplicationInfo.FLAG_SYSTEM } // non system apps
        return apps
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}