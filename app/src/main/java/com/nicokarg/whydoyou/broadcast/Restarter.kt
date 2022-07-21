package com.nicokarg.whydoyou.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.nicokarg.whydoyou.services.YourService


class Restarter : BroadcastReceiver() {
    // The broadcast with the action name "restartservice" which is defined in YourService.java
    // triggers a method which will restart your service.
    // This is done using BroadcastReceiver in Android.

    // override the built-in onReceive() method (in BroadcastReceiver) to add the statement which will restart the service.
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i("Broadcast Listened", "Service tried to stop")
        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // startService() only works as intended below Android Oreo 8.1
            // use the startForegroundService() for higher versions
            context.startForegroundService(Intent(context, YourService::class.java)) // show a continuous notification to keep the service running
        } else {
            context.startService(Intent(context, YourService::class.java))
        }
    }
}