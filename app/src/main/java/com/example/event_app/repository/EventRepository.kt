package com.example.event_app.repository

import com.example.event_app.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()

    val allPictures = database.reference.child("photos")
    private val pictureReportRef = database.reference.child("reported-pictures")
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
        eventParticipantsRef.child(idUser).child(idEvent).setValue(EventParticipant(idEvent, nameUser, 0, 0))
    }

    fun acceptInvitation(idEvent: String, idUser: String, nameUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 1, 0))
        return eventParticipantsRef.child(idUser).child(idEvent).setValue(EventParticipant(idEvent, nameUser, 1, 0))
    }

    fun refuseInvitation(idEvent: String, idUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).removeValue()
        return eventParticipantsRef.child(idEvent).child(idUser).removeValue()
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
        return if (photo.id != null) {
            RxFirebaseDatabase.setValue(pictureReportRef.child(eventId).child(photo.id!!), photo)
        } else {
            Completable.error(Throwable("error: photo id is null"))
        }

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

    fun getReportedPicturesForEventList(list: List<EventItem>): List<Observable<MutableList<Photo>>> {
        return list.map {event ->
            RxFirebaseDatabase.observeSingleValueEvent(pictureReportRef.child(event.idEvent), DataSnapshotMapper.listOf(Photo::class.java)).toObservable()
        }

    }

}