package com.example.event_app.adapter

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.EventItem
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_event.view.*

class ListInvitationAdapter(val context : Context) : ListAdapter<EventItem, ListInvitationAdapter.EventViewHolder>(DiffCardCallback()) {

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
            itemView.tv_name_myevents_item.text = event.nameEvent
            itemView.tv_organizer_myevents_item.text = event.nameOrganizer
            itemView.tv_startDate_myevents_item.text = event.dateStart
            if(event.organizer == 1 || event.accepted == 1){
                itemView.b_accept_myevents_item.visibility = View.GONE
                itemView.b_refuse_myevents_item.visibility = View.GONE
                if(event.organizer == 1){
                    itemView.chip_user_state_myevents_item.chipBackgroundColor = ColorStateList.valueOf(context.resources.getColor(R.color.orange))
                    itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_organizer)
                } else {
                    itemView.chip_user_state_myevents_item.chipBackgroundColor = ColorStateList.valueOf(context.resources.getColor(R.color.green))
                    itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_participate)
                }
            } else {
                itemView.b_accept_myevents_item.visibility = View.VISIBLE
                itemView.b_refuse_myevents_item.visibility = View.VISIBLE
                itemView.chip_user_state_myevents_item.chipBackgroundColor = ColorStateList.valueOf(context.resources.getColor(R.color.colorPrimary))
                itemView.chip_user_state_myevents_item.text = context.getString(R.string.tv_state_invited)
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
        }

        override fun areContentsTheSame(oldItem: EventItem, newItem: EventItem): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }
    }
}