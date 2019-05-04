package com.example.event_app.repository

import com.example.event_app.model.Commentaire
import com.example.event_app.model.Event
import com.example.event_app.model.EventInvitation
import com.example.event_app.model.Photo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()
    val allPictures = database.reference.child("photos")
    private val eventsRef = database.reference.child("events")
    private val eventsInvitationsRef = database.reference.child("events-invitations")
    private val myEventsRef = database.reference.child("my-events")
    private var invitations: BehaviorSubject<List<String>> = BehaviorSubject.create()
    private val commentsRef: DatabaseReference = EventRepository.database.getReference("commentaires")


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

    fun getPhotoDetail(eventId: String, photoId: String): Maybe<Photo> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            allPictures.child(eventId).child(photoId), Photo::class.java
        )

    }

    fun fetchCommentaires(photoId: String): Flowable<List<Commentaire>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            commentsRef.child(photoId), DataSnapshotMapper.listOf(Commentaire::class.java)
        ).toFlowable()
    }
}