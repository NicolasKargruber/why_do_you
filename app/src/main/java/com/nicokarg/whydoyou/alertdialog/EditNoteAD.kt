package com.nicokarg.whydoyou.alertdialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.nicokarg.whydoyou.R


class EditNoteAD(
    private val c: Context,
    private val noteText: String,
    val onPositive: (String) -> String?
) :
    DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(c) // create and pass context to builder
            val editText = EditText(c) // Set up editText

            editText.apply {
                inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS // Specify the type of input expected
                maxLines = 3 // shows maximal 3 lines at once
                hint = noteText // Set displayed hint
            }

            val messageMap = mapOf(
                true to "Respond with a reason in the text field beneath",
                false to "Respond with the same reason as listed below"
            )

            builder.setTitle("Note")
                .setMessage(messageMap[noteText.isBlank()])
                .setView(editText)
                .setPositiveButton(R.string.ok) { _, _ -> }
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }
            // Create the AlertDialog object and return it
            val dialog = builder.create()
            dialog.show() // is necessary for null pointer exception
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val message = onPositive(editText.text.toString())
                    if (message.isNullOrBlank()) dialog.cancel()
                    else {
                        editText.requestFocus()
                        val dwbl = ContextCompat.getDrawable(c, R.drawable.ic_round_error_24)!!
                        dwbl.setBounds(0, 0, dwbl.intrinsicWidth, dwbl.intrinsicHeight)
                        editText.setError(message, dwbl)
                    }
                }
            dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}