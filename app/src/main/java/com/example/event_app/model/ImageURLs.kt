package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ImageURLs(
    var urls : List<String>? = null
) : Parcelable