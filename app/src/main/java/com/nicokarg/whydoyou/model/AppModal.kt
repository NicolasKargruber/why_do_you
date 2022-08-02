package com.nicokarg.whydoyou.model

import android.graphics.drawable.Drawable
import java.util.*

data class AppModal(
    val name: String,
    val icon: Pair <Int,Drawable>,
    val packageName: String,
    val isSystemApp: Boolean,
    var isLocked: Boolean,
    var lastTimeLocked: Date
) {
    fun isEqual(otherApp: AppModal): Boolean {
        if (name!=otherApp.name) return false
        else if(packageName!=otherApp.packageName) return false
        // ToDo check icon (Drawable/Bitmap currently changes when read from DB)
        return true
    }
    var id = 0
}