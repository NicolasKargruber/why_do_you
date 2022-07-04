package com.example.timekeeper.model

import androidx.lifecycle.ViewModel
import com.example.timekeeper.data.App

class MainViewModel:ViewModel() {
    var appList:MutableList<App> = mutableListOf()

    fun sortAppsByName(): Unit {
        appList.sortWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.name }
        )
    }
}