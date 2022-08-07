package com.nicokarg.whydoyou.activities.games

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nicokarg.whydoyou.R
import com.nicokarg.whydoyou.activities.lock.LockScreenActivity
import com.nicokarg.whydoyou.database.DBHandler
import com.nicokarg.whydoyou.databinding.FragmentNotesBinding
import com.nicokarg.whydoyou.viewmodel.NotesViewModel
import com.google.android.material.snackbar.Snackbar
import com.nicokarg.whydoyou.adapter.RecyclerAdapterActivities
import com.nicokarg.whydoyou.adapter.RecyclerAdapterNotes
import com.nicokarg.whydoyou.alertdialog.EditNoteAD


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private var _viewModel: NotesViewModel? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private val viewModel get() = _viewModel!!

    val minWords = 6
    val minChars = 33

    val empty = "empty"
    val invalid = "invalid"
    val more_words = "more_words"
    val more_letters = "more_letters"
    val incorrect = "incorrect"

    val errorMap = mapOf(
        empty to "Your note cannot be empty",
        invalid to "Your note must consist of at least 6 words and 28 letters",
        more_words to "You need %d more words",
        more_letters to "Your need %d more letters",
        incorrect to "Your entry does not equal the text note"
    )


    private val parentIsLock get() = requireActivity().javaClass == LockScreenActivity::class.java

    private val logTag = "NotesFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _viewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        if (parentIsLock) _viewModel!!.apply {
            dbHandler = DBHandler(requireContext())
            readNotes()
        } else _viewModel!!.updateDB = false

        binding.apply {
            // set up rv
            notesRecyclerView.apply {
                adapter = RecyclerAdapterNotes(viewModel) { content, pos   ->
                    createAlertDialog(content,pos)
                }
                layoutManager = object :
                    LinearLayoutManager(requireContext(), VERTICAL, false) {
                    override fun canScrollVertically(): Boolean {
                        return false // so that rv is not scrollable and shows additional shadow
                    }
                }
            }
            fabAddNote.setOnClickListener {
                createAlertDialog()
            }
            // textCreateFirstMemo.isVisible = arrayAdapter.isEmpty
            textCreateFirstMemo.isVisible = viewModel.getNotes().isEmpty()
            if (parentIsLock) {
                (requireActivity() as LockScreenActivity).initIcon(wdyInclude.wdyTextWhatApp)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _viewModel!!.emptyNotes()
        Log.d(logTag, "Fragment destroyed")
    }

    private fun createAlertDialog(noteText: String = "", pos: Int = -1) {
        val ad = EditNoteAD(requireContext(), noteText) { t -> onPositive(t, pos) }
        ad.show(requireActivity().supportFragmentManager, "")
    }

    // onPositive will cancel the dialog when true and will not if false is returned
    private fun onPositive(noteText: String, pos: Int): String? {
        viewModel.apply {
            Log.d(logTag, "${noteText.length} chars long and ${noteText.wordCount()} words")
            if (noteText.isBlank()) return errorMap[empty]
            else if (pos == -1) {
                if (!noteText.isLongEnough() && !noteText.isEnoughWords()) return errorMap[invalid] // min. 6 words and 28 letters (33 chars)
                else if (!noteText.isLongEnough()) return String.format(
                    errorMap[more_letters]!!,
                    minChars - noteText.length
                )
                else if (!noteText.isEnoughWords()) return String.format(
                    errorMap[more_words]!!,
                    minWords - noteText.wordCount()
                )
                else {
                    binding.notesRecyclerView.adapter!!.notifyItemInserted(pos+1)
                    addNote(noteText) // note is not yet in array
                }
            } else if (noteText.lowercase() != getNote(pos).lowercase()) return errorMap[incorrect] // did not enter the same text
            // else text was the same and task was completed
        }
        // arrayAdapter.notifyDataSetChanged()
        // binding.textCreateFirstMemo.isVisible = arrayAdapter.isEmpty
        binding.textCreateFirstMemo.isVisible = viewModel.getNotes().isEmpty()
        showSuccessAndQuit() // show success
        return null // onPositive will cancel the dialog
    }

    private fun String.isLongEnough(): Boolean {
        return this.length >= 33
    }

    private fun String.isEnoughWords(): Boolean {
        return this.trim().split(" ").size >= 6
    }

    private fun String.wordCount(): Int {
        return this.trim().split(" ").size
    }

    private fun disableViews() {
        binding.apply {
            fabAddNote.isEnabled = false
            // notesList.isEnabled = false
            notesRecyclerView.isEnabled = false
        }
    }

    private fun showSuccessAndQuit() {
        Snackbar.make(
            binding.notesCoordinatorLayout,
            getString(R.string.task_completed),
            Snackbar.LENGTH_LONG
        ).setAction("Action", null).show()
        if (parentIsLock) {
            disableViews()
            (activity as LockScreenActivity).updateLastLocked()
            Handler(Looper.getMainLooper()).postDelayed({
                (requireActivity() as LockScreenActivity).revertToMainActivity()
            }, 3000)
        }
    }
}