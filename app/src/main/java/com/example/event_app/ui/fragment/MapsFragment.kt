package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.*
import com.example.event_app.R
import com.example.event_app.ui.activity.MainActivity
import com.example.event_app.viewmodel.MapsViewModel
import com.google.android.gms.dynamic.SupportFragmentWrapper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance

class MapsFragment : BaseFragment(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap

    val viewModel : MapsViewModel by instance(arg = this)

    companion object {
        const val TAG = "MAPSFRAGMENT"
        fun newInstance(): MapsFragment = MapsFragment()
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {



        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initMap(savedInstanceState)
        (activity as MainActivity).updateToolbar(true)

        viewModel.mapAdress.subscribe(
            {

            },
            {

            }

        )
    }


    private fun initMap(savedInstanceState: Bundle?){

        var mapFragment : SupportMapFragment = childFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney)) //To change body of created functions use File | Settings | File Templates.
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_maps, menu)
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onDestroyView() {
        ( activity as MainActivity).updateToolbar(false)
        super.onDestroyView()

    }




}