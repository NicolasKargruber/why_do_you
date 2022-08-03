package com.nicokarg.whydoyou.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.model.ListActivity
import com.nicokarg.whydoyou.model.NoteModal
import com.nicokarg.whydoyou.viewmodel.NotesViewModel

class RecyclerAdapterNotes(private val viewModel:NotesViewModel, val onClick:(String,Int)->Unit) :
    RecyclerView.Adapter<RecyclerAdapterNotes.ViewHolder>() {

    val logTag: String = "RecyclerAdapterNotes"
    val notesContentList get() = viewModel.getNotes()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: MaterialButton = itemView.findViewById(R.id.mat_btn_note)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_note_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.button.apply {
            notesContentList[position].let {
                text = it
                setOnClickListener { onClick(text.toString(),position) }
            }
        }
    }

    override fun getItemCount(): Int {
        return notesContentList.size
    }
}