package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.EventRepository
import com.example.event_app.ui.fragment.ModifyEventFragment

class ModifyEventViewModel(private val eventRepository: EventRepository): BaseViewModel(){



    class Factory(private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ModifyEventViewModel(eventRepository) as T
        }
    }

}