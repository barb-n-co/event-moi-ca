package com.example.event_app.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.R
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


class EventMapViewModel(private val eventRepository: EventRepository, private val userRepository: UserRepository) :
    BaseViewModel() {

    val myEventList: BehaviorSubject<List<EventItem>> = BehaviorSubject.create()
    val currentLocation: PublishSubject<LatLng> = PublishSubject.create()
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    fun getMyEvents() {
        userRepository.currentUser.value?.id?.let { idUser ->
            Observable.combineLatest(
                eventRepository.fetchEvents(),
                eventRepository.fetchMyEvents(idUser),
                BiFunction<List<Event>, List<MyEvents>, Pair<List<Event>, List<MyEvents>>> { t1, t2 ->
                    Pair(t1, t2)
                })
                .map { response ->
                    response.second.map { myEvents ->
                        val item = response.first.find { events ->
                            events.idEvent == myEvents.idEvent
                        }
                        item?.let {
                            EventItem(
                                it.idEvent,
                                it.name,
                                idUser,
                                it.organizer,
                                it.place,
                                it.dateStart,
                                it.dateEnd,
                                myEvents.accepted,
                                myEvents.organizer,
                                it.description,
                                it.idOrganizer,
                                it.reportedPhotoCount,
                                it.isEmptyEvent,
                                it.organizerPhoto,
                                it.latitude,
                                it.longitude,
                                organizerPhotoReference = eventRepository.getStorageReferenceForUrl(it.organizerPhoto)
                            )
                        }
                    }.filterNotNull()
                }.subscribe({
                    myEventList.onNext(it)
                },
                    {
                        Timber.e(it)
                    }).addTo(disposeBag)
        }
    }

    fun getStorageRef(url: String): StorageReference {
        return eventRepository.getStorageReferenceForUrl(url)
    }

    fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorDrawableResourceId: Int): BitmapDescriptor {
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

    @SuppressLint("MissingPermission")
    fun getCurrentLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                val latitude = it.result?.latitude
                val longitude = it.result?.longitude
                if (latitude != null && longitude != null) {
                    val latLong = LatLng(latitude, longitude)
                    currentLocation.onNext(latLong)
                }
            } else {
                Timber.e("fused error : ${it.exception}")
            }
        }
    }

    fun setRegion(list: List<EventItem>): LatLngBounds {
        var latitudeMin = 90.0
        var latitudeMax = -90.0
        var longitudeMin = 180.0
        var longitudeMax = -180.0

        list.forEach {
            if (it.latitude < latitudeMin) latitudeMin = it.latitude
            if (it.longitude < longitudeMin) longitudeMin = it.longitude
            if (it.latitude > latitudeMax) latitudeMax = it.latitude
            if (it.longitude > longitudeMax) longitudeMax = it.longitude
        }
        if (latitudeMin - 0.5 >= -90) latitudeMin -= 0.5
        if (latitudeMax + 0.5 <= 90) latitudeMax += 0.5
        if (longitudeMin - 1 >= -180) longitudeMin -= 1
        if (longitudeMax + 1 <= 180) longitudeMax += 1

        return LatLngBounds(LatLng(latitudeMin, longitudeMin), LatLng(latitudeMax, longitudeMax))
    }

    fun createMapIntent(address: String): Intent {
        return Uri.parse(address).let { location ->
            Intent(Intent.ACTION_VIEW, location)
        }
    }


    class Factory(private val eventRepository: EventRepository, private val userRepository: UserRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EventMapViewModel(eventRepository, userRepository) as T
        }
    }
}
