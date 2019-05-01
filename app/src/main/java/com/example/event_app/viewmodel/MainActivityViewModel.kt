package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository

class MainActivityViewModel(private val userRepository: UserRepository) : BaseViewModel() {

    fun logout() {
        userRepository.fireBaseAuth.signOut()
    }

    fun getCurrentUser(): User? {
        return userRepository.currentUser
    }

    fun getCurrentUserToDisplay(): String {
        val email = userRepository.currentUser?.email ?: ""
        return "Bienvenue !\n$email"
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return MainActivityViewModel(userRepository) as T
        }
    }
}