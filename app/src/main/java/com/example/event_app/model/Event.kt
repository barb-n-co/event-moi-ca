package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Event(
        var idEvent: String = "",
        var idOrganizer: String? = null,
        var organizer: String = "",
        var name: String = "",
        var description: String? = null,
        var dateStart: String = "",
        var dateEnd: String = "",
        var reportedPhotoCount: Int = 0
) : Parcelable