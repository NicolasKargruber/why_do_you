package com.example.timekeeper.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.button.MaterialButton

class NotesViewModel:ViewModel() {
 val notes = mutableListOf<String>()
}