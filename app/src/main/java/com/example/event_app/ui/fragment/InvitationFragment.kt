package com.example.event_app.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListInvitationAdapter
import com.example.event_app.model.Event
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_invitation.*
import org.kodein.di.direct
import org.kodein.di.generic.instance
import timber.log.Timber

private lateinit var viewModel: HomeFragmentViewModel

class InvitationFragment : BaseFragment() {

    companion object {
        const val TAG = "INVITATIONFRAGMENT"
        fun newInstance(): InvitationFragment = InvitationFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_invitation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        com.example.event_app.ui.fragment.viewModel = kodein.direct.instance(arg = this)

        swiperefresh_fragment_invitation.setOnRefreshListener { viewModel.getEventsInvitations() }

        Log.d(MyEventsFragment.TAG, "fragment")

        viewModel.invitationList.subscribe(
            {
                initAdapter(it)
                swiperefresh_fragment_invitation.isRefreshing = false
            },
            {
                Timber.e(it)
                swiperefresh_fragment_invitation.isRefreshing = false
            })
            .addTo(viewDisposable)
    }

    private fun initAdapter(eventList: List<Event>) {
        val adapter = ListInvitationAdapter()
        val mLayoutManager = LinearLayoutManager(this.context)
        rv_invitation_fragment.layoutManager = mLayoutManager
        rv_invitation_fragment.itemAnimator = DefaultItemAnimator()
        rv_invitation_fragment.adapter = adapter
        adapter.submitList(eventList)
        swiperefresh_fragment_invitation.isRefreshing = false

        adapter.acceptClickPublisher.subscribe(
            {
                viewModel.acceptInvitation(it)
                Toast.makeText(context, "invitation ACCEPTEE", Toast.LENGTH_SHORT).show()
            },
            { Timber.e(it) }
        ).addTo(viewDisposable)

        adapter.refuseClickPublisher.subscribe(
            {
                viewModel.refuseInvitation(it)
                Toast.makeText(context, "invitation REFUSEE", Toast.LENGTH_SHORT).show()
            },
            { Timber.e(it) }
        ).addTo(viewDisposable)
    }

    //auto refresh
    override fun onStart() {
        super.onStart()
        viewModel.getEventsInvitations()
    }
}