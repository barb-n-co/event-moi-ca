package com.example.event_app.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.example.event_app.utils.getLatLng
import com.example.event_app.viewmodel.MapsViewModel
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.util.*




class AddAddressMapFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel: MapsViewModel by instance(arg = this)
    private lateinit var mMap: GoogleMap

    companion object {
        fun newInstance(): AddAddressMapFragment = AddAddressMapFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMap()

        iv_back_menu_maps.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        viewModel.mapAddress.subscribe(
            { addressMap ->
                btn_maps.visibility = View.VISIBLE
                val latLng = addressMap.getLatLng()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
                latLng?.let {
                    mMap.clear()
                    mMap.addMarker(viewModel.createMarker(latLng, addressMap, context!!))
                }

                btn_maps.setOnClickListener {
                    btn_maps.visibility = View.GONE
                    val intent = viewModel.createAddressIntent(addressMap, getString(R.string.chip_adresse))
                    targetFragment?.onActivityResult(targetRequestCode, RESULT_OK, intent)
                    fragmentManager?.popBackStack()
                }
            },
            { error ->
                Timber.e(error)
            }

        ).addTo(viewDisposable)

        val autocompleteFragment = childFragmentManager.findFragmentById(R.id.autocomplete_event_fragment) as AutocompleteSupportFragment?

        autocompleteFragment?.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG))

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                viewModel.searchAddress("${place.address}")
            }

            override fun onError(status: Status) {
                Timber.i( "An error occurred: $status")
            }
        })
    }

    private fun initMap() {
        val mapFragment: SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    override fun onResume() {
        super.onResume()
        setVisibilityToolbar(false)
    }

    override fun onStop() {
        super.onStop()
        setVisibilityToolbar(true)
    }
}


