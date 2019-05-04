package com.example.event_app.repository

import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    private val database = FirebaseDatabase.getInstance()
    val allPictures = database.reference.child("photos")
    private val eventsRef = database.reference.child("events")
    private val myEventsRef = database.reference.child("my-events")


    fun fetchEvents(): Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
    }

    fun addEvent(idOrganizer: String, event: Event){
        RxFirebaseDatabase.setValue(myEventsRef.child(event.idEvent), idOrganizer).subscribe()
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent).child("participants"), event).subscribe()
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
}