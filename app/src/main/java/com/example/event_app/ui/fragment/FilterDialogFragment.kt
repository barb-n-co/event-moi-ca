package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.model.User
import com.example.event_app.model.UserEventState
import com.example.event_app.viewmodel.HomeFragmentViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.dialog_fragment_filter.*
import org.kodein.di.generic.instance

class FilterDialogFragment(private val stateSelectedListener: (UserEventState) -> Unit, private val filterState: UserEventState) : BottomSheetDialogFragment() {

    var cbInvitation: Boolean = false
    var cbParticipate: Boolean = false
    var cbOrganizer: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when(filterState){
            UserEventState.INVITATION -> {
                cb_filter_invitation_dialog_fragment.isChecked = true
            }
            UserEventState.PARTICIPATE -> {
                cb_filter_invitation_dialog_fragment.isChecked = true
            }
            UserEventState.ORGANIZER -> {
                cb_filter_organizer_dialog_fragment.isChecked = true
            }
        }

        b_validate_dialog_fragment.setOnClickListener {
            dismiss()
        }

        cb_filter_invitation_dialog_fragment.setOnClickListener {
            cbInvitation = cb_filter_invitation_dialog_fragment.isChecked
            changeStateCheckboxes(UserEventState.INVITATION)
        }

        cb_filter_participate_dialog_fragment.setOnClickListener {
            cbParticipate = cb_filter_participate_dialog_fragment.isChecked
            changeStateCheckboxes(UserEventState.PARTICIPATE)
        }

        cb_filter_organizer_dialog_fragment.setOnClickListener {
            cbOrganizer = cb_filter_organizer_dialog_fragment.isChecked
            changeStateCheckboxes(UserEventState.ORGANIZER)
        }
    }

    private fun changeStateCheckboxes(userStateEvent: UserEventState){
        if(!cbInvitation && !cbParticipate && !cbOrganizer){
            stateSelectedListener(UserEventState.NOTHING)
        } else {
            when(userStateEvent){
                UserEventState.INVITATION -> {
                    cb_filter_participate_dialog_fragment.isChecked = false
                    cb_filter_organizer_dialog_fragment.isChecked = false
                    stateSelectedListener(UserEventState.INVITATION)
                }
                UserEventState.PARTICIPATE -> {
                    cb_filter_invitation_dialog_fragment.isChecked = false
                    cb_filter_organizer_dialog_fragment.isChecked = false
                    stateSelectedListener(UserEventState.PARTICIPATE)
                }
                UserEventState.ORGANIZER -> {
                    cb_filter_participate_dialog_fragment.isChecked = false
                    cb_filter_invitation_dialog_fragment.isChecked = false
                    stateSelectedListener(UserEventState.ORGANIZER)
                }
            }
        }
    }
}