package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LikeComment(var userId: String = "", var commentId: String = "", var likeId: String = "") : Parcelable
