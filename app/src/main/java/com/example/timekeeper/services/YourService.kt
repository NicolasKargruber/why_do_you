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
import com.example.timekeeper.activities.lock.LockScreenActivity
import com.example.timekeeper.broadcast.Restarter
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler
import java.util.*


class YourService : Service() {
    private val logTag = "YourService"

    var counter = 0

    var lockedAppPackage:String? = null

    private var dbHandler: DBHandler? = null
    private var dbAppList: List<AppModal>? = null

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
        dbAppList = dbHandler!!.readApps()
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
        timerTask = task
        timer!!.schedule(timerTask, 1000, 1000)
    }

    val task = object : TimerTask() {
        override fun run() { // runs every 1 second
            Log.i(
                "Count",
                "=========  " + counter++
            ) // while incrementing itself every time Log prints.
            val app = getForegroundTask(this@YourService)
            Log.e(logTag, "Current App in foreground is: $app")

            if (appIsLocked(app?:"")&&app!=lockedAppPackage) {
                Log.e(logTag, "Try to lock current App: $app")
                // start another activity
                val lockIntent = Intent(this@YourService, LockScreenActivity::class.java)
                lockIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this@YourService.startActivity(lockIntent)
                lockedAppPackage = app
            }
        }
    }

    fun appIsLocked(appPackage:String) : Boolean{
        if (dbAppList!!.none { it.packageName == appPackage }) return false
        return dbAppList!!.first {  it.packageName == appPackage }.isLocked
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
}