package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.Observable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class AddEventFragmentViewModel(
    private val userRepository: UserRepository,
    private val eventsRepository: EventRepository
) : BaseViewModel() {

    fun addEventFragment(
        idEvent: String, organizer: String,
        name: String, place: String,
        description: String, startDateString: String,
        endDateString: String, latitude: Double, longitude: Double
    ) {
        userRepository.currentUser.value?.let { user ->
            user.id?.let { id ->
                user.name?.let { userName ->
                    eventsRepository.addEvent(
                        id, userName,
                        Event(
                            idEvent = idEvent, idOrganizer = id,
                            organizer = organizer, name = name,
                            place = place, description = description,
                            dateStart = startDateString, dateEnd = endDateString,
                            latitude = latitude, longitude = longitude
                        )
                    )
                }

            }
        }
    }

    fun getEventInfo(eventId: String): Observable<Event> {
        return eventsRepository.getEventDetail(eventId)
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddEventFragmentViewModel(userRepository, eventsRepository) as T
        }
    }
}
