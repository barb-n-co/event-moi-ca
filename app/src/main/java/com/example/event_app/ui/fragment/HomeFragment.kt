package com.example.event_app.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.event_app.R
import com.example.event_app.adapter.ListMyEventsAdapter
import com.example.event_app.model.EventItem
import com.example.event_app.model.UserEventState
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.reactivex.rxkotlin.addTo
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_home.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.lang.ref.WeakReference

class HomeFragment : BaseFragment(), HomeInterface {

    private val viewModel: HomeFragmentViewModel by instance(arg = this)
    private lateinit var weakContext: WeakReference<Context>

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
        setHasOptionsMenu(true)
        setVisibilityNavBar(true)
        setFab()

        weakContext = WeakReference(context!!)

        val adapter = ListMyEventsAdapter(weakContext.get()!!, viewModel)
        val mGrid = GridLayoutManager(context, 1)
        rv_event_home_fragment.layoutManager = mGrid
        rv_event_home_fragment.itemAnimator = SlideInUpAnimator()
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
            viewModel.fetchMyEvents()
        }

        viewModel.myEventList.subscribe(
            { eventList ->
                if (eventList.isEmpty()) {
                    showPlaceHolder(adapter)
                } else {
                    displayEvents(adapter, eventList)
                }
                changeTitle(viewModel.stateUserEvent)
                swiperefresh_fragment_home.isRefreshing = false
            },
            {
                Timber.e(it)
                swiperefresh_fragment_home.isRefreshing = false
            })
            .addTo(viewDisposable)
    }

    private fun changeTitle(stateUserEvent: UserEventState) {
        when(stateUserEvent){
            UserEventState.NOTHING -> setTitleToolbar(getString(R.string.title_home))
            UserEventState.INVITATION -> setTitleToolbar(getString(R.string.title_invitation))
            UserEventState.PARTICIPATE -> setTitleToolbar(getString(R.string.title_participation))
            UserEventState.ORGANIZER -> setTitleToolbar(getString(R.string.title_organizer))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_home_fragment, menu)
        setSearchView(menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_filter -> {
                openFilter()
                true
            }
            else -> false
        }
    }

    private fun displayEvents(adapter: ListMyEventsAdapter, eventList: List<EventItem>?) {
        g_no_item_home_fragment.visibility = GONE
        adapter.submitList(eventList)
    }

    private fun showPlaceHolder(adapter: ListMyEventsAdapter) {
        g_no_item_home_fragment.visibility = VISIBLE
        adapter.submitList(emptyList())
    }

    private fun setFab() {
        fabmenu_home.setOnClickListener {
            val action = HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment()
            NavHostFragment.findNavController(this).navigate(action)
        }
//        fabmenu_home.addOnMenuItemClickListener { _, _, itemId ->
//            when (itemId) {
//                R.id.action_scan_qrcode -> {
//                    permissionManager.executeFunctionWithPermissionNeeded(
//                        activity as BaseActivity,
//                        Manifest.permission.CAMERA,
//                        { openQrCode() })
//                }
//                R.id.action_add_event -> {
//                    val action =
//                        HomeFragmentDirections.actionMyHomeFragmentToAddEventFragment()
//                    NavHostFragment.findNavController(this).navigate(action)
//                }
//            }
//        }
    }

//    private fun openQrCode() {
//        ScannerQrCodeActivity.start(activity!!)
//    }

    private fun setSearchView(menu: Menu) {
        val searchView = menu.findItem(R.id.sv_search_event).actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchEvent(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    searchEvent(it)
                }
                return false
            }
        })
    }

    private fun searchEvent(search: String) {
        if(search.isEmpty()){
            viewModel.getMyEvents()
        } else {
            viewModel.searchEvent(search)
        }
    }

    override fun getInvitation(idEvent: String) {
        viewModel.addInvitation(idEvent)
    }

    private fun openFilter() {
        val bottomSheetDialog = FilterDialogFragment(stateSelectedListener = {
            viewModel.stateUserEvent = it
            viewModel.fetchMyEvents()
        }, filterState = viewModel.stateUserEvent)
        bottomSheetDialog.show(requireFragmentManager(), TAG)
    }


    override fun onResume() {
        super.onResume()
        setTitleToolbar(getString(R.string.title_home))
        viewModel.fetchMyEvents()
    }

    override fun onDestroyView() {
        weakContext.clear()
        super.onDestroyView()
    }
}

interface HomeInterface {
    fun getInvitation(idEvent: String)
}
