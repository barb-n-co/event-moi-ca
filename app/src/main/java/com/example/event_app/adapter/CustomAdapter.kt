package com.example.event_app.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.squareup.picasso.Picasso
import io.reactivex.subjects.PublishSubject


class CustomAdapter :
    ListAdapter<Photo, CustomAdapter.ViewHolder>(DiffPhotocallback()) {

    val photosClickPublisher: PublishSubject<Int> = PublishSubject.create()

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
        //holder.iv?.setImageResource(imageIdList[position])
        holder.bindPhoto(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
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

    inner class ViewHolder(private var v: View, private val photosClickPublisher: PublishSubject<Int>) :
        RecyclerView.ViewHolder(v) {
        internal var iv: ImageView? = v.findViewById(R.id.image_item)
        private var photo: Photo? = null

        fun bindPhoto(photo: Photo) {
            v.setOnClickListener {
                photo.id?.let {
                    photosClickPublisher.onNext(photo.id!!)
                }
            }

            this.photo = photo
            Picasso.get().load(photo.url).into(iv)
        }
    }

}