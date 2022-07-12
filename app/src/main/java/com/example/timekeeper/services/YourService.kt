package com.example.timekeeper.services

import android.annotation.TargetApi
import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.text.TextUtils
import android.util.Log
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.timekeeper.broadcast.Restarter
import java.util.*


class YourService : Service() {
    var counter = 0

    // we are starting a foreground service differently for Build versions greater than Android Oreo
    // This because of the strict notification policies introduced recently
    // where we have to define our own notification channel to display them correctly.
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) startMyOwnForeground() else startForeground(
            1,
            Notification()
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
        stoptimertask()
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
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() { // prints a counter value every 1 second in the Log
                Log.i(
                    "Count",
                    "=========  " + counter++
                ) // while incrementing itself every time Log prints.
                val app = getLauncherTopApp()
                Log.e("adapter", "Current App in foreground is: $app")
            }
        }
        timer!!.schedule(timerTask, 1000, 1000)
    }

    val task = object : TimerTask() {
        override fun run() { // runs every 1 second
            getForegroundTask(this@YourService)

            // start another activity
//            val lockIntent = Intent(this@YourService, LockScreenActivity::class.java)
//            lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            this@YourService.startActivity(lockIntent)

        }
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun getRecentTask(): String {
        var topPackageName: String = "nothing here"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val mUsageStatsManager = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val time = System.currentTimeMillis()
            // We get usage stats for the last 10 seconds
            val stats = mUsageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                time - 1000 * 10,
                time
            )
            // Sort the stats by the last time used
            if (stats != null) {
                val mySortedMap: SortedMap<Long, UsageStats> = TreeMap()
                for (usageStats in stats) {
                    mySortedMap[usageStats.lastTimeUsed] = usageStats
                }
                if (!mySortedMap.isEmpty()) {
                    topPackageName = mySortedMap[mySortedMap.lastKey()]!!.packageName
                }
            }
        }
        return topPackageName
    }

    private fun getForegroundTask(context: Context): String? {
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

    fun getTopAppName(context: Context): String? {
        val mActivityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        var strName: String? = ""
        try {
            strName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getLollipopFGAppPackageName(context)
            } else {
                mActivityManager.getRunningTasks(1)[0].topActivity!!.className
            }
        } catch (e: Exception) {
            e.printStackTrace()

            Log.e("exception", "Exception is: $e")
        }
        return strName
    }

    private fun getLollipopFGAppPackageName(ctx: Context): String? {
        try {
            val usageStatsManager = ctx.getSystemService("usagestats") as UsageStatsManager
            val milliSecs = (60 * 1000).toLong()
            val date = Date()
            val queryUsageStats = usageStatsManager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                date.time - milliSecs,
                date.time
            )
            if (queryUsageStats.size > 0) {
                Log.i("LPU", "queryUsageStats size: " + queryUsageStats.size)
            }
            var recentTime: Long = 0
            var recentPkg = ""
            for (i in queryUsageStats.indices) {
                val stats = queryUsageStats[i]
                if (i == 0 && "com.example.timekeeper" != stats.packageName) {
                    Log.i("LPU", "PackageName: " + stats.packageName + " " + stats.lastTimeStamp)
                }
                if (stats.lastTimeStamp > recentTime) {
                    recentTime = stats.lastTimeStamp
                    recentPkg = stats.packageName
                }
            }
            return recentPkg
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return ""
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getForegroundApp(context: Context): String? {
        val usageStatsManager = context
            .getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val ts = System.currentTimeMillis()
        val queryUsageStats = usageStatsManager
            .queryUsageStats(
                UsageStatsManager.INTERVAL_BEST,
                ts - 2000, ts
            )
        if (queryUsageStats == null || queryUsageStats.isEmpty()) {
            return null
        }
        var recentStats: UsageStats? = null
        for (usageStats in queryUsageStats) {
            if (recentStats == null
                || recentStats.lastTimeUsed < usageStats
                    .lastTimeUsed
            ) {
                recentStats = usageStats
            }
        }
        return recentStats!!.packageName
    }

    fun getLauncherTopApp(): String? {
        //isLockTypeAccessibility = SpUtil.getInstance().getBoolean(AppConstants.LOCK_TYPE, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val appTasks = activityManager.getRunningTasks(1)
            if (null != appTasks && appTasks.isNotEmpty()) {
                return appTasks[0].topActivity!!.packageName
            }
        } else {
            val endTime = System.currentTimeMillis()
            val beginTime = endTime - 10000
            var result = ""
            val event = UsageEvents.Event()
            val mUsageStatsManager = this.getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val usageEvents: UsageEvents = mUsageStatsManager.queryEvents(beginTime, endTime)
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.packageName
                }
            }
            if (!TextUtils.isEmpty(result)) {
                return result
            }
        }
        return ""
    }
}