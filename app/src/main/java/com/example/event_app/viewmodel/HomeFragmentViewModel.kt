package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.model.UserEventState
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.storage.StorageReference
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class HomeFragmentViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    val myEventList: PublishSubject<List<EventItem>> = PublishSubject.create()
    var stateUserEvent = UserEventState.NOTHING

    init {
        eventsRepository.myEventsItem.subscribe(
            {
                myEventList.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    fun getMyEvents() {
        userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.fetchEventsItem(idUser, stateUserEvent)
                .subscribe(
                {
                    myEventList.onNext(it)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)
        }
    }

    fun getStorageRef(url: String): StorageReference {
        return eventsRepository.getStorageReferenceForUrl(url)
    }

    fun addInvitation(idEvent: String) {
        userRepository.currentUser.value?.let { user ->
            user.id?.let { id ->
                eventsRepository.addInvitation(idEvent, id)
            }
        }
    }

    fun acceptInvitation(idEvent: String) {
        userRepository.currentUser.value?.let { user ->
            user.id?.let { id ->
                user.name?.let { name ->
                    eventsRepository.acceptInvitation(idEvent, id, name).addOnCompleteListener {
                        getMyEvents()
                    }
                }
            }
        }
    }

    fun refuseInvitation(idEvent: String) {
        userRepository.currentUser.value?.id?.let { userId ->
            eventsRepository.exitEvent(idEvent, userId).addOnCompleteListener {
                getMyEvents()
            }
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
