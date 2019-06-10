package com.example.event_app.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.R
import com.example.event_app.model.AddressMap
import com.example.event_app.repository.MapsRepository
import com.example.event_app.ui.fragment.AddEventFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class MapsViewModel(private val mapsRepository: MapsRepository) : BaseViewModel() {

    var mapAddress: PublishSubject<AddressMap> = PublishSubject.create()

    fun observeAddressResults() {
        mapsRepository.mapAddress.subscribe(
            {
                mapAddress.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)

    }

    fun searchAddress(adr: String) {
        mapsRepository.buildRequestWithAddress(adr)
    }

    fun createMarker(latLng: LatLng, addressMap: AddressMap, context: Context): MarkerOptions {
        return MarkerOptions()
            .position(latLng)
            .icon(bitmapDescriptorFromVector(context, R.drawable.ic_camera_4))
            .title(addressMap.address)
    }

    private fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
        val background = ContextCompat.getDrawable(context, R.drawable.ic_address_48dp)
        background!!.setBounds(0, 0, background.intrinsicWidth, background.intrinsicHeight)
        val vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId)
        vectorDrawable!!.setBounds(45, 28, vectorDrawable.intrinsicWidth + 50, vectorDrawable.intrinsicHeight + 33)
        val bitmap = Bitmap.createBitmap(background.intrinsicWidth, background.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        background.draw(canvas)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    fun createAddressIntent(addressMap: AddressMap, placeHolder: String): Intent {
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
