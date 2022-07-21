package com.nicokarg.whydoyou.viewmodel

import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.database.DBHandler

class LockAppsViewModel:ViewModel() {
    var dbHandler: DBHandler? = null
    var dbAppList: MutableList<AppModal>? = null
}