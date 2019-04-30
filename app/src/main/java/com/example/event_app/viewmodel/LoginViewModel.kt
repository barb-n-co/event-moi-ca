package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.UserRepository
import io.reactivex.Flowable
import timber.log.Timber

class LoginViewModel(private val userRepository: UserRepository): BaseViewModel() {

    fun logIn(email: String, password: String): Flowable<Boolean> {
        return userRepository.logUser(email, password)
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(userRepository) as T
        }
    }
}