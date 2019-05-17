package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Photo(
    var id: String = "",
    var auteurId: String = "",
    var authorName: String = "",
    var like: Int = 0,
    var url: String = "",
    var commentaires: MutableList<Commentaire> = ArrayList(),
    var isReported: Int = 0
) : Parcelable