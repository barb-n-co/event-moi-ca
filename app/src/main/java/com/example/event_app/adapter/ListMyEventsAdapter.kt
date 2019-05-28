package com.example.event_app.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.event_app.R
import com.example.event_app.model.EventItem
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.HomeFragmentViewModel
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_event.view.*

class ListMyEventsAdapter(val context: Context, val fragmentViewModel: HomeFragmentViewModel) :
    ListAdapter<EventItem, ListMyEventsAdapter.EventViewHolder>(DiffCardCallback()) {

    val eventClickPublisher: PublishSubject<String> = PublishSubject.create()
    val acceptClickPublisher: PublishSubject<String> = PublishSubject.create()
    val refuseClickPublisher: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: EventItem) {
            //disposition

            if (event.reportedPhotoCount > 0 && event.organizer == 1) {
                itemView.btn_reported_photo.visibility = VISIBLE
            }

            itemView.tv_name_myevents_item.text = event.nameEvent
            itemView.tv_organizer_myevents_item.text = event.nameOrganizer
            itemView.tv_startDate_myevents_item.text = event.dateStart
            if (event.organizer == 1 || event.accepted == 1) {
                itemView.b_accept_myevents_item.visibility = GONE
                itemView.b_refuse_myevents_item.visibility = GONE
                if (event.organizer == 1) {
                    itemView.chip_user_state_myevents_item.chipBackgroundColor =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.dark_green))
                    itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_organizer)
                } else {
                    itemView.chip_user_state_myevents_item.chipBackgroundColor =
                        ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green))
                    itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_participate)
                }
            } else {
                itemView.b_accept_myevents_item.visibility = VISIBLE
                itemView.b_refuse_myevents_item.visibility = VISIBLE
                itemView.chip_user_state_myevents_item.chipBackgroundColor =
                    ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorPrimary))
                itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_invited)
            }
            if (event.organizerPhoto.isNotEmpty()) {
                GlideApp
                    .with(context)
                    .load(fragmentViewModel.getStorageRef(event.organizerPhoto))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .circleCrop()
                    .into(itemView.iv_organizer_photo_item_event)
            }

            itemView.setOnClickListener {
                eventClickPublisher.onNext(event.idEvent)
            }

            itemView.b_accept_myevents_item.setOnClickListener {
                acceptClickPublisher.onNext(event.idEvent)
            }

            itemView.b_refuse_myevents_item.setOnClickListener {
                refuseClickPublisher.onNext(event.idEvent)
            }

        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<EventItem>() {
        override fun areItemsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.idEvent == newItem.idEvent
                    && oldItem.accepted == newItem.accepted
                    && oldItem.reportedPhotoCount == newItem.reportedPhotoCount
        }

        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.idEvent == newItem.idEvent && oldItem.accepted == newItem.accepted
        }
    }
}