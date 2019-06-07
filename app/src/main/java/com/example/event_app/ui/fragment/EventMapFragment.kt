package com.example.event_app.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.event_app.R
import com.example.event_app.adapter.CustomInfoWindowGoogleMap
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_LOCATION
import com.example.event_app.model.EventItem
import com.example.event_app.model.spannable
import com.example.event_app.model.url
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.EventMapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.bottom_sheet_detail_event_map.*
import org.kodein.di.generic.instance
import timber.log.Timber


class EventMapFragment : BaseFragment(), OnMapReadyCallback, EventMapFragmentInterface {


    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private lateinit var googleEventMap: GoogleMap
    private val viewModel: EventMapViewModel by instance(arg = this)

    companion object {
        fun newInstance(): EventMapFragment = EventMapFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity as MainActivity)

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
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
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
                                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                            )
                            marker.tag = event
                            val region = viewModel.setRegion(it)
                            /** center region on all events */
                            googleEventMap.moveCamera(CameraUpdateFactory.newLatLngBounds(region, 0))
                        }
                    } else {
                        viewModel.getCurrentLocation()
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
        requestPermissions()

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
                    .load(viewModel.getStorageRef(event.organizerPhoto))
                    .transition(GenericTransitionOptions.with(R.anim.fade_in))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .placeholder(R.drawable.ic_profile)
                    .into(iv_organizer_detail_bottom_sheet_map)
            }

        }
        tv_event_name_detail_bottom_sheet_map.text = event.nameEvent
        tv_organizer_detail_bottom_sheet_map.text = event.nameOrganizer
        tv_address_detail_bottom_sheet_map.text = spannable { url("", event.place) }//event.place
        tv_start_event_detail_bottom_sheet_map.text = event.dateStart
        tv_finish_event_detail_bottom_sheet_map.text = event.dateEnd
        tv_description_detail_bottom_sheet_map.text = event.description
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED

        tv_address_detail_bottom_sheet_map.setOnClickListener {
            val query = tv_start_event_detail_bottom_sheet_map.text.toString()
            val address = getString(R.string.map_query, query)
            if (query.isNotEmpty()) {
                startActivity(viewModel.createMapIntent(address))
            }

        }
    }


    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        requestPermissions(permissions, PERMISSION_LOCATION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_LOCATION && grantResults.size == 2) {
            viewModel.getCurrentLocation()
        }

    }

    override fun displayMapItems() {
        displayEventsOnMap()
    }

}

interface EventMapFragmentInterface {
    fun displayMapItems()
}
