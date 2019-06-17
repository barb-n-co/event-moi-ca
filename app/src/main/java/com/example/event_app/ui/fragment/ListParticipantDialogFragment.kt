package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListParticipantsAdapter
import com.example.event_app.model.User
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.dialog_fragment_list_participants.*
import timber.log.Timber


class ListParticipantDialogFragment(
    private val deleteSelectedListener: (String) -> Unit,
    private val idOrganizer: String,
    private val isNotAnOrganizer: Boolean,
    private val participants: List<User>
) : DialogFragment() {

    private var viewDisposable: CompositeDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_list_participants, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val adapter = ListParticipantsAdapter(idOrganizer, isNotAnOrganizer)
        rv_listParticipants.layoutManager = LinearLayoutManager(context)
        rv_listParticipants.adapter = adapter
        adapter.submitList(participants)

        adapter.userClickPublisher.subscribe({
            val dialog = AlertDialog.Builder(activity!!)
            dialog.setTitle(getString(R.string.delete_participant_alert_title_dialog_fragment))
                .setMessage(getString(R.string.delete_participant_alert_message_dialog_fragment))
                .setNegativeButton(getString(R.string.tv_dialogCancel_detail_event_fragment)) { _, _ -> }
                .setPositiveButton(getString(R.string.tv_dialogValidate_detail_event_fragment)) { _, _ ->
                    deleteSelectedListener(it)
                    dismiss()
                }.show()


        }, {
            Timber.e(it)
        }).addTo(viewDisposable)

        iv_closeDialog.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        viewDisposable.dispose()
        super.onDestroyView()
    }

}
