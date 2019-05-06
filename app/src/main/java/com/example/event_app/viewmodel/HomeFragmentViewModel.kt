package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventInvitation
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class HomeFragmentViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()
    val invitationList: BehaviorSubject<List<Event>> = BehaviorSubject.create()

    fun getEvents() {
        eventsRepository.fetchEvents().subscribe(
            {
                eventList.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)
    }

    fun getEventsInvitations() {
        userRepository.currentUser.value?.id?.let { idUser ->
            Observable.combineLatest(
                eventsRepository.fetchEvents(),
                eventsRepository.fetchEventsInvitations(),
                BiFunction<List<Event>, List<EventInvitation>, Pair<List<Event>, List<EventInvitation>>> { t1, t2 ->
                    Pair(
                        t1,
                        t2
                    )
                })
                .map { response ->
                    response.second.filter {
                        it.idUser == idUser
                    }.map {
                        response.first.find {first ->
                            first.idEvent == it.idEvent
                        }
                    }.filterNotNull()
                }
                .subscribe({
                    invitationList.onNext(it)
                },
                    {
                        Timber.e(it)
                    })
        }
    }

    fun addInvitation(idEvent: String) {
        userRepository.currentUser.value?.id?.let {
            eventsRepository.addInvitation(idEvent, it)
        }
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeFragmentViewModel(userRepository, eventsRepository) as T
        }
    }
}