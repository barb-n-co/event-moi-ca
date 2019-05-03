package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Event
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_invitation.view.*

class ListInvitationAdapter : ListAdapter<Event, ListInvitationAdapter.EventViewHolder>(DiffCardCallback()) {

    val acceptClickPublisher: PublishSubject<String> = PublishSubject.create()
    val refuseClickPublisher: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invitation, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event) {
            //disposition
            itemView.tv_name_invitation_item.text = event.name
            itemView.tv_description_invitation_item.text = event.description
            itemView.tv_startDate_invitation_item.text = "Du " + event.dateStart + " au " + event.dateEnd

            itemView.b_accept_item_invitation_item.setOnClickListener {
                acceptClickPublisher.onNext(event.idEvent)
            }

            itemView.b_refuse_invitation_item.setOnClickListener {
                refuseClickPublisher.onNext(event.idEvent)
            }
        }
    }

    class DiffCardCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.idEvent == newItem.idEvent
        }
    }
}