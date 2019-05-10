package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.example.event_app.ui.fragment.ModifyEventFragment
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ModifyEventViewModel(private val eventsRepository: EventRepository, private val userRepository: UserRepository): BaseViewModel(){

    val event: BehaviorSubject<EventItem> = BehaviorSubject.create()

    companion object {

        var eventsRepository = EventRepository
        var userRepository = UserRepository}

    fun getEventInfo(eventId: String) {
        userRepository.currentUser.value?.id?.let { idUser ->
            eventsRepository.getEventDetail(eventId)
            Observable.combineLatest(
                eventsRepository.getEventDetail(eventId),
                eventsRepository.getMyEvent(idUser, eventId),
                BiFunction<Event, MyEvents, Pair<Event, MyEvents>> { t1, t2 ->
                    Pair(
                        t1,
                        t2
                    )
                }).map { response ->
                EventItem(
                    response.first.idEvent,
                    response.first.name,
                    idUser,
                    response.first.organizer,
                    response.first.place,
                    response.first.dateStart,
                    response.first.dateEnd,
                    response.second.accepted,
                    response.second.organizer,
                    response.first.description,
                    response.first.idOrganizer
                )

            }.subscribe({
                event.onNext(it)
            },
                {
                    Timber.e(it)
                }).addTo(disposeBag)

        }

    }

    class Factory(private val eventsRepository: EventRepository, private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ModifyEventViewModel(eventsRepository,userRepository) as T
        }
    }

}