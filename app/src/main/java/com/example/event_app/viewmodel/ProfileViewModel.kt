package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.NumberEvent
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ProfileViewModel(private val userRepository: UserRepository, private val eventRepository: EventRepository): BaseViewModel() {

    var user: BehaviorSubject<User> = BehaviorSubject.create()
    var eventCount: BehaviorSubject<NumberEvent> = BehaviorSubject.create()

    fun logout() {
        userRepository.fireBaseAuth.signOut()
    }

    fun deleteAccount() {
        userRepository.currentUser.value?.id?.let { idUser ->
            userRepository.deleteAccount(idUser)
            eventRepository.deleteAllEventOfUser(idUser)
        }
    }

    fun getNumberEventUser(){
        userRepository.currentUser.value?.id?.let { idUser ->
            eventRepository.myEvents.subscribe(
                {
                    val numberEvent = NumberEvent(0,0,0)
                    it.forEach {
                        if(it.accepted == 0 && it.organizer == 0){
                            numberEvent.invitation += 1
                        } else if(it.accepted == 1 && it.organizer == 0){
                            numberEvent.participate += 1
                        } else if(it.organizer == 1){
                            numberEvent.organizer += 1
                        }
                    }
                    eventCount.onNext(numberEvent)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)
        }
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