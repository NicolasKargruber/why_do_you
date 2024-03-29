package com.nicokarg.whydoyou.activities.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.about.AboutMeActivity
import com.nicokarg.whydoyou.activities.settings.SettingsActivity
import com.nicokarg.whydoyou.broadcast.Restarter
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.ActivityMainBinding
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.viewmodel.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private var _viewModel: MainActivityViewModel? = null

    private val logTag = "MainActivity"

    private val navController get() = findNavController(R.id.content_main_nav_host_fragment)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate UI
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        _viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        // set up toolbar
        binding.toolbar.apply {
            setSupportActionBar(this) // to interact with icon in toolbar
            // give the back button functionality
            setNavigationOnClickListener {
                Log.d(logTag, "Navigation icon clicked")
                onBackPressed()
            }
        }

        // nav Controller
        appBarConfiguration = AppBarConfiguration(navController.graph)

        binding.apply {
            // set up navigation and UI
            navController.addOnDestinationChangedListener(onDestinationListener) // hide/show toolbar and bottom navigation
            activityMainBottomNavigation.setupWithNavController(navController) // set up bottom navigation
        }

        _viewModel!!.apply {
            // SQLite Database
            dbHandler = DBHandler(this@MainActivity) // creating a new DBHandler class
            lifecycleScope.launch {
                updateDbIfNecessary()
            }

            // shared Preferences
            sharedPreferences =
                getSharedPreferences(resources.getString(R.string.MY_PREFS), Context.MODE_PRIVATE)
        }
    }

    // destination on change listener hides/shows toolbar and bottom navigation when needed
    private val onDestinationListener =
        NavController.OnDestinationChangedListener { _, destination, _ ->
            binding.apply {
                when (destination.id) {
                    R.id.firstFragment -> { // permissions screen
                        Log.d(logTag, "Destination is firstFragment")
                        toolbar.navigationIcon = null
                        toolbar.menu.clear()
                        // toolbar.menu.findItem(R.id.refresh).isVisible = false
                        activityMainBottomNavigation.isGone = true
                    }
                    R.id.lockAppsFragment -> { // lock apps screen
                        Log.d(logTag, "Destination is lockAppsFragment")
                        _viewModel!!.apply { bottomNavSelectItemId = destination.id }
                        toolbar.navigationIcon = null
                        // toolbar.menu.findItem(R.id.refresh).isVisible = true
                        activityMainBottomNavigation.isVisible = true
                    }
                    R.id.selectGameFragment -> { // select game screen
                        Log.d(logTag, "Destination is selectGameFragment")
                        _viewModel!!.apply { bottomNavSelectItemId = destination.id }
                        toolbar.navigationIcon = null
                        // toolbar.menu.findItem(R.id.refresh).isVisible = false
                        activityMainBottomNavigation.isVisible = true
                    }
                    else -> { // must be a game
                        Log.d(logTag, "Destination is 'else'")
                        toolbar.navigationIcon = ContextCompat.getDrawable(
                            this@MainActivity,
                            R.drawable.ic_round_arrow_back_24
                        )
                        // toolbar.menu.findItem(R.id.refresh).isVisible = false
                        activityMainBottomNavigation.isGone = true
                    }
                }
            }
        }

    suspend fun updateDbIfNecessary() {
        withContext(Dispatchers.IO) {
            _viewModel!!.apply {
                // apps in database
                dbAppList = dbHandler!!.readApps()

                // read applicationInfo of installed apps
                val installedApps = getPackages().toApps() // get installed Apps
                var updateAppAtIndex = listOf<String>() // index of installed apps
                var removeAppAtIndex = listOf<String>() // index of database apps

                if (dbAppList.isNullOrEmpty()) {
                    installedApps.forEach {
                        dbHandler!!.addNewApp(it)
                    }
                    dbAppList = installedApps
                } else {
                    updateAppAtIndex = appIndicesToUpdate(dbAppList!!, installedApps)
                    removeAppAtIndex = appIndicesToRemove(dbAppList!!, installedApps)
                }

                if (updateAppAtIndex.any { it != "nothing" }) { // installed apps list has updated
                    updateAppAtIndex.forEachIndexed { index, i ->
                        installedApps[index].let { instApp ->
                            when (i) {
                                "add" -> dbHandler!!.addNewApp(instApp)
                                "update" -> dbHandler!!.updateApp(instApp)
                                // else app remained the same (i==0)
                            }
                        }
                    }
                }
                if (removeAppAtIndex.any { it != "nothing" }) { // got apps to delete: installed apps list has updated
                    removeAppAtIndex.forEachIndexed { index, i ->
                        dbAppList!![index].let { dbApp ->
                            if (i == "remove") dbHandler!!.deleteApp(dbApp.packageName)
                        }
                    }
                }
                // else do nothing as apps have not changed
            }
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun getPackages(): MutableList<ApplicationInfo> {
        val pm = this.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        val packages =
            pm.getInstalledApplications(PackageManager.GET_META_DATA) //get a list of installed apps.

        Log.d(logTag, "Found ${packages.size} packages in total")
        val apps =
            packages.filter { pm.getLaunchIntentForPackage(it.packageName) != null } // returns only launchable apps
                .toMutableList()
        Log.d(logTag, "Of which ${apps.size} apps have an intent")
        return apps
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
                it.category,
                it.isSystemApp(),
                it.getIsLocked(),
                dateOneDayAgo
            )
        }.toMutableList()
    }

    // finds app in list of apps of DB and returns isLocked of app
    private fun ApplicationInfo.getIsLocked(): Boolean {
        val temp = _viewModel!!.dbAppList?.filter { it.packageName == this.packageName }
        return if (temp.isNullOrEmpty()) false // app not found
        else temp.first().isLocked
    }

    // finds app in list of apps of DB and returns isLocked of app
    private fun ApplicationInfo.isSystemApp(): Boolean {
        val isSystem =
            (this.flags and ApplicationInfo.FLAG_SYSTEM != 0) || (this.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0)
        Log.d(logTag, "Is a System app: $isSystem")
        return isSystem
    }

    private val dateOneDayAgo: Date
        get() {
            val calendar: Calendar = Calendar.getInstance() // this would default to now
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            return calendar.time
        }

    private fun appIndicesToUpdate(
        dbApps: List<AppModal>,
        instApps: MutableList<AppModal>
    ): List<String> {
        val updateIndexList = mutableListOf<String>()
        // if (dbApps.size != instApps.size) return updateIndexList
        instApps.forEach { instApp ->
            val apps = dbApps.filter { dbApp -> dbApp.packageName == instApp.packageName }
            if (apps.isEmpty()) updateIndexList.add("add") // not in the list
            else if (
                !apps.first().isEqualTo(instApp)
            ) updateIndexList.add("update") // apps should hold only instance, update it
            else updateIndexList.add("nothing") // nothing of both
        }
        return updateIndexList // index of installed apps
    }

    private fun appIndicesToRemove(
        dbApps: List<AppModal>,
        instApps: MutableList<AppModal>
    ): List<String> {
        val removeIndexList = mutableListOf<String>()
        dbApps.forEach { dbApp ->
            if (instApps.none { it.packageName == dbApp.packageName }) removeIndexList.add("remove") // not installed anymore
            else removeIndexList.add("nothing") // do nothing
        }
        return removeIndexList // index of database apps
    }

    override fun onDestroy() {
        super.onDestroy()

        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(this, Restarter::class.java)
        this.sendBroadcast(broadcastIntent)

        // save selectedTabId
        val editor = _viewModel!!.sharedPreferences!!.edit()
        editor.putInt(
            resources.getString(R.string.CURRENT_TAB_ID),
            binding.activityMainBottomNavigation.selectedItemId
        )
        editor.apply()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.top_app_bar_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.more -> {
                val intent = Intent(this, AboutMeActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.content_main_nav_host_fragment)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
