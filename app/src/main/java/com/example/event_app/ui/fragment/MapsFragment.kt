package com.example.event_app.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.event_app.R
import com.example.event_app.utils.getLatLng
import com.example.event_app.utils.hideKeyboard
import com.example.event_app.viewmodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance
import timber.log.Timber


class MapsFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel: MapsViewModel by instance(arg = this)
    private lateinit var mMap: GoogleMap

    companion object {
        const val requestCodeMapFragment = 201

        fun newInstance(): MapsFragment = MapsFragment()

        var fragmentManager: FragmentManager? = null
        fun popBack() {
            fragmentManager?.popBackStack()
        }
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

        MapsFragment.fragmentManager = fragmentManager
        initMap()
        viewModel.searchAdress()

        iv_back_menu_maps.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        iv_search_menu.setOnClickListener {
            viewModel.searchAdress(et_search_menu.text.toString())
            view.hideKeyboard()
        }

        viewModel.mapAdress.subscribe(
            { addressMap ->
                btn_maps.visibility = View.VISIBLE
                val latLng = addressMap.getLatLng()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f))
                latLng?.let {
                    mMap.clear()
                    mMap.addMarker(viewModel.createMarker(latLng, addressMap))
                }

                btn_maps.setOnClickListener {
                    btn_maps.visibility = View.INVISIBLE
                    val intent = viewModel.createIntentWithExtra(addressMap, getString(R.string.chip_adresse))
                    targetFragment?.onActivityResult(requestCodeMapFragment, RESULT_OK, intent)
                    fragmentManager?.popBackStack()
                }
            },
            { error ->
                Timber.e(error)
            }

        ).addTo(viewDisposable)
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


