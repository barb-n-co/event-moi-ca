package com.example.event_app.repository


import android.content.Context
import android.content.SharedPreferences
import com.example.event_app.BuildConfig
import com.example.event_app.model.AddressMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import io.reactivex.subjects.PublishSubject
import timber.log.Timber
import java.util.*

private const val LATITUDE = "lastLatitude"
private const val LONGITUDE = "lastLongitude"

class MapsRepository(context: Context) {
    private var placesClient: PlacesClient
    var mapAddress: PublishSubject<AddressMap> = PublishSubject.create()
    var sharedPref: SharedPreferences? = null

    init {
        Places.initialize(context, BuildConfig.GOOGLE_MAP_API_KEY)
        placesClient = Places.createClient(context)
    }

    fun getLastLocation(): LatLng? {
        var latLng: LatLng? = null
        sharedPref?.let { sharedPreferences ->

            val lastLatitudeSaved = sharedPreferences.getString(LATITUDE, "")
            val lastLongitudeSaved = sharedPreferences.getString(LONGITUDE, "")

            if (!lastLatitudeSaved.isNullOrEmpty() && !lastLongitudeSaved.isNullOrEmpty()) {
                latLng = LatLng(lastLatitudeSaved.toDouble(), lastLongitudeSaved.toDouble())
            }
        }
        return latLng
    }

    fun saveLastLocation(latitude: Double, longitude: Double) {
        sharedPref?.let { sharedPreferences ->
            val newLocationToSave = sharedPreferences.edit()
            newLocationToSave.putString(LATITUDE, latitude.toString())
            newLocationToSave.putString(LONGITUDE, longitude.toString())
            newLocationToSave.apply()
        }
    }

    fun buildRequestWithAddress(address: String) {
        val token = AutocompleteSessionToken.newInstance()

        val request = FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)
            .setQuery(address)
            .build()

        placesClient.findAutocompletePredictions(request).addOnSuccessListener { response ->
            val placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
            if (response.autocompletePredictions.isNotEmpty()) {

                val placeRequest = FetchPlaceRequest.builder(
                    response.autocompletePredictions.first().placeId,
                    placeFields
                )
                    .build()
                placesClient.fetchPlace(placeRequest).addOnSuccessListener { placeResponse ->
                    placeResponse.place.let { place ->

                        val addressmap = AddressMap(
                            place.id,
                            place.name,
                            place.address,
                            place.latLng?.latitude,
                            place.latLng?.longitude
                        )
                        mapAddress.onNext(addressmap)
                    }
                }
            }

        }.addOnFailureListener { exception ->
            Timber.e("Place not found: ${exception.localizedMessage}")
        }
    }
}

