package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import io.reactivex.Flowable

class LoginViewModel(private val userRepository: UserRepository): BaseViewModel() {

    fun logIn(email: String, password: String): Flowable<Boolean> {
        return userRepository.logUser(email, password)
    }

    fun register(email: String, password: String, name: String): Flowable<Boolean> {
        return userRepository.registerUser(email, password, name)
    }

    fun resetPassword(email: String){
        userRepository.resetPassword(email)
    }

    fun getFirebaseAuth(): FirebaseAuth {
        return userRepository.fireBaseAuth
    }

    fun getUsersRef(): DatabaseReference {
        return userRepository.usersRef
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository) as T
        }
    }
}