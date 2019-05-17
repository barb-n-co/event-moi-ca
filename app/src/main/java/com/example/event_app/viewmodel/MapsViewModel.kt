package com.example.event_app.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.R
import com.example.event_app.model.AddressMap
import com.example.event_app.repository.MapsRepository
import com.example.event_app.ui.fragment.AddEventFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MapsViewModel (private val mapsRepository: MapsRepository): BaseViewModel(){

    var mapAdress: PublishSubject<AddressMap> = PublishSubject.create()

    fun searchAdress() {
        mapsRepository.mapAdress.subscribe(
            {
                mapAdress.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)

    }

    fun searchAdress(adr : String){
        mapsRepository.getPositionWithAdress(adr)
    }

    fun createMarker(latLng: LatLng, addressMap: AddressMap): MarkerOptions {
        return MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.map_pin))
            .title(addressMap.address)
    }

    fun createIntentWithExtra(addressMap: AddressMap, placeHolder: String): Intent {
        val intent = Intent()
        intent.putExtra(AddEventFragment.ADDRESS_TAG, addressMap.address ?: placeHolder)
        intent.putExtra(AddEventFragment.LAT_TAG, addressMap.lat ?: 0.0)
        intent.putExtra(AddEventFragment.LONG_TAG, addressMap.lng ?: 0.0)
        return intent
    }

    class Factory(private val mapsRepository: MapsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MapsViewModel(mapsRepository) as T
        }
    }

}
