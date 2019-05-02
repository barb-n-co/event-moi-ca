package com.example.event_app.repository

import com.example.event_app.model.Event
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import durdinapps.rxfirebase2.DataSnapshotMapper
import durdinapps.rxfirebase2.RxFirebaseDatabase
import io.reactivex.Flowable

object EventRepository {

    val db = FirebaseStorage.getInstance("gs://event-moi-ca.appspot.com")
    val ref = db.reference
    val allImgRef = ref.child("allImages")

    private val database = FirebaseDatabase.getInstance()
    private val eventsRef: DatabaseReference = EventRepository.database.getReference("events")

    fun fetchEvents() : Flowable<List<Event>> {
        return RxFirebaseDatabase.observeSingleValueEvent(
            eventsRef, DataSnapshotMapper.listOf(Event::class.java)
        ).toFlowable()
    }
}