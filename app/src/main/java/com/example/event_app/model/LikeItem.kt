package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LikeItem(var userId: String = "", var photoId: String = "") : Parcelable
