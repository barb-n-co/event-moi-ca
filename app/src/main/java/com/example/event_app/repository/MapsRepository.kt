package com.example.event_app.repository


import android.content.Context
import com.example.event_app.BuildConfig
import com.example.event_app.model.AddressMap
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*


class MapsRepository(private val context: Context) {
    var placesClient: PlacesClient
    var i: Int = 0
    var mapAdress: PublishSubject<AddressMap> = PublishSubject.create()

    init {
        Places.initialize(context, BuildConfig.GOOGLE_MAP_API_KEY)
        placesClient = Places.createClient(context)
    }


    fun getPositionWithAdress(address: String) {
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(address)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            if (response.autocompletePredictions.isNotEmpty()) {

                val request = FetchPlaceRequest.builder(
                    response.autocompletePredictions.first().placeId,
                    placeFields
                )
                    .build()
                placesClient.fetchPlace(request).addOnSuccessListener { response ->
                    response.place.let { place ->

                        val addressmap = AddressMap(
                            place.id,
                            place.name,
                            place.address,
                            place.latLng?.latitude,
                            place.latLng?.longitude
                        )
                        mapAdress.onNext(addressmap)
                    }
                }
            }

        }.addOnFailureListener { exception ->
            Timber.e("Place not found: ${exception.localizedMessage}")
        }
    }
}

