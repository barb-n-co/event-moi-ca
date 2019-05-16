package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Message(var title: String, var message: String) : Parcelable
