package com.example.yeebum.screens.flows_control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.example.yeebum.R
import com.example.yeebum.models.ActionType
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_add_action.*

class AddActionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_action, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listOfOptions =
            arrayListOf<LinearLayout>(addColorAction, addPauseAction, addColorTempAction)
        val listOfData =
            arrayListOf<ActionType>(ActionType.Color, ActionType.Sleep, ActionType.ColorTemp)

        listOfOptions.forEach { layout ->
            layout.setOnClickListener {
                findNavController().navigate(
                    AddActionFragmentDirections.actionAddActionFragmentToActionDetailsFragment().actionId,
                    bundleOf(
                        "actionType" to Gson().toJson(listOfData[listOfOptions.indexOf(layout)]),
                        "flow" to arguments?.getString("flow")
                    )
                )
            }
        }

        addActionBackButton.setOnClickListener {
            requireActivity().onBackPressed()
        }


    }


}