package com.example.event_app.repository

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
    private val eventsRef: DatabaseReference = EventRepository.database.getReference("events")
    private val photoRef: DatabaseReference = EventRepository.database.getReference("photos")


    fun fetchEvents(): Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
    }

    fun getEventDetail(eventId: Int): Maybe<Event> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef.child(eventId.toString()), Event::class.java
        )
    }

    fun getPhotoDetail(eventId: Int, photoId: Int): Maybe<Photo> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            photoRef.child(eventId.toString()).child(photoId.toString()), Photo::class.java
        )

    }
}