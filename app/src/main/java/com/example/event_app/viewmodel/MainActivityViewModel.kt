package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.MapsRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class MainActivityViewModel(private val userRepository: UserRepository,private val mapsRepository: MapsRepository) : BaseViewModel() {

    var user: BehaviorSubject<User> = BehaviorSubject.create()

    fun getCurrentUser() {
        userRepository.currentUser.subscribe(
            {
                user.onNext(it)
            },
            {
                Timber.e(it)
            }
        )
    }

    fun searchAdress (adr : String){
        mapsRepository.getPositionWithAdress(adr)
    }

    class Factory(private val userRepository: UserRepository,private val mapsRepository: MapsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(userRepository,mapsRepository) as T
        }
    }



}