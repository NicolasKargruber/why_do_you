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
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.model.AppModal

class RecyclerAdapterLockApps(
    val context: Context,
    private val appList: List<AppModal>,
    val setIsLocked: (String, Boolean) -> Unit,
    val applicationPackage: String
) :
    RecyclerView.Adapter<RecyclerAdapterLockApps.ViewHolder>() {

    val logTag: String = "RecyclerAdapterLockApps"
    var showSystemApps = true
    val lockedStrings = mapOf(
        true to context.getString(R.string.locked),
        false to context.getString(R.string.not_locked),
    )
    val lockableDrawables =
        ContextCompat.getDrawable(context, R.drawable.material_button_drawable_selector)
    val notLockableDrawable = ContextCompat.getDrawable(context, R.drawable.ic_outline_info_24)

    val nsaAppList: List<AppModal> get() = appList.filter { !it.isSystemApp }

    var displayedAppList: List<AppModal>? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appIcon: ImageView = itemView.findViewById(R.id.item_app_icon)
        val appName: TextView = itemView.findViewById(R.id.item_app_name)
        val appIsLocked: CheckedTextView = itemView.findViewById(R.id.item_app_is_locked)
        val lockButton: MaterialButton = itemView.findViewById(R.id.item_app_lock_btn)
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
            displayedAppList!![position].let { app -> // app is either from nsAppList or appList
                if (!showSystemApps && app.isSystemApp) {
                    itemView.isGone = true
                    return
                } else itemView.isVisible = true
                appIcon.setImageDrawable(app.icon.second)
                appName.text = app.name
                appIsLocked.text = lockedStrings[app.isLocked]
                appIsLocked.isChecked = app.isLocked // sets textColor of text
                lockButton.isChecked = app.isLocked // this also sets the drawable
                if (applicationPackage == app.packageName) lockButton.apply { // do not lock whyDoYou
                    isCheckable = false
                    icon = notLockableDrawable
                    setOnClickListener {
                        createDialog("This application cannot be locked") // open dialog
                    }
                } else if (app.isSystemApp) lockButton.apply { // make system app non lockable
                    // TODO check preferences for boolean
                    isCheckable = false
                    icon = notLockableDrawable
                    setOnClickListener {
                        createDialog("System apps cannot be locked") // open dialog
                    }
                } else lockButton.apply { // is normal app
                    //TODO Zen Mode bugs locks also
                    isCheckable = true
                    isChecked = app.isLocked // this also sets the drawable
                    icon = lockableDrawables
                    addOnCheckedChangeListener { _, b ->
                        appIsLocked.text = lockedStrings[b]
                        appIsLocked.isChecked = b // sets textColor
                        setIsLocked(app.packageName, b)
                        setIsLockedInAppList(
                            app,
                            b
                        ) // do this so that the recyclerview also knows it
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
        displayedAppList =
            if (showSystemApps) appList
            else nsaAppList
        return displayedAppList!!.size
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

    private fun setIsLockedInAppList(app: AppModal, b: Boolean) {
        appList.first { it.packageName == app.packageName }.isLocked = b
    }
}