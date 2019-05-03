package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.UserResponse
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository

class AddEventFragmentViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) : BaseViewModel() {

    fun addEventFragment(idEvent: String, organizer: String, name: String, description: String, startDateString: String, endDateString: String){
        userRepository.currentUser.value?.id?.let {
            eventsRepository.addEvent(it, Event(idEvent, it, organizer, name, description, startDateString, endDateString))
        }
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddEventFragmentViewModel(userRepository, eventsRepository) as T
        }
    }
}