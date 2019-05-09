package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class HomeFragmentViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    val myEventList: BehaviorSubject<List<EventItem>> = BehaviorSubject.create()

    fun getMyEvents() {
        userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.fetchMyEvents(idUser)
            Observable.combineLatest(
                eventsRepository.fetchEvents(),
                eventsRepository.fetchMyEvents(idUser),
                BiFunction<List<Event>, List<MyEvents>, Pair<List<Event>, List<MyEvents>>> { t1, t2 ->
                    Pair(
                        t1,
                        t2
                    )
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
                                it.dateStart,
                                it.dateEnd,
                                myEvents.accepted,
                                myEvents.organizer,
                                it.description,
                                it.idOrganizer,
                                it.reportedPhotoCount
                            )
                        }
                    }.filterNotNull()
                }
                .subscribe({
                    myEventList.onNext(it)
                },
                    {
                        Timber.e(it)
                    })
        }
    }

    fun addInvitation(idEvent: String) {
        userRepository.currentUser.value?.let { user ->
            user.id?.let { id ->
                user.name?.let { name ->
                    eventsRepository.addInvitation(idEvent, id, name)
                }
            }
        }
    }

    fun acceptInvitation(idEvent: String) {
        userRepository.currentUser.value?.let { user ->
            user.id?.let { id ->
                user.name?.let {name ->
                    eventsRepository.acceptInvitation(idEvent, id, name).addOnCompleteListener {
                        getMyEvents()
                    }
                }
            }
        }
    }

    fun refuseInvitation(idEvent: String) {
        userRepository.currentUser.value?.id?.let { userId ->
            eventsRepository.refuseInvitation(idEvent, userId).addOnCompleteListener {
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