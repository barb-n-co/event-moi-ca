package com.example.event_app.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListEventAdapter
import com.example.event_app.adapter.ListInvitationAdapter
import com.example.event_app.model.Event
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_invitation.*
import kotlinx.android.synthetic.main.fragment_myevents.*
import org.kodein.di.direct
import org.kodein.di.generic.instance
import timber.log.Timber

private lateinit var viewModel: HomeFragmentViewModel

class MyEventsFragment : BaseFragment() {
    companion object {
        const val TAG = "MYEVENTSFRAGMENT"
        fun newInstance(): MyEventsFragment = MyEventsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_myevents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = kodein.direct.instance(arg = this)

        swiperefresh_fragment_myevents.setOnRefreshListener { viewModel.getEvents() }

        viewModel.eventList.subscribe(
            {
                initAdapter(it)
                swiperefresh_fragment_myevents.isRefreshing = false
            },
            {
                Timber.e(it)
                swiperefresh_fragment_myevents.isRefreshing = false
            })
            .addTo(viewDisposable)

        viewModel.getEvents()
    }

    private fun initAdapter(eventList: List<Event>) {
        val adapter = ListEventAdapter()
        val mLayoutManager = LinearLayoutManager(this.context)
        rv_myevents_fragment.layoutManager = mLayoutManager
        rv_myevents_fragment.itemAnimator = DefaultItemAnimator()
        rv_myevents_fragment.adapter = adapter
        adapter.submitList(eventList)
        swiperefresh_fragment_myevents.isRefreshing = false

        adapter.refuseClickPublisher.subscribe(
            {
                //viewModel.acceptInvitation(it)
                Toast.makeText(context, "Quitter événement", Toast.LENGTH_SHORT).show()
            },
            { Timber.e(it) }
        ).addTo(viewDisposable)
    }
}