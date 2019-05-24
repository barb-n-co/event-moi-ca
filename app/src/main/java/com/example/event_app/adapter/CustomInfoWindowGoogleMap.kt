package com.example.event_app.adapter

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import com.example.event_app.R
import com.example.event_app.model.EventItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.window_custom_info_map.view.*

class CustomInfoWindowGoogleMap(val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoContents(p0: Marker?): View {
        val mInfoView = (context as Activity).layoutInflater.inflate(R.layout.window_custom_info_map, null)
        val mInfoWindow: EventItem? = p0?.tag as EventItem?
        mInfoView.tv_title_info_window_map.text = mInfoWindow?.nameEvent
        mInfoView.tv_date_info_window_map.text = mInfoWindow?.dateStart
        mInfoWindow?.let {
            mInfoView?.chip_user_state_info_window_map?.visibility = View.VISIBLE
            if (it.organizer == 1 || it.accepted == 1) {
                if (it.organizer == 1) {
                    mInfoView?.chip_user_state_info_window_map?.chipBackgroundColor =
                        ColorStateList.valueOf(context.resources.getColor(R.color.dark_green))
                    mInfoView?.chip_user_state_info_window_map?.text = context.getString(R.string.tv_state_organizer)
                } else {
                    mInfoView?.chip_user_state_info_window_map?.chipBackgroundColor =
                        ColorStateList.valueOf(context.resources.getColor(R.color.green))
                    mInfoView?.chip_user_state_info_window_map?.text = context.getString(R.string.tv_state_participate)
                }
            } else {
                mInfoView?.chip_user_state_info_window_map?.chipBackgroundColor =
                    ColorStateList.valueOf(context.resources.getColor(R.color.colorPrimary))
                mInfoView?.chip_user_state_info_window_map?.text = context.getString(R.string.tv_state_invited)
            }
        }
        return mInfoView
    }

    override fun getInfoWindow(p0: Marker?): View? {
        return null
    }
}
