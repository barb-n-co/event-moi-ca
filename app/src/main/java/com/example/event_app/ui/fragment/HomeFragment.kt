package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListMyEventsAdapter
import com.example.event_app.ui.activity.ScannerQrCodeActivity
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.generic.instance
import timber.log.Timber

class HomeFragment : BaseFragment(), HomeInterface {

    private val viewModel : HomeFragmentViewModel by instance(arg = this)

    companion object {
        const val TAG = "HOMEFRAGMENT"
        fun newInstance(): HomeFragment = HomeFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(false)
        setVisibilityNavBar(true)
        setFab()

        val adapter = ListMyEventsAdapter(activity!!)
        val mLayoutManager = LinearLayoutManager(this.context)
        rv_event_home_fragment.layoutManager = mLayoutManager
        rv_event_home_fragment.itemAnimator = DefaultItemAnimator()
        rv_event_home_fragment.adapter = adapter

        swiperefresh_fragment_home.isRefreshing = false

        adapter.acceptClickPublisher.subscribe(
            {
                viewModel.acceptInvitation(it)
            },
            { Timber.e(it) }
        ).addTo(viewDisposable)

        adapter.refuseClickPublisher.subscribe(
            {
                viewModel.refuseInvitation(it)
            },
            { Timber.e(it) }
        ).addTo(viewDisposable)

        adapter.eventClickPublisher.subscribe(
            {
                val action = HomeFragmentDirections.actionMyHomeFragmentToDetailEventFragment(it)
                NavHostFragment.findNavController(this).navigate(action)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        swiperefresh_fragment_home.setOnRefreshListener {
            viewModel.getMyEvents()
        }

        viewModel.myEventList.subscribe(
            { eventList ->
                if (eventList.isEmpty()) {
                    rv_event_home_fragment.visibility = View.GONE
                    g_no_item_home_fragment.visibility = VISIBLE
                } else {
                    rv_event_home_fragment.visibility = VISIBLE
                    g_no_item_home_fragment.visibility = View.GONE
                    adapter.submitList(eventList)
                }
                swiperefresh_fragment_home.isRefreshing = false
            },
            {
                Timber.e(it)
                swiperefresh_fragment_home.isRefreshing = false
            })
            .addTo(viewDisposable)

    }

    private fun setFab() {
        fabmenu_home.addOnMenuItemClickListener { miniFab, label, itemId ->
            when (itemId) {
                R.id.action_scan_qrcode -> {
                    requestCameraPermission()
                }
                R.id.action_add_event -> {
                    val action = HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                }
            }
        }
    }

    private fun openQrCode() {
        ScannerQrCodeActivity.start(activity!!)
    }

    private fun requestCameraPermission() {
        if (permissionManager.requestCameraPermission(activity!!)) {
            openQrCode()
        }
    }

    override fun getInvitation(idEvent: String) {
        viewModel.addInvitation(idEvent)
    }

    override fun openFilter() {
        val bottomSheetDialog = FilterDialogFragment.instance
        bottomSheetDialog.show(requireFragmentManager(), TAG)
    }

    override fun onStart() {
        super.onStart()
        setTitleToolbar(getString(R.string.title_home))
        viewModel.getMyEvents()
    }
}

interface HomeInterface {
    fun getInvitation(idEvent: String)
    fun openFilter()
}