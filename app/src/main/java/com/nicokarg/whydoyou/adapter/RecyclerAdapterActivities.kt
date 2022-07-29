package com.nicokarg.whydoyou.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.model.ListActivity

class RecyclerAdapterActivities(private val activityList: List<ListActivity>,val onClick:(Int)->Unit) :
    RecyclerView.Adapter<RecyclerAdapterActivities.ViewHolder>() {

    val logTag: String = "RecyclerAdapterActivities"
    //private val appList get() = viewModel.appList

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.activities_icon)
        val descr: TextView = itemView.findViewById(R.id.activity_tv_description)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_activity_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            activityList[position].let {
                icon.setImageDrawable(it.icon)
                descr.text = it.text
            }
            itemView.setOnClickListener { onClick(position) }
        }
    }

    override fun getItemCount(): Int {
        return activityList.size
    }
}