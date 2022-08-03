package com.nicokarg.whydoyou.viewmodel

import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.services.YourService

class MainActivityViewModel:ViewModel() {
    // Database
    var dbHandler: DBHandler? = null
    var dbAppList: List<AppModal>? = null

    // Shared Preferences
    var sharedPreferences: SharedPreferences? = null

    var bottomNavSelectItemId:Int = -1
}