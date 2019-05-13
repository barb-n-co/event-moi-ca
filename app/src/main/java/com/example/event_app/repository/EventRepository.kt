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
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object EventRepository {

    private val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()

    val allPictures = database.reference.child("photos")
    private val eventsRef = database.reference.child("events")
    private val myEventsRef = database.reference.child("user-events")
    private val eventParticipantsRef = database.reference.child("event-participants")
    private val commentsRef = database.reference.child("commentaires")
    private val likesRef = database.reference.child("likes")
    private val userRef = database.reference.child("users")

    val myEvents: BehaviorSubject<List<MyEvents>> = BehaviorSubject.create()

    fun fetchEvents(): Observable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toObservable()
    }

    fun fetchMyEvents(idUser: String): Observable<List<MyEvents>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            myEventsRef.child(idUser), DataSnapshotMapper.listOf(MyEvents::class.java)
        ).map {
            myEvents.onNext(it)
            it.filter {
                it.idEvent?.isNotEmpty() ?: false
            }
        }.toObservable()
    }

    fun getAllUsers(): Observable<List<User>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            userRef, DataSnapshotMapper.listOf(User::class.java)
        ).toObservable()
    }

    fun getMyEvent(idUser: String, idEvent: String): Observable<MyEvents> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            myEventsRef.child(idUser).child(idEvent), MyEvents::class.java
        ).toObservable()
    }

    fun deleteAllEventOfUser(idUser: String) {
        myEventsRef.child(idUser).removeValue()
        fetchMyEvents(idUser).subscribe(
            {
                it.forEach { event ->
                    event.idEvent?.let { idEvent ->
                        eventParticipantsRef.child(idEvent).child(idUser).removeValue()
                    }
                }
            },
            {
                Timber.e(it)
            }
        )
    }

    fun addEvent(idOrganizer: String, nameOrganizer: String, event: Event) {
        myEventsRef.child(idOrganizer).child(event.idEvent).setValue(MyEvents(event.idEvent, 1, 1))
        eventParticipantsRef.child(event.idEvent).child(idOrganizer)
            .setValue(EventParticipant(idOrganizer, nameOrganizer, 1, 1))
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
    }

    fun addInvitation(idEvent: String, idUser: String, nameUser: String) {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 0, 0))
        //eventParticipantsRef.child(idEvent).child(idUser).setValue(EventParticipant(idUser, nameUser, 0, 0))
    }

    fun acceptInvitation(idEvent: String, idUser: String, nameUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 1, 0))
        return eventParticipantsRef.child(idEvent).child(idUser).setValue(EventParticipant(idUser, nameUser, 1, 0))
    }

    fun refuseInvitation(idEvent: String, idUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).removeValue()
        return eventParticipantsRef.child(idEvent).child(idUser).removeValue()
    }

    fun exitEvent(idEvent: String, idUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).removeValue()
        return eventParticipantsRef.child(idEvent).child(idUser).removeValue()
    }

    fun getEventDetail(eventId: String): Observable<Event> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef.child(eventId), Event::class.java
        ).toObservable()
    }

    fun removeLikes(pictureId: String): Task<Void> {
        return likesRef.child(pictureId).removeValue()
    }

    fun removePictureReference(eventId: String) {
        allPictures.child(eventId).removeValue()
    }

    fun removeParticipation(userId: String, eventId: String): Task<Void> {
        return myEventsRef.child(userId).child(eventId).removeValue()
    }

    fun removeEvent(eventId: String) {
        eventsRef.child(eventId).removeValue()
    }

    fun removePaticipant(eventId: String): Task<Void> {
        return eventParticipantsRef.child(eventId).removeValue()
    }

    fun getParticipants(idEvent: String): Observable<List<User>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventParticipantsRef.child(idEvent), DataSnapshotMapper.listOf(User::class.java)
        ).toObservable()
    }

    fun fetchPictures(eventId: String): Observable<List<Photo>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            allPictures.child(eventId),
            DataSnapshotMapper.listOf(Photo::class.java)
        ).toObservable()
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

    fun pushPictureReport(eventId: String, photo: Photo, reportValue: Int): Completable {
        return if (photo.id != null) {
            val updatedPhoto = photo
            updatedPhoto.isReported = reportValue
            RxFirebaseDatabase.updateChildren(allPictures.child(eventId), mapOf(Pair(photo.id, updatedPhoto)))
        } else {
            Completable.error(Throwable("error: photo id is null"))
        }
    }

    fun deletePhotoFromFireStore(photoUrl: String): Completable {
        return RxFirebaseStorage.delete(ref.child(photoUrl))
    }

    fun downloadImageFile(url: String): Maybe<ByteArray> {
        return RxFirebaseStorage.getBytes(ref.child(url), 2000 * 1000 * 360)
    }

    fun deletePhotoOrga(eventId: String, photoId: String): Task<Void> {
        return allPictures.child(eventId).child(photoId).removeValue()
    }

    fun updateEventForPhotoReporting(eventId: String, updateEvent: Event): Completable {
        return RxFirebaseDatabase.updateChildren(eventsRef, mapOf(Pair(eventId, updateEvent)))
    }

    fun getLikesFromPhoto(photoId: String) : Flowable<List<User>>{
        return RxFirebaseDatabase.observeSingleValueEvent(
            likesRef.child(photoId), DataSnapshotMapper.listOf(User::class.java)
        ).toFlowable()
    }
    fun addLikes(photoId: String, user:User): Completable {
        return if (user.id != null) {
            RxFirebaseDatabase.setValue(likesRef.child(photoId).child(user.id!!), Pair("name", user.name))
        } else {
            Completable.error(Throwable("error: user id is null"))
        }
    }

    fun addCommentToPhoto(comment: String, photoId: String, user: User): Completable {
        val date = Calendar.getInstance().get(Calendar.DATE)
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        val newDate = df.format(date)
        val pushPath = commentsRef.child(photoId).push().key!!
        val author = user.name ?: ""
        val userId = user.id!!
        val newComment = Commentaire(pushPath, author, userId, comment, photoId, newDate)
        return RxFirebaseDatabase.setValue(commentsRef.child(photoId).child(pushPath), newComment)
    }

    fun removeUserEvent(userId: String, idEvent: String) {
        myEventsRef.child(userId).child(idEvent).removeValue()
    }
}