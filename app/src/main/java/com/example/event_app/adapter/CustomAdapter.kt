package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.GenericTransitionOptions
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.example.event_app.repository.EventRepository
import com.example.event_app.utils.GlideApp
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.list_pic_event.view.*
import timber.log.Timber


class CustomAdapter :
    ListAdapter<Photo, CustomAdapter.ViewHolder>(DiffPhotocallback()) {

    val photosClickPublisher: PublishSubject<String> = PublishSubject.create()
    val isOrganizerClickPublisher: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPhoto(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.list_pic_event, parent, false)
        return ViewHolder(inflateView, photosClickPublisher)
    }

    class DiffPhotocallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.url == newItem.url
        }
    }

    inner class ViewHolder(private var v: View, private val photosClickPublisher: PublishSubject<String>) :
        RecyclerView.ViewHolder(v) {
        private var photo: Photo? = null

        fun bindPhoto(photo: Photo) {

            isOrganizerClickPublisher.subscribe(
                {
                    if(photo.isReported == 1){
                        v.report_tag.visibility = VISIBLE
                    }
                },
                {
                    Timber.e(it)
                }
            )

            v.setOnClickListener {
                photo.id.let {
                    photosClickPublisher.onNext(photo.id)
                }
            }
            photo.url.let { path ->
                val storageReference = EventRepository.ref.child(path)
                this.photo = photo
                GlideApp.with(v.context)
                    .load(storageReference)
                    .override(300, 300)
                    .centerCrop()
                    .into(v.image_item)
            }
        }
    }
}
