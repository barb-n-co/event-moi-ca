package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MyEvents(
    var idEvent: String? = null,
    var accepted: Int = 0,
    var IsOrganizer: Int = 0,
    var IsEmptyEvent: Int = 0
) : Parcelable