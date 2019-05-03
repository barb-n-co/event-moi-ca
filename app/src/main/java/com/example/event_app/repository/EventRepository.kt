package com.example.event_app.repository

import android.util.Log
import com.example.event_app.model.Event
import com.example.event_app.model.EventInvitation
import com.example.event_app.model.Photo
import com.gojuno.koptional.toOptional
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseChildEvent
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    val allImgRef = ref.child("allImages")
    private val database = FirebaseDatabase.getInstance()
    private var currentEventId = ""

    private var invitations: BehaviorSubject<List<String>> = BehaviorSubject.create()

    private val eventsRef = database.reference.child("events")
    private val eventsInvitationsRef = database.reference.child("events-invitations")
    private val myEventsRef = database.reference.child("my-events")
    private val photoRef = database.reference.child("photos")

    init {
        fetchEventsInvitations()
    }

    fun fetchEvents(): Observable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toObservable()
    }

    fun fetchEventsInvitations(): Observable<List<EventInvitation>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsInvitationsRef, DataSnapshotMapper.listOf(EventInvitation::class.java)
        ).toObservable()
    }

    fun addEvent(idOrganizer: String, event: Event) {
        myEventsRef.child(event.idEvent).push().setValue(idOrganizer)
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
    }

    fun addInvitation(idEvent: String, idUser: String) {
        eventsInvitationsRef.push().setValue(EventInvitation(idEvent, idUser))
    }

    fun getEventDetail(eventId: String): Maybe<Event> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef.child(eventId), Event::class.java
        )
    }

    fun getPhotoDetail(eventId: String, photoId: Int): Maybe<Photo> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            photoRef.child(eventId).child(photoId.toString()), Photo::class.java
        )
    }

    fun setCurrentEvent(id: String) {
        currentEventId = id
    }

    fun getCurrentEventId(): String? {
        return currentEventId
    }
}