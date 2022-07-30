package com.nicokarg.whydoyou.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton
import com.nicokarg.whydoyou.model.ListActivity

class ActivitiesViewModel : ViewModel() {
    var sharedPreferences: SharedPreferences? = null
    var activitiesList: List<ListActivity>? = null
    var selectedActivityPosition = -1
    val selectedActivity: ListActivity?
        get() = if (selectedActivityPosition==-1 || activitiesList == null) null
        else activitiesList!![selectedActivityPosition]
    private var timePair: Pair<Int, Int> = Pair(0, 0) // in minutes

    fun setTime(min: Int, sec: Int) {
        timePair = Pair(min, sec)
    }

    fun setTimeInMillis(millis: Int) {
        val min: Int = millis / 1000 / 60 // quotient ex. 5:00
        val sec: Int = millis / 1000 % 60 // remainder ex. 0:32
        timePair = Pair(min, sec)
    }

    fun getTime(): Pair<Int, Int> {
        return timePair
    }

    fun getTimeInMillis(): Int {
        return (timePair.first * 60 + timePair.second) * 1000
    }

    fun getTimeString(): String? {
        return "${timePair.first.takeIf { it > -1 } ?: return null}:${timePair.second.takeIf { it > 9 } ?: "0${timePair.second}"}"
    }

    fun oneSecPassed(): String? {
        val min = timePair.first
        val sec = timePair.second
        timePair = if (sec > 0) Pair(min, sec - 1)
        else Pair(min - 1, 59) // seconds are 0
        // if (min == -1) do something
        return getTimeString()
    }
}