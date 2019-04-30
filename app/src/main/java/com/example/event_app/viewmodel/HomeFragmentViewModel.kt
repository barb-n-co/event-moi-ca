package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import io.reactivex.subjects.BehaviorSubject

class HomeFragmentViewModel(private val eventsRepository: EventRepository) : BaseViewModel() {
    val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()

    fun getEvents() {
        eventsRepository.fectchEvents()
            .
    }

    class Factory(private val eventsRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return HomeFragmentViewModel(eventsRepository) as T
        }
    }
}