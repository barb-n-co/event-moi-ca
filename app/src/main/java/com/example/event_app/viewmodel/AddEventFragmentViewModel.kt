package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import io.reactivex.Observable
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
            user.id?.let { idUser ->
                user.name?.let { userName ->
                    eventsRepository.addEvent(
                        userName,
                        Event(
                            idEvent = idEvent, idOrganizer = idUser,
                            organizer = organizer, name = name,
                            place = place, description = description,
                            dateStart = startDateString, dateEnd = endDateString,
                            latitude = latitude, longitude = longitude, organizerPhoto = user.photoUrl
                        )
                    )
                    initMessageReceiving(idEvent)
                }

            }
        }
    }

    fun getEventInfo(eventId: String): Observable<Event> {
        return eventsRepository.getEventDetail(eventId)
    }

    private fun initMessageReceiving(eventId: String) {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "getInstanceId failed%s")
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                val msg = "message with token = $token"
                Timber.d(msg)
            })

        FirebaseMessaging.getInstance().subscribeToTopic(eventId)
            .addOnCompleteListener { task ->
                var msg = "subscribed to $eventId!!!"
                if (!task.isSuccessful) {
                    msg = "failed to subscribed"
                }
                Timber.d("message for subscribing: $msg")
            }
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddEventFragmentViewModel(userRepository, eventsRepository) as T
        }
    }
}
