package com.nicokarg.whydoyou.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.model.AppModal

class MainActivityViewModel:ViewModel() {
    var dbHandler: DBHandler? = null
    var dbAppList: List<AppModal>? = null
}