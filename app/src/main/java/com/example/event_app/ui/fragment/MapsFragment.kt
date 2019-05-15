package com.example.event_app.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.example.event_app.R
import com.example.event_app.utils.hideKeyboard
import com.example.event_app.viewmodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance
import timber.log.Timber


class MapsFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel : MapsViewModel by instance(arg = this)
    private lateinit var mMap: GoogleMap

    companion object {
        const val TAG = "MAPSFRAGMENT"
        const val requestCodeMapFragment = 201

        fun newInstance(): MapsFragment = MapsFragment()

        var fragmentManager: FragmentManager? = null
        fun popBack() {
            fragmentManager?.popBackStack()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        MapsFragment.fragmentManager = fragmentManager

        initMap()
        viewModel.searchAdress()
        setVisibilityToolbar(false)

        iv_back_menu_maps.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        iv_search_menu.setOnClickListener {
            viewModel.searchAdress(et_search_menu.text.toString())
            view.hideKeyboard()
        }

        viewModel.mapAdress.subscribe(
            {addressMap ->
                btn_maps.visibility = View.VISIBLE
                val address = addressMap.lat?.let { it1 -> addressMap.lng?.let { it2 -> LatLng(it1, it2) } }
                  AddEventFragment.lieu=  addressMap.address
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(address,12.0f))
                address?.let {
                    mMap.clear()
                    val marker = MarkerOptions().position(address)
                    mMap.addMarker(marker)
                }

                btn_maps.setOnClickListener {
                    btn_maps.visibility = View.INVISIBLE
                    val intent = Intent().putExtra(AddEventFragment.TAG, addressMap.address ?: getString(R.string.chip_adresse))
                    targetFragment?.onActivityResult(requestCodeMapFragment, RESULT_OK, intent)
                    fragmentManager?.popBackStack()
                }
            },
            { error ->
                Timber.e(error)
            }

        ).addTo(viewDisposable)
    }

    private fun initMap(){
        val mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
   }

    override fun onStop() {
        super.onStop()
        setVisibilityToolbar(true)
    }


}


