package com.example.event_app.repository

import com.example.event_app.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()

    val allPictures = database.reference.child("photos")
    private val eventsRef = database.reference.child("events")
    private val myEventsRef = database.reference.child("user-events")
    private val eventParticipantsRef = database.reference.child("event-participants")
    private val commentsRef = database.getReference("commentaires")

    fun fetchEvents(): Observable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toObservable()
    }

    fun fetchMyEvents(idUser: String): Observable<List<MyEvents>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            myEventsRef.child(idUser), DataSnapshotMapper.listOf(MyEvents::class.java)
        ).map {
            it.filter {
                it.idEvent?.isNotEmpty() ?: false
            }
        }.toObservable()
    }

    fun addEvent(idOrganizer: String, nameOrganizer: String, event: Event) {
        myEventsRef.child(idOrganizer).child(event.idEvent).setValue(MyEvents(event.idEvent, 1, 1))
        eventParticipantsRef.child(event.idEvent).child(idOrganizer).setValue(EventParticipant(idOrganizer, nameOrganizer, 1, 1))
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
    }

    fun addInvitation(idEvent: String, idUser: String, nameUser: String) {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 0, 0))
        eventParticipantsRef.child(idUser).child(idEvent).setValue(EventParticipant(idUser, nameUser, 0, 0))
    }

    fun acceptInvitation(idEvent: String, idUser: String, nameUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 1, 0))
        return eventParticipantsRef.child(idUser).child(idEvent).setValue(EventParticipant(idUser, nameUser, 1, 0))
    }

    fun refuseInvitation(idEvent: String, idUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).removeValue()
        return eventParticipantsRef.child(idEvent).child(idUser).removeValue()
    }

    fun getParticipants(idEvent: String): Observable<List<User>>{
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventParticipantsRef.child(idEvent), DataSnapshotMapper.listOf(User::class.java)
        ).toObservable()
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