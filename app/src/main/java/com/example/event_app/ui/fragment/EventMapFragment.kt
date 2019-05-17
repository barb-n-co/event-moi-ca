package com.example.event_app.ui.fragment

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.adapter.CustomInfoWindowGoogleMap
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_LOCATION
import com.example.event_app.model.EventItem
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.EventMapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxkotlin.addTo
import org.kodein.di.generic.instance
import timber.log.Timber


class EventMapFragment : BaseFragment(), OnMapReadyCallback, EventMapFragmentInterface {


    private lateinit var googleEventMap: GoogleMap
    private val viewModel: EventMapViewModel by instance(arg = this)

    companion object {
        fun newInstance(): EventMapFragment = EventMapFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity as MainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_event_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMap()
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }


    private fun displayEventsOnMap() {
        if (googleEventMap.isBuildingsEnabled) {

            viewModel.myEventList.subscribe(
                {
                    googleEventMap.clear()
                    it.forEach { event ->

                        val position = LatLng(event.latitude, event.longitude)
                        val marker = googleEventMap.addMarker(
                            MarkerOptions().position(position)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                            //.title(event.nameEvent)
                        )
                        marker.tag = event
                        marker.showInfoWindow()
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
        }

        googleEventMap.setInfoWindowAdapter(customInfoWindow)
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
            viewModel.getCurrentLocation(googleEventMap)
        }

    }

    override fun displayMapItems() {
        displayEventsOnMap()
    }

}

interface EventMapFragmentInterface {
    fun displayMapItems()
}
