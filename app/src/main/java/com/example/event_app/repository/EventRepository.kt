package com.example.event_app.repository

import android.util.Log
import com.example.event_app.model.Event
import com.google.firebase.database.*
import com.example.event_app.model.Photo
import durdinapps.rxfirebase2.RxFirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import io.reactivex.Flowable
import io.reactivex.Maybe

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    val allImgRef = ref.child("allImages")
    private val database = FirebaseDatabase.getInstance()

    private val eventsRef = database.reference.child("events")
    private val myEventsRef = database.reference.child("my-events")
    private val photoRef = database.reference.child("photos")


    fun fetchEvents(): Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
    }

    fun addEvent(idOrganizer: String, event: Event){
        RxFirebaseDatabase.setValue(myEventsRef.child(event.idEvent), idOrganizer).subscribe()
        RxFirebaseDatabase.setValue(eventsRef.child(event.idEvent), event).subscribe()
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
}