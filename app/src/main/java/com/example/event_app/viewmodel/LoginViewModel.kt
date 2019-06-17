package com.example.event_app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.R
import com.example.event_app.model.Event
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.NotificationRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.Flowable
import io.reactivex.rxkotlin.addTo
import timber.log.Timber
import java.io.ByteArrayOutputStream


class LoginViewModel(
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val notificationRepository: NotificationRepository
) : BaseViewModel() {

    fun logIn(email: String, password: String): Flowable<Boolean> {
        return userRepository.logUser(email, password)
    }

    fun register(email: String, password: String, name: String, context: Context): Flowable<Boolean> {
        userRepository.currentUser.subscribe(
            {
                putPlaceHolderWithBitmap(it, it.id!!, context)
            },
            {
                Timber.e(it)
            }
        ).addTo(disposeBag)
        return userRepository.registerUser(email, password, name)
    }

    fun resetPassword(email: String) {
        userRepository.resetPassword(email)
    }

    fun checkIfFieldsAreEmpty(email: String, password: String): Boolean {
        return email.isEmpty() || password.isEmpty()
    }

    fun setEmptyEvent() {
        userRepository.currentUser.value?.id?.let {
            val event = Event(isEmptyEvent = 1, idEvent = "empty")
            eventRepository.addEvent("", event)
            notificationRepository.createNotificationChannel(it)
        }

    }

    fun initNotificationChannel() {
        userRepository.currentUser.value?.id?.let {
            notificationRepository.createNotificationChannel(it)
        }
    }

    private fun generateProfilePlaceHolder(context: Context): Bitmap {
        val imageBitmap = BitmapFactory.decodeResource(
            context.resources,
            R.drawable.profile_placeholder
        )
        return Bitmap.createScaledBitmap(imageBitmap, 200, 200, false)
    }

    private fun putPlaceHolderWithBitmap(currentUser: User, userId: String, context: Context) {
        val bitmap = generateProfilePlaceHolder(context)
        val data: ByteArray
        val baos = ByteArrayOutputStream()


        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        data = baos.toByteArray()


        eventRepository.putBytesToFireStoreForUserPhotoProfile(userId, data, userId)
            .subscribe(
                { snapshot ->
                    val url = snapshot.metadata!!.path
                    currentUser.photoUrl = url

                    userRepository.updateUser(currentUser.id!!, currentUser.email!!, currentUser.name!!, url)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)

    }

    class Factory(
        private val userRepository: UserRepository,
        private val eventRepository: EventRepository,
        private val notificationRepository: NotificationRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository, eventRepository, notificationRepository) as T
        }
    }
}
