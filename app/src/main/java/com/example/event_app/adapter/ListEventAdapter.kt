package com.example.event_app.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Event
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.item_myevent.view.*

class ListEventAdapter : ListAdapter<Event, ListEventAdapter.EventViewHolder>(DiffCardCallback()) {

    val eventsClickPublisher: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_myevent, parent, false)
        return EventViewHolder(view, eventsClickPublisher)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(itemView: View, private val eventsClickPublisher: PublishSubject<String>) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(event: Event) {
            //disposition
            itemView.tv_name_myevents_item.text = event.name
            itemView.tv_description_myevents_item.text = event.description
            itemView.tv_startDate_myevents_item.text = event.dateStart
            itemView.tv_endDate_myevents_item.text = event.dateEnd

            Log.d("MYEVENTSFRAGMENT", "adpater")

            bindPositionClick(event.idEvent)
        }

        private fun bindPositionClick(idEvent: String) {
            itemView.setOnClickListener {
                eventsClickPublisher.onNext(idEvent)
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