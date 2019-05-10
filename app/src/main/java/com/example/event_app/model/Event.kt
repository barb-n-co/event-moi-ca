package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event(
        var idEvent: String = "",
        var idOrganizer: String = "",
        var organizer: String = "",
        var name: String = "",
        var place: String = "",
        var description: String = "",
        var dateStart: String = "",
        var dateEnd: String = "",
        var reportedPhotoCount: Int = 0
) : Parcelable