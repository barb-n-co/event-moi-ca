package com.example.event_app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.User
import com.example.event_app.repository.UserRepository
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import timber.log.Timber

class SplashScreenViewModel(private val userRepository: UserRepository) : BaseViewModel() {

    fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
        }
        return user
    }

    fun initMessageReceiving() {

        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w(task.exception, "getInstanceId failed")
                    return@OnCompleteListener
                }

                // Get new Instance ID token
                val token = task.result?.token

                // Log and toast
                val msg = "message with token = $token"
                Timber.d(msg)
            })

        FirebaseMessaging.getInstance().subscribeToTopic("notif_event_moi_ca")
            .addOnCompleteListener { task ->
                var msg = "subscribed !!!"
                if (!task.isSuccessful) {
                    msg = "failed to subscribed"
                }
                Timber.d("message for subscribing: $msg")
            }
    }

    class Factory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return SplashScreenViewModel(userRepository) as T
        }
    }
}
