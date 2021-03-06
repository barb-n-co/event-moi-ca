package com.example.event_app.ui.fragment

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import com.bumptech.glide.GenericTransitionOptions
import com.example.event_app.R
import com.example.event_app.adapter.CustomInfoWindowGoogleMap
import com.example.event_app.model.EventItem
import com.example.event_app.model.spannable
import com.example.event_app.model.url
import com.example.event_app.utils.GlideApp
import com.example.event_app.utils.getLatLng
import com.example.event_app.viewmodel.EventMapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.bottom_sheet_detail_event_map.*
import org.kodein.di.generic.instance
import timber.log.Timber


class EventMapFragment : BaseFragment(), OnMapReadyCallback {


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var googleEventMap: GoogleMap
    private val viewModel: EventMapViewModel by instance(arg = this)

    companion object {
        fun newInstance(): EventMapFragment = EventMapFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        activity?.getSharedPreferences(context?.getString(R.string.preferences_key_file), Context.MODE_PRIVATE)?.let {
            viewModel.sharedPreferences.onNext(it)
        }

        viewModel.fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)

        viewModel.currentLocation.subscribe(
            {
                googleEventMap.moveCamera(CameraUpdateFactory.newLatLngZoom(it, 10f))
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet_event_detail_map)
        initMap()

        viewModel.mapAddress.subscribe(
            { addressMap ->
                val latLng = addressMap.getLatLng()
                googleEventMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
            },
            { error ->
                Timber.e(error)
            }

        ).addTo(viewDisposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_event_map_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        setSearchView(menu)
        super.onPrepareOptionsMenu(menu)
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    private fun setSearchView(menu: Menu) {
        val searchView = menu.findItem(R.id.sv_search_event_map).actionView as SearchView
        searchView.queryHint = getString(R.string.tv_hint_search_event_map)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchAddress(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })
    }


    private fun displayEventsOnMap() {
        if (::googleEventMap.isInitialized) {
            viewModel.myEventList.subscribe(
                {
                    googleEventMap.clear()
                    if (it.isNotEmpty()) {
                        it.forEach { event ->

                            val position = LatLng(event.latitude, event.longitude)
                            val marker = googleEventMap.addMarker(
                                MarkerOptions().position(position)
                                    .icon(viewModel.bitmapDescriptorFromVector(context!!, R.drawable.ic_camera_4))
                            )
                            marker.tag = event
                            val region = viewModel.setRegion(it)
                            /** center region on all events */
                            googleEventMap.moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0))
                        }
                    } else {
                        requestCurrentLocation()
                    }

                },
                {
                    Timber.e(it)
                }
            ).addTo(viewDisposable)

            viewModel.getMyEvents()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleEventMap = googleMap
        requestCurrentLocation()
        displayEventsOnMap()

        val customInfoWindow = CustomInfoWindowGoogleMap(context = requireContext())
        googleMap.setOnInfoWindowClickListener {
            val event: EventItem = it.tag as EventItem
            showBottomSheetDetails(event)
        }

        googleEventMap.setInfoWindowAdapter(customInfoWindow)
    }

    private fun showBottomSheetDetails(event: EventItem) {
        context?.let {

            if (event.organizerPhoto.isNotEmpty()) {
                GlideApp
                    .with(it)
                    .load(event.organizerPhotoReference)
                    .transition(GenericTransitionOptions.with(R.anim.fade_in))
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(iv_organizer_detail_bottom_sheet_map)
            }

        }
        tv_event_name_detail_bottom_sheet_map.text = event.nameEvent
        tv_organizer_detail_bottom_sheet_map.text = event.nameOrganizer
        tv_address_detail_bottom_sheet_map.text = spannable { url("", event.place) }
        tv_start_event_detail_bottom_sheet_map.text = event.dateStart
        tv_finish_event_detail_bottom_sheet_map.text = event.dateEnd
        tv_description_detail_bottom_sheet_map.text = event.description
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        tv_address_detail_bottom_sheet_map.setOnClickListener {
            val query = tv_start_event_detail_bottom_sheet_map.text.toString()
            val address = getString(R.string.map_query, query)
            if (query.isNotEmpty()) {
                viewModel.createMapIntent(address)?.let {
                    startActivity(it)
                }
            }

        }
    }

    private fun requestCurrentLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        permissionManager.executeFunctionWithPermissionNeeded(
            this as BaseFragment,
            permissions,
            { viewModel.getCurrentLocation() }
        )
    }

    override fun onResume() {
        super.onResume()
        //displayEventsOnMap()
    }

    override fun onStart() {
        super.onStart()
        setTitleToolbar(getString(R.string.title_event_map))
    }
}

