package com.example.event_app.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ReportedPhotoList(var listOfphotoList: MutableList<PhotoList> = mutableListOf()) : Parcelable