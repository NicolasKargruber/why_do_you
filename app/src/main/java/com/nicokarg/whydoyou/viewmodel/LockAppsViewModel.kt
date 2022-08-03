package com.nicokarg.whydoyou.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.model.AppModal
import com.nicokarg.whydoyou.database.DBHandler

class LockAppsViewModel:ViewModel() {
    var dbHandler: DBHandler? = null
    val dbAppList: MutableLiveData<MutableList<AppModal>> = MutableLiveData()
    fun updateIsLockedOfApp(pack: String, il: Boolean) {
        dbHandler!!.updateIsLockedOfApp(pack,il)
        // update dbAppList
        val apps = dbAppList.value!!
        val pos = apps.indexOfFirst { it.packageName == pack }
        apps[pos].isLocked = il
        dbAppList.postValue(apps)
    }
    val lockedTotal: LiveData<Int> = Transformations.map(dbAppList) { apps ->
        if (apps.isNullOrEmpty()) 0
        else apps.map { it.isLocked }.count { it }
    }




}