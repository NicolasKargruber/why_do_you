package com.nicokarg.whydoyou.alertdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Window
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.adapter.RecyclerAdapterActivities
import com.nicokarg.whydoyou.model.ListActivity


class SelectActivityAD(val c: Context, val activitiesList: List<ListActivity>, val onClick:(Int)-> Unit) : Dialog(c) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.select_activity_layout)

        // set up rv
        val rv = findViewById<RecyclerView>(R.id.select_activity_rv)
        rv.apply {
            adapter = RecyclerAdapterActivities(activitiesList) { p ->
                onClick(p)
                dismiss()
            }
            layoutManager = GridLayoutManager(c, 3)
        }
    }
}