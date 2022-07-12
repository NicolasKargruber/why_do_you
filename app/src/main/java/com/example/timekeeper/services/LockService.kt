package com.example.timekeeper.services

import android.app.ActivityManager
import android.app.AlarmManager
import android.app.IntentService
import android.app.PendingIntent
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.text.TextUtils
import android.util.Log
import com.example.timekeeper.broadcast.Restarter
import com.example.timekeeper.utils.SpUtil
import java.lang.Exception
import java.util.*

class LockService : IntentService("LockService") {
    // IntentService is an extension of the Service() class
    // handles asynchronous requests (expressed as Intents) on demand

    var threadIsTerminate = false
    var savePkgName: String? = null
    var sUsageStatsManager: UsageStatsManager? = null
    var timer = Timer()

    //private boolean isLockTypeAccessibility;
    private var lastUnlockTimeSeconds: Long = 0
    private var lastUnlockPackageName = ""
    private var lockState = false
    private var mServiceReceiver: ServiceReceiver? = null

    // private var mLockInfoManager: CommLockInfoManager? = null
    private var activityManager: ActivityManager? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        // ActivityManager gives information about, and interacts with, activities, services, and the containing process.
        activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
        mServiceReceiver =
            ServiceReceiver() // Broadcast Receiver that receives and handles broadcast intents sent

        // app's manifest file specifies the type of intents that the component would like to receive.
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(UNLOCK_ACTION)
        // If intent matches intent filter(declared in the manifest file),
        // the system starts that component and delivers it the Intent object.
        registerReceiver(mServiceReceiver, filter) // registers this broadcast with our intent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // UsageStatsManager provides access to device usage history and statistics.
            // Usage data is aggregated into time intervals: days, weeks, months, and years.
            sUsageStatsManager =
                this.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
        }
        threadIsTerminate = true // A thread is a thread of execution in a program
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // timely information about events in your app while it's not in use
            //NotificationUtil.createNotification(this, "App Lock", "App Lock running in background")
        }
    }

    protected override fun onHandleIntent(intent: Intent?) {
        // which the system calls when the IntentService receives a start request.
        runForever()
    }

    private fun runForever() {
        while (threadIsTerminate) { // terminates if onDestroy and onTaskRemoved is called
            val packageName = getLauncherTopApp(
                this@LockService,
                activityManager!!
            ) // returns foreground package name
            Log.d("Test", "This is the app: $packageName")
            /*if (lockState && !TextUtils.isEmpty(packageName) && !inWhiteList(packageName)) {
                val isLockOffScreenTime: Boolean =
                    SpUtil.instance.getBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false)
                val isLockOffScreen: Boolean =
                    SpUtil.instance.getBoolean(AppConstants.LOCK_AUTO_SCREEN, false)
                savePkgName =
                    SpUtil.instance.getString(AppConstants.LOCK_LAST_LOAD_PKG_NAME, "")
                if (isLockOffScreenTime && !isLockOffScreen) {
                    val time: Long =
                        SpUtil.instance.getLong(AppConstants.LOCK_CURR_MILLISECONDS, 0)
                    val leaverTime: Long =
                        SpUtil.instance.getLong(AppConstants.LOCK_APART_MILLISECONDS, 0)
                    if (!TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName) && savePkgName != packageName) {
                        if (homes.contains(packageName) || packageName.contains("launcher")) {
                            val isSetUnLock: Boolean = mLockInfoManager.isSetUnLock(savePkgName)
                            if (!isSetUnLock) {
                                if (System.currentTimeMillis() - time > leaverTime) {
                                    mLockInfoManager.lockCommApplication(savePkgName)
                                }
                            }
                        }
                    }
                }
                if (isLockOffScreenTime && isLockOffScreen) {
                    val time: Long =
                        SpUtil.instance.getLong(AppConstants.LOCK_CURR_MILLISECONDS, 0)
                    val leaverTime: Long =
                        SpUtil.instance.getLong(AppConstants.LOCK_APART_MILLISECONDS, 0)
                    if (!TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(packageName) && savePkgName != packageName) {
                        if (homes.contains(packageName) || packageName.contains("launcher")) {
                            val isSetUnLock: Boolean = mLockInfoManager.isSetUnLock(savePkgName)
                            if (!isSetUnLock) {
                                if (System.currentTimeMillis() - time > leaverTime) {
                                    mLockInfoManager.lockCommApplication(savePkgName)
                                }
                            }
                        }
                    }
                }
                if (!isLockOffScreenTime && isLockOffScreen && !TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(
                        packageName
                    )
                ) {
                    if (savePkgName != packageName) {
                        isActionLock = false
                        if (homes.contains(packageName) || packageName.contains("launcher")) {
                            val isSetUnLock: Boolean = mLockInfoManager.isSetUnLock(savePkgName)
                            if (!isSetUnLock) {
                                mLockInfoManager.lockCommApplication(savePkgName)
                            }
                        }
                    } else {
                        isActionLock = true
                    }
                }
                if (!isLockOffScreenTime && !isLockOffScreen && !TextUtils.isEmpty(savePkgName) && !TextUtils.isEmpty(
                        packageName
                    ) && savePkgName != packageName
                ) {
                    if (homes.contains(packageName) || packageName.contains("launcher")) {
                        val isSetUnLock: Boolean = mLockInfoManager.isSetUnLock(savePkgName)
                        if (!isSetUnLock) {
                            mLockInfoManager.lockCommApplication(savePkgName)
                        }
                    }
                }
                if (mLockInfoManager.isLockedPackageName(packageName)) {
                    passwordLock(packageName)
                    continue
                }
            }*/
            try {
                Thread.sleep(210) // pauses thread
            } catch (ignore: Exception) {
            }
        }
    }

    /*private fun inWhiteList(packageName: String): Boolean {
        return packageName == AppConstants.APP_PACKAGE_NAME
    }*/

    private fun getLauncherTopApp(context: Context, activityManager: ActivityManager): String {
        //isLockTypeAccessibility = SpUtil.instance.getBoolean(AppConstants.LOCK_TYPE, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // getRunningTasks is deprecated in lollipop
            val appTasks: List<ActivityManager.RunningTaskInfo> = activityManager.getRunningTasks(1) // get foregroundTask (App)
            if (null != appTasks && appTasks.isNotEmpty()) {
                return appTasks[0].topActivity!!.packageName // returns package name
            }
        } else {
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000
            var result = ""
            val event: UsageEvents.Event = UsageEvents.Event() // An event representing a state change for a component.
            // queryEvents gets aggregated event stats for the given time interval
            val usageEvents: UsageEvents = sUsageStatsManager!!.queryEvents(beginTime, endTime)
            while (usageEvents.hasNextEvent()) { // Returns whether or not there are more events to read
                usageEvents.getNextEvent(event) // get additional event
                // MOVE_TO_FOREGROUND -> An event type denoting that an Activity moved to the foreground
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.packageName // get package name
                }
            }
            // TextUtils -> set of utility functions to do operations on String
            if (!TextUtils.isEmpty(result)) { // if not empty
                return result // returns package name
            }
        }
        return "" // no foreground activity was found
    }

    private val homes: List<String>
        private get() {
            val names: MutableList<String> = ArrayList()
            val packageManager: PackageManager = this.packageManager
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            val resolveInfo: List<ResolveInfo> =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (ri in resolveInfo) {
                names.add(ri.activityInfo.packageName)
            }
            return names
        }

    /*private fun passwordLock(packageName: String) {
        LockApplication.instance.clearAllActivity()
        val intent = Intent(this, GestureUnlockActivity::class.java)
        intent.putExtra(AppConstants.LOCK_PACKAGE_NAME, packageName)
        intent.putExtra(AppConstants.LOCK_FROM, AppConstants.LOCK_FROM_FINISH)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }*/

    override fun onDestroy() { // Thread terminates here
        super.onDestroy()
        threadIsTerminate = false // thread is terminated
        timer.cancel() // Terminates this timer, discarding any currently scheduled tasks.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // NotificationUtil.cancelNotification(this) // cancel notification
        }
         lockState = true // SpUtil.instance!!.getBoolean(AppConstants.LOCK_STATE)
        if (lockState) {
            // Intent contains brief description of an operation to be performed
            val intent = Intent(this, Restarter::class.java) // Create an intent for a specific component.
            intent.putExtra("type", "lockservice") // Add extended data to the intent. (can later be retrieved)
            sendBroadcast(intent) // Broadcast the given intent to all interested BroadcastReceivers.
        }
        unregisterReceiver(mServiceReceiver) // unregisters our broadcast receiver
    }

    // if the service is currently running and the user has removed a task (force stop)
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        threadIsTerminate = false // thread is terminated
        timer.cancel() // Terminates this timer, discarding any currently scheduled tasks.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // NotificationUtil.cancelNotification(this) // cancel notification
        }
        lockState = true // SpUtil.instance!!.getBoolean(AppConstants.LOCK_STATE)
        if (lockState) {
            val restartServiceTask = Intent(applicationContext, this.javaClass) // Create an intent for a specific component.
            // Set explicit application package name,
            // Intent can only match the components in the given application package
            restartServiceTask.setPackage(packageName)
            // giving a PendingIntent to another application,
            // you are granting it the right to perform the operation
            // you have specified as if the other application was yourself
            val restartPendingIntent: PendingIntent = PendingIntent.getService(
                applicationContext,
                1495,
                restartServiceTask,
                PendingIntent.FLAG_ONE_SHOT // indicating that this PendingIntent can be used only once
            )
            // AlarmManager provides access to the system alarm services.
            // These allow you to schedule your application to be run at some point in the future.
            val myAlarmService: AlarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME, // time since boot, including sleep
                SystemClock.elapsedRealtime() + 1500, // in 1,5s
                restartPendingIntent // restart LockService()
            )
        }
    }

    // Base class (BroadcastReceiver) for code that receives and handles broadcast intents sent
    inner class ServiceReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // called when the BroadcastReceiver is receiving an Intent broadcast.
            val action: String = intent.action.toString() // action (will be performed) of intent
            val isLockOffScreen: Boolean = false
                // SpUtil.instance!!.getBoolean(AppConstants.LOCK_AUTO_SCREEN, false)
            val isLockOffScreenTime: Boolean = false
                // SpUtil.instance!!.getBoolean(AppConstants.LOCK_AUTO_SCREEN_TIME, false)
            when (action) {
                UNLOCK_ACTION -> { // own custom action
                    lastUnlockPackageName = intent.getStringExtra(LOCK_SERVICE_LASTAPP).toString() // Retrieve extended data from the intent.
                    lastUnlockTimeSeconds =
                        intent.getLongExtra(LOCK_SERVICE_LASTTIME, lastUnlockTimeSeconds) // Retrieve extended data from the intent.
                }
                Intent.ACTION_SCREEN_OFF -> { // when the device goes to sleep and becomes non-interactive
                    //SpUtil.instance!!.putLong(AppConstants.LOCK_CURR_MILLISECONDS, System.currentTimeMillis())
                    if (!isLockOffScreenTime && isLockOffScreen) {
                        val savePkgName: String = ""
                            //SpUtil.instance!!.getString(AppConstants.LOCK_LAST_LOAD_PKG_NAME, "").toString()
                        if (!TextUtils.isEmpty(savePkgName)) {
                            if (isActionLock) {
                                // mLockInfoManager.lockCommApplication(lastUnlockPackageName)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val UNLOCK_ACTION = "UNLOCK_ACTION"
        const val LOCK_SERVICE_LASTTIME = "LOCK_SERVICE_LASTTIME"
        const val LOCK_SERVICE_LASTAPP = "LOCK_SERVICE_LASTAPP"
        private const val TAG = "LockService"
        var isActionLock = false
    }
}