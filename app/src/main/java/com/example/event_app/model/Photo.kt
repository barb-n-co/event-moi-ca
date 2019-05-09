package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Photo(
    var id: String? = null,
    var auteurId: String? = null,
    var authorName: String? = null,
    var like: Int? = null,
    var url: String? = null,
    var commentaires: MutableList<Commentaire> = ArrayList(),
    var isReported: Int = 0
) : Parcelable