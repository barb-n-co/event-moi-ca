package com.example.event_app.viewmodel

import android.content.ContentResolver
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.event_app.model.Event
import com.example.event_app.model.EventItem
import com.example.event_app.model.MyEvents
import com.example.event_app.model.User
import com.example.event_app.repository.EventRepository
import com.example.event_app.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import timber.log.Timber

class ShareGalleryViewModel(private val userRepository: UserRepository, private val eventsRepository: EventRepository) :
    BaseViewModel() {

    val eventList: PublishSubject<List<EventItem>> = PublishSubject.create()


    private fun getCurrentUser(): FirebaseUser? {
        val user = userRepository.fireBaseAuth.currentUser
        user?.let {
            userRepository.currentUser.onNext(User(it.uid, it.displayName, it.email))
        }
        return user
    }

    fun getMyEvents() {
        getCurrentUser()?.uid?.let { idUser ->
            Observable.combineLatest(
                eventsRepository.fetchEvents(),
                eventsRepository.fetchMyEvents(idUser),
                BiFunction<List<Event>, List<MyEvents>, Pair<List<Event>, List<MyEvents>>> { t1, t2 ->
                    Pair(t1, t2)
                }).map { response ->
                response.second.map { myEvents ->
                    val item = response.first.find { events ->
                        events.idEvent == myEvents.idEvent
                    }
                    item?.let {
                        EventItem(
                            it.idEvent,
                            it.name,
                            idUser,
                            it.organizer,
                            it.place,
                            it.dateStart,
                            it.dateEnd,
                            myEvents.accepted,
                            myEvents.organizer,
                            it.description,
                            it.idOrganizer,
                            it.reportedPhotoCount,
                            it.isEmptyEvent
                        )
                    }
                }.filterNotNull()
            }.subscribe(
                {
                    eventList.onNext(it)
                },
                {
                    Timber.e(it)
                }
            ).addTo(disposeBag)
        }
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
