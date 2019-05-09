package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null
) : Parcelable