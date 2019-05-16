package com.example.event_app.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_LOCATION
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.EventMapViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import org.kodein.di.generic.instance
import timber.log.Timber


class EventMapFragment: BaseFragment(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val viewModel: EventMapViewModel by instance(arg = this)
    private val displayMapItems: BehaviorSubject<Boolean> = BehaviorSubject.create()

    companion object {
        fun newInstance(): EventMapFragment = EventMapFragment()
        var displayEventOnMap: BehaviorSubject<Boolean> = BehaviorSubject.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity as MainActivity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_event_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        displayEventOnMap = displayMapItems

        displayMapItems.subscribe(
            {
                displayEventsOnMap()
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
        initMap()

    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
       setHasOptionsMenu(true)
    }


    private fun displayEventsOnMap() {
        if (mMap.isBuildingsEnabled) {

            viewModel.myEventList.subscribe(
                {
                    it.forEach { event ->
                        val position = LatLng(event.latitude, event.longitude)
                        mMap.addMarker(
                            MarkerOptions().position(position)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
                                .title(event.nameEvent)
                        )
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
        mMap = googleMap



        requestPermissions()

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
            getCurrentLocation()
        }

    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                val latitude = it.result?.latitude
                val longitude = it.result?.longitude
                if (latitude != null && longitude != null) {
                    val latLong = LatLng(latitude, longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 10f))
                }
            } else {
                Timber.e("fused error : ${it.exception}")
            }
        }
    }
}
