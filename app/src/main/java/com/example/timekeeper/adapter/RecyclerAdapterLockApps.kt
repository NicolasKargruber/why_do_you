package com.example.timekeeper.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.timekeeper.R
import com.example.timekeeper.data.AppModal
import com.google.android.material.switchmaterial.SwitchMaterial

class RecyclerAdapterLockApps(
    private val appList: List<AppModal>,
    val setIsLocked: (AppModal) -> Unit,
    val applicationPackage: String
) :
    RecyclerView.Adapter<RecyclerAdapterLockApps.ViewHolder>() {

    val logTag: String = "RecyclerAdapterLockApps"
    //private val appList get() = viewModel.appList

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.item_app_icon)
        val appName: TextView = itemView.findViewById(R.id.item_app_name)
        val appPackage: TextView = itemView.findViewById(R.id.item_app_package)
        val lockSwitch: SwitchMaterial = itemView.findViewById(R.id.item_app_lock_switch)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            appList[position].let {
                appIcon.setImageDrawable(it.icon)
                appName.text = it.name
                appPackage.text = it.packageName
                lockSwitch.isChecked = it.isLocked
                if (applicationPackage==it.packageName) lockSwitch.isEnabled = false
                    lockSwitch.setOnCheckedChangeListener { _, b ->
                        it.isLocked = b
                        setIsLocked(it)
                    }
            }
        }
    }

    override fun getItemCount(): Int {
        return appList.size
    }
}