package com.example.timekeeper.viewmodel

import androidx.lifecycle.ViewModel
import com.example.timekeeper.data.AppModal

class MainViewModel:ViewModel() {
    var appList:MutableList<AppModal> = mutableListOf()
    var lockGameId: Int = -1

    fun sortAppsByName(): Unit {
        appList.sortWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )
    }
}