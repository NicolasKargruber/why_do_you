package com.example.timekeeper.viewmodel

import androidx.lifecycle.ViewModel

class NotesViewModel : ViewModel() {
    val notes = mutableListOf<String>()
}