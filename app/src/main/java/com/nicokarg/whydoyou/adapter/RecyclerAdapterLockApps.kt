package com.nicokarg.whydoyou.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.model.AppModal

class RecyclerAdapterLockApps(
    val context: Context,
    private val appList: List<AppModal>,
    val setIsLockedInDB: (String, Boolean) -> Unit,
    val applicationPackage: String
) :
    RecyclerView.Adapter<RecyclerAdapterLockApps.ViewHolder>() {

    val logTag: String = "RecyclerAdapterLockApps"
    var showSystemApps = true

    val notLockableStrings = mapOf(
        true to context.getString(R.string.not_lockable_system_app),
        false to context.getString(R.string.not_lockable_app_why_do_you),
    )
    val lockedStrings = mapOf(
        true to context.getString(R.string.locked),
        false to context.getString(R.string.not_locked),
    )
    val lockableDrawables =
        ContextCompat.getDrawable(context, R.drawable.material_button_drawable_selector)
    val notLockableDrawable = ContextCompat.getDrawable(context, R.drawable.ic_outline_info_24)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.item_app_icon)
        val appName: TextView = itemView.findViewById(R.id.item_app_name)
        val appIsLocked: CheckedTextView = itemView.findViewById(R.id.item_app_is_locked)
        val lockButton: MaterialButton = itemView.findViewById(R.id.item_app_lock_btn)
        val infoButton: MaterialButton = itemView.findViewById(R.id.item_app_lock_info_btn)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        showSystemApps = getSystemAppsPref() // check weather to show system apps
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_app_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.apply {
            appList[position].let { app -> // app is either from nsAppList or appList
                appIcon.setImageDrawable(app.icon.second)
                appName.text = app.name
                appIsLocked.setLocked(app.isLocked)
                if (app.isWhyDoYou() || app.isSystemApp) infoButton.apply {
                    if (!showSystemApps && app.isSystemApp) {
                        itemView.isGone = true
                        itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
                        return // stop doing the rest of here
                    }
                    isVisible = true
                    lockButton.isInvisible = true
                    setOnClickListener {
                        createDialog(notLockableStrings[app.isSystemApp]!!)
                    }

                } else lockButton.apply { // is normal app
                    itemView.isVisible = true
                    itemView.layoutParams =
                        RecyclerView.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )
                    // make lockButton visible
                    isVisible = true
                    infoButton.isInvisible = true
                    isChecked = app.isLocked // this sets the drawable
                    addOnCheckedChangeListener { _, b ->
                        appIsLocked.setLocked(b)
                        setIsLockedInDB(app.packageName, b)
                        app.isLocked = b // now rv remembers
                        appList[position].isLocked = b // now rv remembers
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        super.onViewRecycled(holder)
        holder.lockButton.apply {
            clearOnCheckedChangeListeners() // prevent this from calling the previous listener
            setOnClickListener { } // prevent this from calling the previous listener
        }
    }

    override fun getItemCount(): Int {
        showSystemApps = getSystemAppsPref() // check weather to show system apps
        return appList.size
    }

    private fun createDialog(message: String) {
        AlertDialog.Builder(context)
            .setTitle("App cannot be locked")
            .setMessage(message)
            .setPositiveButton(R.string.ok, null)
            .show()
    }

    private fun getSystemAppsPref(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
            context.getString(R.string.key_system_apps_switch), true
        )
    }

    private fun CheckedTextView.setLocked(b: Boolean) {
        this.text = lockedStrings[b]
        this.isChecked = b // sets textColor of text
    }

    private fun AppModal.isWhyDoYou(): Boolean {
        return this.packageName == applicationPackage
    }
}