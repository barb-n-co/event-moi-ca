package com.example.event_app.ui.fragment

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListMyEventsAdapter
import com.example.event_app.model.EventItem
import com.example.event_app.ui.activity.ScannerQrCodeActivity
import com.example.event_app.viewmodel.HomeFragmentViewModel
import com.facebook.shimmer.ShimmerFrameLayout
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.lang.ref.WeakReference

class HomeFragment : BaseFragment(), HomeInterface {

    private val viewModel: HomeFragmentViewModel by instance(arg = this)
    private lateinit var shimmer: ShimmerFrameLayout
    private lateinit var weakContext: WeakReference<Context>
    private val handler = Handler()
    private val shimmerRunnable: Runnable = Runnable {
        viewModel.getMyEvents()
    }

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

        shimmer = shimmer_view_container
        weakContext = WeakReference(context!!)

        val adapter = ListMyEventsAdapter(weakContext.get()!!)
        val mLayoutManager = LinearLayoutManager(context)
        rv_event_home_fragment.layoutManager = mLayoutManager
        rv_event_home_fragment.itemAnimator = DefaultItemAnimator()
        rv_event_home_fragment.adapter = adapter

        swiperefresh_fragment_home.isRefreshing = false

        viewModel.loading.subscribe(
            {
                pb_home_fragment.visibility = if (it) VISIBLE else GONE
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

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
            shimmer.startShimmer()
            handler.postDelayed(shimmerRunnable, 1000L)
        }

        viewModel.myEventList.subscribe(
            { eventList ->
                if (eventList.isEmpty()) {
                    showPlaceHolder()
                } else {
                    displayEvents(adapter, eventList)
                }
                swiperefresh_fragment_home.isRefreshing = false
            },
            {
                Timber.e(it)
                swiperefresh_fragment_home.isRefreshing = false
            })
            .addTo(viewDisposable)

    }

    private fun displayEvents(adapter: ListMyEventsAdapter, eventList: List<EventItem>?) {
        rv_event_home_fragment.visibility = VISIBLE
        g_no_item_home_fragment.visibility = GONE
        adapter.submitList(eventList)
        shimmer.stopShimmer()
    }

    private fun showPlaceHolder() {
        rv_event_home_fragment.visibility = GONE
        g_no_item_home_fragment.visibility = VISIBLE
    }

    private fun setFab() {
        fabmenu_home.addOnMenuItemClickListener { miniFab, label, itemId ->
            when (itemId) {
                R.id.action_scan_qrcode -> {
                    requestCameraPermission()
                }
                R.id.action_add_event -> {
                    val action =
                        HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment()
                    NavHostFragment.findNavController(this).navigate(action)
                }
            }
        }
    }

    private fun openQrCode() {
        ScannerQrCodeActivity.start(activity!!)
    }

    override fun getInvitation(idEvent: String) {
        viewModel.addInvitation(idEvent)
    }

    override fun openFilter() {
        val bottomSheetDialog = FilterDialogFragment(stateSelectedListener = {
            viewModel.stateUserEvent = it
            viewModel.getMyEvents()
        }, filterState = viewModel.stateUserEvent)
        bottomSheetDialog.show(requireFragmentManager(), TAG)
    }

    private fun requestCameraPermission() {
        if (permissionManager.requestCameraPermission(activity!!)) {
            openQrCode()
        }
    }

    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_home))
        displayFilterMenu(true)
    }

    override fun onStart() {
        super.onStart()
        setTitleToolbar(getString(R.string.title_home))
        shimmer.startShimmer()
        handler.postDelayed(shimmerRunnable, 1000L)
        pb_home_fragment.visibility = VISIBLE
    }

    override fun onPause() {
        super.onPause()
        displayFilterMenu(false)
    }

    override fun onDestroyView() {
        weakContext.clear()
        handler.removeCallbacks(shimmerRunnable)
        super.onDestroyView()
    }

}

interface HomeInterface {
    fun getInvitation(idEvent: String)
    fun openFilter()
}
