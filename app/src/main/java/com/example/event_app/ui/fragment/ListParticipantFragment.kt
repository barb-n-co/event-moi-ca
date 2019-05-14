package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListParticipantsAdapter
import com.example.event_app.model.User
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.list_participants_popup.*
import timber.log.Timber


class ListParticipantFragment(
    private val deleteSelectedListener: (String) -> Unit,
    private val idOrganizer: String,
    private val isNotAnOrga: Boolean,
    private val participants: List<User>
) : DialogFragment() {

    private lateinit var listParticipantsAdapter: ListParticipantsAdapter
    private var viewDisposable : CompositeDisposable = CompositeDisposable()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.list_participants_popup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mLinear = LinearLayoutManager(context)
        listParticipantsAdapter = ListParticipantsAdapter(context!!, idOrganizer, isNotAnOrga)
        rv_listParticipants.layoutManager = mLinear
        rv_listParticipants.adapter = listParticipantsAdapter
        listParticipantsAdapter.submitList(participants)
        val itemDecor = DividerItemDecoration(context, VERTICAL)
        rv_listParticipants.addItemDecoration(itemDecor)

        listParticipantsAdapter.userClickPublisher.subscribe({
            deleteSelectedListener(it)
            dismiss()
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