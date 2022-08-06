package com.nicokarg.whydoyou.model

import android.graphics.drawable.Drawable
import java.util.*

data class AppModal(
    val name: String,
    val icon: Pair <Int,Drawable>,
    val packageName: String,
    val category:Int,
    val isSystemApp: Boolean,
    var isLocked: Boolean,
    var lastTimeLocked: Date
) {
    fun isEqualTo(otherApp: AppModal): Boolean {
        return name == otherApp.name && icon.first == otherApp.icon.first && category == otherApp.category
    }
    var id = 0
}