package com.example.event_app.repository

import com.example.event_app.model.*
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import durdinapps.rxfirebase2.RxFirebaseStorage
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val BUCKET = "gs://event-moi-ca.appspot.com"
private const val PHOTOS = "photos"
private const val EVENTS = "events"
private const val USER_EVENTS = "user-events"
private const val EVENT_PARTICIPANT = "event-participants"
private const val COMMENTS = "commentaires"
private const val LIKES = "likes"
private const val COMMENTS_LIKES = "comment-likes"

object EventRepository {

    private val db = FirebaseStorage.getInstance(BUCKET)
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()

    private val allPictures = database.reference.child(PHOTOS)
    private val eventsRef = database.reference.child(EVENTS)
    private val myEventsRef = database.reference.child(USER_EVENTS)
    private val eventParticipantsRef = database.reference.child(EVENT_PARTICIPANT)
    private val commentsRef = database.reference.child(COMMENTS)
    private val likesRef = database.reference.child(LIKES)
    private val commentLikesRef = database.reference.child(COMMENTS_LIKES)

    val myEvents: BehaviorSubject<List<MyEvents>> = BehaviorSubject.create()
    val myEventsItem: BehaviorSubject<List<EventItem>> = BehaviorSubject.create()

    //region FireStore

    fun getStorageReferenceForUrl(url: String): StorageReference {
        return ref.child(url)
    }

    //end region

    //region Event

    fun fetchEventsItem(idUser: String, stateUserEvent: UserEventState): Observable<List<EventItem>> {
        val obs = Observable.combineLatest(
            fetchEvents(),
            fetchMyEvents(idUser),
            BiFunction<List<Event>, List<MyEvents>, Pair<List<Event>, List<MyEvents>>> { t1, t2 ->
                Pair(t1, t2)
            }).map { response ->
            response.second.filter {
                when {
                    stateUserEvent.equals(UserEventState.NOTHING) -> true
                    stateUserEvent.equals(UserEventState.INVITATION) && it.organizer == 0 && it.accepted == 0 -> true
                    stateUserEvent.equals(UserEventState.PARTICIPATE) && it.organizer == 0 && it.accepted == 1 -> true
                    stateUserEvent.equals(UserEventState.ORGANIZER) && it.organizer == 1 && it.accepted == 1 -> true
                    else -> false
                }
            }.map { myEvents ->
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
                        it.isEmptyEvent,
                        it.organizerPhoto
                    )
                }
            }.filterNotNull()
        }

        obs.subscribe(
            {
                myEventsItem.onNext(it)
            },
            {
                Timber.e(it)
            }
        )

        return obs
    }

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
                it.idEvent?.isNotEmpty() ?: false && it.isEmtyEvent == 0
            }
        }.toObservable()
    }

    fun getMyEvent(idUser: String, idEvent: String): Observable<MyEvents> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            myEventsRef.child(idUser).child(idEvent), MyEvents::class.java
        ).toObservable()
    }

    fun deleteAllEventOfUser(idUser: String): Observable<List<MyEvents>> {
        myEventsRef.child(idUser).removeValue()
        return fetchMyEvents(idUser)
    }


    fun addEvent(idOrganizer: String, nameOrganizer: String, event: Event) {
        if (event.isEmptyEvent == 0) {
            myEventsRef.child(idOrganizer).child(event.idEvent).setValue(MyEvents(event.idEvent, 1, 1))
        } else {
            myEventsRef.child(idOrganizer).child(event.idEvent).setValue(MyEvents(event.idEvent, 1, 1, 1))
        }
        eventParticipantsRef.child(event.idEvent).child(idOrganizer)
            .setValue(EventParticipant(idOrganizer, nameOrganizer, 1, 1))
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
    }

    fun exitEvent(idEvent: String, idUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).removeValue()
        return eventParticipantsRef.child(idEvent).child(idUser).removeValue()
    }

    fun exitMyEvent(idEvent: String, idUser: String): Task<Void> {
        return myEventsRef.child(idUser).child(idEvent).removeValue()
    }

    fun getEventDetail(eventId: String): Observable<Event> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef.child(eventId), Event::class.java
        ).toObservable()
    }

    fun removeEvent(eventId: String) {
        eventsRef.child(eventId).removeValue()
    }

    fun removeUserEvent(userId: String, idEvent: String) {
        myEventsRef.child(userId).child(idEvent).removeValue()
    }

    // endregion

    // region Invitation

    fun addInvitation(idEvent: String, idUser: String) {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 0, 0))
    }

    fun acceptInvitation(idEvent: String, idUser: String, nameUser: String): Task<Void> {
        myEventsRef.child(idUser).child(idEvent).setValue(MyEvents(idEvent, 1, 0))
        return eventParticipantsRef.child(idEvent).child(idUser).setValue(EventParticipant(idUser, nameUser, 1, 0))
    }

    // endregion

    // region Participant

    fun deleteParticipantWithId(list: List<MyEvents>, idUser: String) {
        list.forEach { event ->
            event.idEvent?.let { idEvent ->
                eventParticipantsRef.child(idEvent).child(idUser).removeValue()
            }
        }
    }

    fun removeParticipant(eventId: String): Task<Void> {
        return eventParticipantsRef.child(eventId).removeValue()
    }

    fun getParticipants(idEvent: String): Observable<List<User>> {
        return RxFirebaseDatabase.observeValueEvent(
            eventParticipantsRef.child(idEvent), DataSnapshotMapper.listOf(User::class.java)
        ).toObservable()
    }

    fun removeParticipation(userId: String, eventId: String): Task<Void> {
        return myEventsRef.child(userId).child(eventId).removeValue()
    }

    // endregion

    // region Like

    fun removeLikes(pictureId: String): Task<Void> {
        return likesRef.child(pictureId).removeValue()
    }

    fun getLikesFromPhoto(photoId: String): Flowable<List<LikeItem>> {
        return RxFirebaseDatabase.observeValueEvent(
            likesRef.child(photoId), DataSnapshotMapper.listOf(LikeItem::class.java)
        )
    }

    fun setNewLike(userId: String, photoId: String): Completable {
        val item = LikeItem(userId, photoId)
        return RxFirebaseDatabase.setValue(likesRef.child(photoId).child(userId), item)
    }


    fun deleteLike(userId: String, photoId: String): Task<Void> {
        return likesRef.child(photoId).child(userId).removeValue()
    }

    fun addNewCommentLike(userId: String, commentId: String, photoId: String): Completable {
        commentLikesRef.push().key?.let {
            val item = LikeComment(userId, commentId, it)
            return RxFirebaseDatabase.setValue(commentLikesRef.child(photoId).child(it), item)
        } ?: return Completable.complete()
    }

    fun removeCommentLike(likeId: String, photoId: String): Task<Void> {
        return commentLikesRef.child(photoId).child(likeId).removeValue()
    }

    fun getCommentLikes(photoId: String): Flowable<List<LikeComment>> {
        return RxFirebaseDatabase.observeValueEvent(
            commentLikesRef.child(photoId), DataSnapshotMapper.listOf(LikeComment::class.java)
        ).map {
            it.filter {
                it.userId.isNotEmpty()
            }
        }
    }

    //endregion

    // region Picture

    fun removePictureReference(eventId: String) {
        allPictures.child(eventId).removeValue()
    }

    fun fetchPictures(eventId: String): Observable<List<Photo>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            allPictures.child(eventId),
            DataSnapshotMapper.listOf(Photo::class.java)
        ).toObservable()
    }

    fun getAllPicturesStream(eventId: String): Observable<List<Photo>> {
        return RxFirebaseDatabase.observeValueEvent(
            allPictures.child(eventId),
            DataSnapshotMapper.listOf(Photo::class.java)
        ).toObservable()
    }

    fun createPicturePath(eventId: String): DatabaseReference {
        return allPictures.child(eventId).push()
    }

    fun pushImageToDatabase(id: String, pushPath: DatabaseReference, value: Photo): Completable {
        return RxFirebaseDatabase.setValue(allPictures.child(id), pushPath.setValue(value))
    }

    fun putBytesToFireStore(eventId: String, data: ByteArray, photoName: String): Flowable<UploadTask.TaskSnapshot> {
        return RxFirebaseStorage.putBytes(ref.child("$eventId/$photoName.jpeg"), data)
            .toFlowable()
    }

    fun putBytesToFireStoreForUserPhotoProfile(
        userId: String,
        data: ByteArray,
        photoName: String
    ): Flowable<UploadTask.TaskSnapshot> {
        return RxFirebaseStorage.putBytes(ref.child("$userId/$photoName.jpeg"), data)
            .toFlowable()
    }

    fun getPhotoDetail(eventId: String, photoId: String): Maybe<Photo> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            allPictures.child(eventId).child(photoId), Photo::class.java
        )
    }

    fun pushPictureReport(eventId: String, photo: Photo, reportValue: Int): Completable {
        photo.isReported = reportValue
        return RxFirebaseDatabase.updateChildren(allPictures.child(eventId), mapOf(Pair(photo.id, photo)))
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

    // endregion

    //region Comment

    fun fetchCommentaires(photoId: String): Flowable<List<Commentaire>> {
        return RxFirebaseDatabase.observeValueEvent(
            commentsRef.child(photoId), DataSnapshotMapper.listOf(Commentaire::class.java)
        )
    }

    fun addCommentToPhoto(comment: String, photoId: String, user: User): Completable {
        val date = Calendar.getInstance().time
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy à HH:mm", Locale.FRANCE)
        val newDate = df.format(date)
        val pushPath = commentsRef.child(photoId).push().key!!
        val author = user.name ?: ""
        val userId = user.id!!
        val userProfileImage = user.photoUrl
        val newComment =
            Commentaire(pushPath, author, userId, comment, photoId, newDate, profileImage = userProfileImage)
        return RxFirebaseDatabase.setValue(commentsRef.child(photoId).child(pushPath), newComment)
    }

    fun deleteCommentOfPhoto(photoId: String, commentId: String): Task<Void> {
        return commentsRef.child(photoId).child(commentId).removeValue()
    }

    fun editCommentOfPhoto(comment: Commentaire): Completable {
        return RxFirebaseDatabase.setValue(commentsRef.child(comment.photoId).child(comment.commentId), comment)
    }

    fun deleteCommentsForDeletedPhoto(photoId: String): Task<Void> {
        return commentsRef.child(photoId).removeValue()
    }

    // endregion
}
