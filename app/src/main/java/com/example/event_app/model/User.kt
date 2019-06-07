package com.example.event_app.model

import android.os.Parcelable
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.firebase.storage.StorageReference
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    var id: String? = null,
    var name: String? = null,
    var email: String? = null,
    var photoUrl: String = ""
) : Parcelable
