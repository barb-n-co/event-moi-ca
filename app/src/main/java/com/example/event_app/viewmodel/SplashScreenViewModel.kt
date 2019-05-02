package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseUser

class SplashScreenViewModel(private val userRepository: UserRepository): BaseViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
        }
        return user
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SplashScreenViewModel(userRepository) as T
        }
    }
}