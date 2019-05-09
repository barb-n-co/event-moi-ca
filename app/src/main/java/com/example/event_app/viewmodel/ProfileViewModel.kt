package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ProfileViewModel(private val userRepository: UserRepository, private val eventRepository: EventRepository): BaseViewModel() {

    var user: BehaviorSubject<User> = BehaviorSubject.create()

    fun logout() {
        userRepository.fireBaseAuth.signOut()
    }

    fun getCurrentUser() {
        userRepository.currentUser.subscribe(
            {
                user.onNext(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
    }

    class Factory(private val userRepository: UserRepository, private val eventRepository: EventRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(userRepository, eventRepository) as T
        }
    }
}