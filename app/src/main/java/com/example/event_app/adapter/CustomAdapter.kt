package com.example.event_app.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.event_app.R
import com.example.event_app.model.Photo
import com.squareup.picasso.Picasso


class CustomAdapter (private val imageIdList: ArrayList<Photo>) : RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

    override fun onBindViewHolder(holder: CustomAdapter.ViewHolder, position: Int) {
    //holder.iv?.setImageResource(imageIdList[position])
        val itemPhoto = imageIdList[position]
        holder.bindPhoto(itemPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomAdapter.ViewHolder {
        val inflateView = LayoutInflater.from(parent.context).inflate(R.layout.list_pic_event, parent, false)
        return ViewHolder(inflateView)
    }

    override fun getItemCount(): Int {
        return imageIdList.size
    }

    class ViewHolder (private var v:View) : RecyclerView.ViewHolder(v), View.OnClickListener{
        internal var iv: ImageView? = v.findViewById(R.id.image_item)
        private var photo: Photo? = null

        init {
            v.setOnClickListener(this)
        }
        companion object {
            //5
            private val PHOTO_KEY = "PHOTO"
        }
        override fun onClick(v: View) {
            Log.d("DetailPhoto", "photo "+photo?.url)
/*            val context = itemView.context
            val showPhotoIntent = Intent(context, DetailPhotoFragment::class.java)
            showPhotoIntent.putExtra(PHOTO_KEY, photo)
            context.startActivity(showPhotoIntent)*/
        }
        fun bindPhoto(photo: Photo) {
            this.photo = photo
            Picasso.get().load(photo.url).into(iv)
        }
    }

}