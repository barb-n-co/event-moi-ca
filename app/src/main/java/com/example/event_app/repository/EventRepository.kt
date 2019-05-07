package com.example.event_app.repository

import com.example.event_app.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Completable
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
    private val pictureReportRef = database.reference.child("reported-pictures")
    private val commentsRef: DatabaseReference = EventRepository.database.getReference("commentaires")

    fun fetchEvents(): Observable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toObservable()
    }

    fun fetchEventsInvitations(): Observable<List<EventInvitation>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsInvitationsRef, DataSnapshotMapper.listOf(EventInvitation::class.java)
        ).map {
            it.filter { it.key?.isNotEmpty() ?: true }
        }.toObservable()
    }

    fun fetchMyEvents(): Observable<List<MyEvents>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            myEventsRef, DataSnapshotMapper.listOf(MyEvents::class.java)
        ).map {
            it.filter { it.key?.isNotEmpty() ?: true }
        }.toObservable()
    }

   fun addEvent(idOrganizer: String, event: Event) {
       val key = myEventsRef.push().key
       key?.let {
           myEventsRef.child(it).setValue(MyEvents(it, event.idEvent, idOrganizer))
           RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
       }
    }

    fun addInvitation(idEvent: String, idUser: String) {
        val key = eventsInvitationsRef.push().key
        key?.let {
            eventsInvitationsRef.child(it).setValue(EventInvitation(it, idEvent, idUser))
        }
    }

    fun acceptInvitation(key: String, idEvent: String, idUser: String): Task<Void>{
        eventsInvitationsRef.child(key).removeValue()
        return myEventsRef.child(key).setValue(MyEvents(key, idEvent, idUser))
    }

    fun refuseInvitation(key: String): Task<Void> {
        return eventsInvitationsRef.child(key).removeValue()
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

    fun pushPictureReport(eventId: String, photo: Photo): Completable {
        return RxFirebaseDatabase.setValue(pictureReportRef.child(eventId).push(), photo)
    }

    fun deletePhotoFromFireStore(photoUrl: String): Completable {
        return RxFirebaseStorage.delete(ref.child(photoUrl))
    }

    fun downloadImageFile(url: String): Maybe<ByteArray> {
        return RxFirebaseStorage.getBytes(ref.child(url), 2000*1000*4)
    }

    fun deletePhotoOrga(eventId: String, photoId: String): Task<Void> {
        return allPictures.child(eventId).child(photoId).removeValue()
    }
}