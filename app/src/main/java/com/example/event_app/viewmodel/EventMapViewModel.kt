package com.example.event_app.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber


class EventMapViewModel (private val eventRepository: EventRepository, private val userRepository: UserRepository): BaseViewModel(){

    val myEventList: BehaviorSubject<List<EventItem>> = BehaviorSubject.create()
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
                    response.second.map {myEvents ->
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
                                it.latitude,
                                it.longitude
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

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(mMap: GoogleMap) {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful) {
                val latitude = it.result?.latitude
                val longitude = it.result?.longitude
                if (latitude != null && longitude != null) {
                    val latLong = LatLng(latitude, longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 10f))
                }
            } else {
                Timber.e("fused error : ${it.exception}")
            }
        }
    }

    class Factory(private val eventRepository: EventRepository,private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return EventMapViewModel(eventRepository, userRepository) as T
        }
    }
}