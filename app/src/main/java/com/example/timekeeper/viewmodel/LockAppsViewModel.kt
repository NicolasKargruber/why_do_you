package com.example.timekeeper.viewmodel

import androidx.lifecycle.ViewModel
import com.example.timekeeper.model.AppModal
import com.example.timekeeper.database.DBHandler

class LockAppsViewModel:ViewModel() {
    var dbHandler: DBHandler? = null
    var dbAppList: MutableList<AppModal>? = null
}