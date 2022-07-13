package com.example.timekeeper.viewmodel

import androidx.lifecycle.ViewModel
import com.example.timekeeper.data.AppModal
import com.example.timekeeper.database.DBHandler

class LockAppsViewModel:ViewModel() {

    var dbHandler: DBHandler? = null
    var dbAppList: List<AppModal>? = null
}