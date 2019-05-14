package com.example.event_app.model

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
    val isEmptyEvent: Int = 0
)