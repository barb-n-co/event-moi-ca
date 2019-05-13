package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

class ShareGalleryViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    val eventList: BehaviorSubject<List<Event>> = BehaviorSubject.create()


    fun getCurrentUser(): User {
        return userRepository.currentUser.value!!
    }
    fun getEvents() {
        eventsRepository.fetchEvents().subscribe(
            {
                eventList.onNext(it)
            },
            {
                Timber.e(it)
            }).addTo(disposeBag)
    }

    fun getBitmapWithResolver(resolver: ContentResolver, uri: Uri): Bitmap {
        return MediaStore.Images.Media.getBitmap(resolver, uri)
    }

    fun putImageWithBitmap(bitmap: Bitmap, eventId: String, fromGallery: Boolean) {
        DetailEventViewModel.putImageWithBitmap(bitmap, eventId, fromGallery)
    }

    class Factory(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ShareGalleryViewModel(userRepository, eventsRepository) as T
        }
    }
}