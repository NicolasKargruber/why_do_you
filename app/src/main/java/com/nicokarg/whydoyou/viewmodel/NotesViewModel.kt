package com.nicokarg.whydoyou.viewmodel

import androidx.lifecycle.ViewModel
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.model.NoteModal

class NotesViewModel : ViewModel() {
    var dbHandler: DBHandler? = null
    private var notes = mutableListOf<String>()
    var updateDB:Boolean = true

    fun readNotes() {
        val dbNotesList = dbHandler!!.readNotes()
        notes = dbNotesList.sortedBy { it.position }.map { it.content }.toMutableList()
    }

    fun getNote(pos:Int): String {
        return notes[pos]
    }

    fun deleteNote(pos:Int) {
        notes.removeAt(pos)
        if(updateDB) { dbHandler!!.deleteNote(pos)
        notes.subList(pos,notes.lastIndex).forEach {
            val posInNotes = notes.indexOf(it)
            dbHandler!!.updateNote(posInNotes+1, NoteModal(posInNotes,it))
        }}
    }

    fun emptyNotes() {
        notes = mutableListOf()
    }

    fun getNotes(): List<String> {
        return notes
    }

    fun setNote(pos: Int, note:String){
        notes[pos] = note
        if(updateDB) dbHandler!!.updateNote(pos, NoteModal(pos,note))
    }

    fun addNote(note:String){
        notes.add(note)
        if(updateDB) dbHandler!!.addNewNote(NoteModal(notes.size-1,note))
    }
}