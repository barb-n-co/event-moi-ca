package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository

class AddEventFragmentViewModel(private val eventsRepository: EventRepository) : BaseViewModel() {

    fun addEventFragment(event: Event){
        eventsRepository.addEvent(event)
    }

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return AddEventFragmentViewModel(eventsRepository) as T
        }
    }
}