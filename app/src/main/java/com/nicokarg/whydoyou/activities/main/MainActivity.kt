package com.nicokarg.whydoyou.activities.main

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
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.settings.SettingsActivity
import com.nicokarg.whydoyou.broadcast.Restarter
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityMainBinding
import com.nicokarg.whydoyou.model.AppModal
import java.util.*


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

        binding.toolbar.apply {
            setSupportActionBar(this) // title
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration) // back button

//        _viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // SQLite Database
        dbHandler = DBHandler(this) // creating a new DBHandler class
        dbAppList = dbHandler!!.readApps()
        val installedApps = getPackages().toApps() // get installed Apps

        if (dbAppList.isNullOrEmpty()) {
            installedApps.forEach {
                dbHandler!!.addNewApp(it)
            }
            dbAppList = installedApps
        } else if (appsAreEqual(dbAppList!!, installedApps)) { // apps have changed
            installedApps.forEach {
                //TODO Bitmap seems to change and isLocked is varying
                val app = dbAppList!!.filter { dbApp -> dbApp.packageName == it.packageName }
                if (app.isEmpty()) dbHandler!!.addNewApp(it)
                else if (!app.first().isEqual(it)) dbHandler!!.updateApp(it)
                // else app remained the same
            }
            dbAppList!!.forEach {
                if (installedApps.none { instApp -> instApp.packageName == it.packageName }) dbHandler!!.deleteApp(
                    it.packageName
                )
            }
        }
        // else do nothing
    }

    private fun appsAreEqual(dbApps: List<AppModal>, instApps: MutableList<AppModal>): Boolean {
        if (dbApps.size!=instApps.size) return false
        instApps.forEach {
            val apps = dbApps.filter { dbApp -> dbApp.packageName == it.packageName }
            if (apps.isEmpty()) return false
            apps.first().let {
                dbApp -> if (!dbApp.isEqual(it)) return false
            }
        }
        return true
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
            AppModal(
                it.loadLabel(this@MainActivity.packageManager).toString(),
                it.loadIcon(this@MainActivity.packageManager),
                it.packageName,
                it.getIsLocked(),
                dateOneDayAgo
            )
        }.toMutableList()
    }

    private val dateOneDayAgo:Date get() {
        val calendar:Calendar = Calendar.getInstance() // this would default to now
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        return calendar.time
    }

    private fun ApplicationInfo.getIsLocked(): Boolean {
        val temp = dbAppList?.filter { it.name == this.name }
        return !temp.isNullOrEmpty() && temp.first().isLocked
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
        menuInflater.inflate(R.menu.custom_toolbar_menu, menu)
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