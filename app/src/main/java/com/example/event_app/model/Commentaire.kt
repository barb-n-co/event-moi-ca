package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Commentaire(
    var auteur: String,
    var commentaire: String,
    var date: String
) : Parcelable