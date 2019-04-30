package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.UserRepository
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class MainActivityViewModel(private val userRepository: UserRepository) : BaseViewModel() {


    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(userRepository) as T
        }
    }
}