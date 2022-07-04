package com.example.timekeeper.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PuzzleViewModel:ViewModel() {

    var currentNum = MutableLiveData<Int>(-2)
}