package com.example.event_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.UserRepository
import timber.log.Timber

class LoginViewModel(private val userRepository: UserRepository): BaseViewModel() {

    fun logIn(email: String, password: String){
        userRepository.logUser(email, password).subscribe(
            {
                Log.e("TEST", it.toString())
            },
            {
                Timber.e(it)
            }
        )
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(userRepository) as T
        }
    }
}