package com.nicokarg.whydoyou.viewmodel

import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.database.DBHandler

class LockScreenActivityViewModel:ViewModel() {
    var sharedPreferences: SharedPreferences? = null
    var dbHandler: DBHandler? = null
    var lockGameId: Int = -1
    var lockedPackage = ""
    var appIcon:Drawable? = null

    fun findLockedAppIcon() {
        appIcon = dbHandler!!.getAppFromDB(lockedPackage)?.icon?.second
    }
}