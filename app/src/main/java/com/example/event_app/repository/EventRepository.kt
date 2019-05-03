package com.example.event_app.repository

import com.example.event_app.model.Event
import com.example.event_app.model.Photo
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable
import io.reactivex.Maybe

object EventRepository {

    private val database = FirebaseDatabase.getInstance()
    private val eventsRef: DatabaseReference = EventRepository.database.getReference("events")
    private val photoRef: DatabaseReference = EventRepository.database.getReference("photos")


    fun fetchEvents(): Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
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