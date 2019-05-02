package com.example.event_app.model

data class Event(
        var idEvent: Int,
        var name: String,
        var description: String?,
        var dateStart: String,
        var dateEnd: String
)