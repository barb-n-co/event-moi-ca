package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.Flowable

class LoginViewModel(private val userRepository: UserRepository, private val eventRepository: EventRepository): BaseViewModel() {

    fun logIn(email: String, password: String): Flowable<Boolean> {
        return userRepository.logUser(email, password)
    }

    fun register(email: String, password: String, name: String): Flowable<Boolean> {
        return userRepository.registerUser(email, password, name)
    }

    fun resetPassword(email: String){
        userRepository.resetPassword(email)
    }

    fun checkIfFieldsAreEmpty(email: String, password: String): Boolean {
        return email.isEmpty() || password.isEmpty()
    }

    fun setEmptyEvent() {
        userRepository.currentUser.value?.id?.let {
            val event = Event(isEmptyEvent = 1, idEvent = "empty")
            eventRepository.addEvent(it, "",event)
        }

    }

    class Factory(private val userRepository: UserRepository, private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository, eventRepository) as T
        }
    }
}