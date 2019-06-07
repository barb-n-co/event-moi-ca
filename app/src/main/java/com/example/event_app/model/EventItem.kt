package com.example.event_app.model

import com.google.firebase.storage.StorageReference

class EventItem(
    val idEvent: String,
    val nameEvent: String,
    val idUser: String,
    val nameOrganizer: String,
    var place: String = "",
    val dateStart: String,
    val dateEnd: String,
    val accepted: Int = 0,
    val organizer: Int = 0,
    val description: String,
    val idOrganizer: String,
    val reportedPhotoCount: Int = 0,
    val isEmptyEvent: Int = 0,
    var organizerPhoto: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var activate: Int = 1,
    var organizerPhotoReference: StorageReference? = null
)
