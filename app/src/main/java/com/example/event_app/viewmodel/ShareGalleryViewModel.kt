package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ShareGalleryViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    lateinit var currentUser: User
    val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()


    fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
            userRepository.getUserNameFromFirebase()
            currentUser = userRepository.currentUser.value!!
        }
        return user
    }
    fun getEvents() {
        eventsRepository.fetchEvents().subscribe(
            {
                eventList.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShareGalleryViewModel(userRepository, eventsRepository) as T
        }
    }
}