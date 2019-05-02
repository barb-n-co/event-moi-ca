package com.example.event_app.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Event
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_invitation.view.*
import java.util.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date


class ListInvitationAdapter : ListAdapter<Event, ListInvitationAdapter.EventViewHolder>(DiffCardCallback()) {

    private val eventsClickPublisher: PublishSubject<Int> = PublishSubject.create()
    val acceptClickPublisher: PublishSubject<Int> = PublishSubject.create()
    val refuseClickPublisher: PublishSubject<Int> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_invitation, parent, false)
        return EventViewHolder(view, eventsClickPublisher)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View, private val eventsClickPublisher: PublishSubject<Int>) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event) {
            //disposition
            itemView.tv_name_invitation_item.text = event.name
            itemView.tv_description_invitation_item.text = event.description
            itemView.tv_startDate_invitation_item.text = "Du " + stringToDate(event.dateStart) + " au " + stringToDate(event.dateEnd) + ""

            itemView.b_accept_item_invitation_item.setOnClickListener {
                acceptClickPublisher.onNext(event.idEvent)
            }

            itemView.b_refuse_invitation_item.setOnClickListener {
                refuseClickPublisher.onNext(event.idEvent)
            }

            bindPositionClick(event.idEvent)
        }

        private fun bindPositionClick(idEvent: Int) {
            itemView.setOnClickListener {
                eventsClickPublisher.onNext(idEvent)
            }
        }

        private fun stringToDate(oldDate: String?) : String? {
            return oldDate
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