package com.example.event_app.ui.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.example.event_app.R
import com.example.event_app.viewmodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance
import timber.log.Timber


class MapsFragment : BaseFragment(), OnMapReadyCallback {

    private val viewModel : MapsViewModel by instance(arg = this)
    private lateinit var mMap: GoogleMap

    companion object {
        const val TAG = "MAPSFRAGMENT"
        fun newInstance(): MapsFragment = MapsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMap()
        viewModel.searchAdress()

        viewModel.mapAdress.subscribe(
            {addressMap ->
                btn_maps.visibility = View.VISIBLE
                val sydney = addressMap.lat?.let { it1 -> addressMap.lng?.let { it2 -> LatLng(it1, it2) } }
                  AddEventFragment.lieu=  addressMap.address
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16.0f))

                btn_maps.setOnClickListener {
                    btn_maps.visibility = View.INVISIBLE
                 val action = MapsFragmentDirections.actionMapsFragmentToAddEventFragment(addressMap.address ?: getString(R.string.chip_adresse))
                    val options = NavOptions.Builder().setPopUpTo(R.id.add_event_fragment, true).build()
                    NavHostFragment.findNavController(this).navigate(action,options)
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

    override fun onStart() {
        super.onStart()
        displaySearchViewMenu(true)
    }

    override fun onStop() {
        super.onStop()
        displaySearchViewMenu(false)
    }

}



