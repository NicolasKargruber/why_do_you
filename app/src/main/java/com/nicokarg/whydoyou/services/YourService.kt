package com.nicokarg.whydoyou.services

import android.app.*
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.lock.LockScreenActivity
import com.nicokarg.whydoyou.broadcast.Restarter
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.database.DBHandler
import java.util.*


class YourService : Service() {
    private val logTag = "YourService"

    // shared Preferences
    var defPrefs: SharedPreferences? = null
    var sharedPreferences: SharedPreferences? = null

    var counter = 0

    private var dbHandler: DBHandler? = null
//    private var dbAppList: List<AppModal>? = null

    // we are starting a foreground service differently for Build versions greater than Android Oreo
    // This because of the strict notification policies introduced recently
    // where we have to define our own notification channel to display them correctly.
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
        )
        dbHandler = DBHandler(this)
//        dbAppList = dbHandler!!.readApps()
        defPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        sharedPreferences = this.getSharedPreferences(
            resources.getString(R.string.MY_PREFS), Context.MODE_PRIVATE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startMyOwnForeground() {
        val NOTIFICATION_CHANNEL_ID = "example.permanence"
        val channelName = "Background Service"
        val chan = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)!!
        manager.createNotificationChannel(chan)
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
        val notification = notificationBuilder.setOngoing(true)
            .setContentTitle("App is running in background")
            .setPriority(NotificationManager.IMPORTANCE_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(2, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startTimer()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimerTask()
        val broadcastIntent = Intent()
        broadcastIntent.action = "restartservice"
        broadcastIntent.setClass(
            this,
            Restarter::class.java
        ) // We'll be using the Restarter later as a trigger to restart our service.
        this.sendBroadcast(broadcastIntent) // asynchronously sends a broadcast with the action name "restartservice".
    }

    // Here we have defined a simple Timer task
    private var timer: Timer? = null
    private fun startTimer() {
        if (timer == null) { // timer causes error if task executed twice
            timer = Timer()
            Timer().schedule(timerTask, 1000, 1000)
//        sharedPreferences!!.edit().putBoolean(SERVICE_RUNNING,true).apply()
        } else Log.d(logTag, "timerTask already running")
    }

    private val timerTask = object : TimerTask() {
        override fun run() { // runs every 1 second
            Log.i(
                "Count",
                "=========  " + counter++
            ) // while incrementing itself every time Log prints.
            val appPackage = getForegroundTask(this@YourService)
            Log.e(logTag, "Current App in foreground is: $appPackage")
            val app = dbHandler!!.getAppFromDB(appPackage) // find app in DB
            if (app != null) Log.e(logTag, "Found app in DB")
            if (appIsLocked(app)) {
                if (passedLockInterval(app!!.lastTimeLocked)) {
                    Log.e(logTag, "Try to lock current App: ${app.name}")
                    // start another activity
                    val lockIntent = Intent(this@YourService, LockScreenActivity::class.java)
                    lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    this@YourService.startActivity(lockIntent)
//                    app.lastTimeLocked = Calendar.getInstance().time
                    sharedPreferences!!.edit().putString(
                        resources.getString(R.string.LOCKED_PACKAGE),
                        appPackage
                    ).apply() // save locked app package
                    // dbHandler!!.updateLastLockedOfApp(app.packageName,Calendar.getInstance().time)
                } else Log.e(logTag, "Waiting to lock this App: ${app.name}")
            } else Log.e(logTag, "Does not need to be locked")
        }
    }

    private fun stopTimerTask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
//        sharedPreferences!!.edit().putBoolean(SERVICE_RUNNING,false).apply()
    }


    fun passedLockInterval(lastTimeLocked: Date): Boolean {
        val now = Calendar.getInstance().time
        val diff: Long = now.time - lastTimeLocked.time
        val seconds = diff / 1000 // total difference in seconds
        val minutes = seconds / 60 // total difference in min
        val hours = minutes / 60 // total difference in hours
        val days = hours / 24 // total difference in days
        val lockInterval = defPrefs!!.getString(
            resources.getString(R.string.key_lock_time_intervals),
            resources.getString(R.string.default_lock_time_interval)
        )!!.toInt()
        if (days > 0 || hours > 0) return true // days or even hours have passed
        else if (minutes >= lockInterval) return true // lock interval is passed
        Log.e(logTag, "Time passed since last locked $minutes:$seconds of $lockInterval min")
        return false
    }

//    private fun getApp(appPackage: String): AppModal? {
//        return if (dbAppList!!.none { it.packageName == appPackage }) null
//        else dbAppList!!.first { it.packageName == appPackage }
//        //TODO make dbHandler do this check!!
//    }


    //TODO simplify
    fun appIsLocked(app: AppModal?): Boolean {
        if (app == null) return false
        return app.isLocked
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun getForegroundTask(context: Context): String {
        var currentApp = "NULL"
        val usm = context.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val appList =
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 1000, time)
        if (appList != null && appList.size > 0) {
            val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
            for (usageStats in appList) {
                mySortedMap[usageStats.lastTimeUsed] = usageStats
            }
            if (!mySortedMap.isEmpty()) {
                currentApp = mySortedMap[mySortedMap.lastKey()]!!.packageName
            }
        }
        return currentApp
    }
}