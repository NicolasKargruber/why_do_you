package com.example.timekeeper

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.timekeeper.databinding.FragmentNotesBinding
import com.example.timekeeper.model.NotesViewModel
import com.google.android.material.button.MaterialButton


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private var _viewModel: NotesViewModel? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel

    private val logTag = "NotesFragment"

    private val arrayAdapter by lazy { ArrayAdapter<String>(
        requireContext(),
        R.layout.note_list_item,
        R.id.mat_btn_note,
        viewModel!!.notes
    ) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity()).get(NotesViewModel::class.java)

        binding.apply {
            notesList.adapter = arrayAdapter
            notesList.onItemClickListener = OnItemClickListener { arrayView, _, pos, _ ->
                val txt = arrayView.getItemAtPosition(pos) as String
                createAlertDialog(txt,pos)
                Log.d(logTag,"I come bis here") }

            fabAddNote.setOnClickListener {
                createAlertDialog()
                textCreateFirstMemo.isVisible = false
            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createAlertDialog(txt:String = "", pos:Int=-1) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Note")

        // Set up the input
        val editText = EditText(requireContext())

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        editText.inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editText.setText(txt)
        builder.setView(editText)

        // Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            editText.text.toString().let {
                if (it.isNotEmpty()){
                    if (pos==-1) viewModel!!.notes.add(it)
                    else viewModel!!.notes[pos] = it
                }
                arrayAdapter.notifyDataSetChanged()
            }

        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

//    private fun updateMapEntry() { // is called when a new note was created
//        viewModel!!.apply {
//            val btn = binding.notesList.getChildAt(notes.size) as MaterialButton
//            notes[btn] = notes.filter { it.key == null }.values.toString()
//            notes.remove(null)
//        }
//    }
}