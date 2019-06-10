package com.example.event_app.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
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
            eventRepository.addEvent(it, "", event)
            notificationRepository.createNotificationChannel(it)
        }

    }

    fun initNotificationChannel() {
        userRepository.currentUser.value?.id?.let {
            notificationRepository.createNotificationChannel(it)
        }
    }

    private fun putPlaceHolderWithBitmap(currentUser: User, userId: String, context: Context) {
        val drawable = context.resources.getDrawable(R.drawable.ic_profile_black, null)
        val bitmap = drawableToBitmap(drawable)
        val data: ByteArray
        val baos = ByteArrayOutputStream()

        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, baos)
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

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {

        val bitmap: Bitmap? = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ALPHA_8)
        }

        if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                return drawable.bitmap
            }
        }

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }


    class Factory(private val userRepository: UserRepository,
                  private val eventRepository: EventRepository,
                  private val notificationRepository: NotificationRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userRepository, eventRepository, notificationRepository) as T
        }
    }
}
