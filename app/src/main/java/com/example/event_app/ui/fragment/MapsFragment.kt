package com.example.event_app.ui.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.PermissionChecker
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
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
import kotlinx.android.synthetic.main.fragment_add_event.*
import kotlinx.android.synthetic.main.fragment_maps.*
import org.kodein.di.generic.instance

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main
.activity_main.*

class MapsFragment : BaseFragment(), OnMapReadyCallback{

    private lateinit var mMap: GoogleMap

    val viewModel : MapsViewModel by instance(arg = this)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var lastLocation: Location
    companion object {
        const val TAG = "MAPSFRAGMENT"
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
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
        setVisibilityToolbar(false)

        iv_back_menu_maps.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        iv_search_menu.setOnClickListener {
            viewModel.searchAdress(et_search_menu.text.toString())
        }

        viewModel.mapAdress.subscribe(
            {addressMap ->
                btn_maps.visibility = View.VISIBLE
                val sydney = addressMap.lat?.let { it1 -> addressMap.lng?.let { it2 -> LatLng(it1, it2) } }
               // mMap.addMarker(sydney?.let { it1 -> MarkerOptions().position(it1).title("Marker in Sydney") })
                  AddEventFragment.lieu=  addressMap.address
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,16.0f))

                btn_maps.setOnClickListener {
                    btn_maps.visibility = View.INVISIBLE
                 val action = MapsFragmentDirections.actionMapsFragmentToAddEventFragment(addressMap.address ?: getString(R.string.chip_adresse))
                    val options = NavOptions.Builder().setPopUpTo(R.id.add_event_fragment, true).build()
                    NavHostFragment.findNavController(this).navigate(action,options)
                }



            },
            {

            }

        ).addTo(viewDisposable)
    }

    private fun initMap(){

        var mapFragment : SupportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setHasOptionsMenu(true)
    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
   }

    override fun onStop() {
        super.onStop()
        setVisibilityToolbar(true)
    }
}


