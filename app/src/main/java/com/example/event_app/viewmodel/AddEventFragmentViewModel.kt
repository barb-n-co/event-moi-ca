package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository

class AddEventFragmentViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) : BaseViewModel() {

    fun addEventFragment(idEvent: String, organizer: String, name: String, description: String, startDateString: String, endDateString: String){
        userRepository.currentUser.value?.let {user ->
            user.id?.let { id ->
                user.name?.let { userName ->
                    eventsRepository.addEvent(id, name, Event(idEvent, id, organizer, name, description, startDateString, endDateString))
                }
            }
        }
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddEventFragmentViewModel(userRepository, eventsRepository) as T
        }
    }
}