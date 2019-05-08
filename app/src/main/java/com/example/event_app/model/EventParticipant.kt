package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EventParticipant(
    var idUser: String? = null,
    var name: String? = null,
    var accepted: Int = 0,
    var organizer: Int = 0
) : Parcelable