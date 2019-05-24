package com.example.event_app.model

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class Cluster(
    val id: Int,
    val mPosition: LatLng?,
    val mTitle: String?,
    val mSnippet: String?
) : ClusterItem {

    override fun getSnippet(): String? {
        return mSnippet
    }

    override fun getTitle(): String? {
        return mTitle
    }

    override fun getPosition(): LatLng? {
        return mPosition
    }
}
