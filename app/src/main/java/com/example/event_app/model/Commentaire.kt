package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Commentaire(
    var commentId: String = "",
    var author: String = "",
    var authorId: String = "",
    var comment: String = "",
    var photoId: String = "",
    var date: String = ""
) : Parcelable