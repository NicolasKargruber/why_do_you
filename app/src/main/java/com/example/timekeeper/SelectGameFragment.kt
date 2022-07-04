package com.example.timekeeper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.timekeeper.databinding.FragmentMainBinding
import com.example.timekeeper.databinding.FragmentSelectGameBinding
import com.example.timekeeper.model.MainViewModel
import com.google.android.material.card.MaterialCardView

class SelectGameFragment : Fragment() {
    private var _binding: FragmentSelectGameBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val logTag = "SelectGameFragment"
    private var _viewModel: MainViewModel? = null
    private var matCards = mutableListOf<MaterialCardView>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSelectGameBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            gameNotes.setOnClickListener(onClickListener)
            gameNumbers.setOnClickListener(onClickListener)
            gameNull.setOnClickListener(onClickListener)

            openGame.setOnClickListener(goToNextFragment)
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val onClickListener = View.OnClickListener {
        it as MaterialCardView
        if (!matCards.contains(it)) matCards.add(it)
        it.isChecked = !it.isChecked

        //uncheck other matCards
        val checkedMatCard = matCards.filter { mC -> mC.isChecked }
        checkedMatCard.filterNot { mC -> mC == it }.forEach { cMC -> cMC.isChecked = false }
        binding.openGame.isVisible = checkedMatCard.isNotEmpty()
    }

    private val goToNextFragment = View.OnClickListener {

        val action = when (matCards.first { it.isChecked }) {
            binding.gameNumbers -> R.id.action_MainFragment_to_PuzzleFragment
            binding.gameNotes -> R.id.action_MainFragment_to_notesFragment
            else -> -1
        }

        if (action != -1) findNavController().navigate(action)
    }
}