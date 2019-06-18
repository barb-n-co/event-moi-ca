package com.example.event_app.adapter

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import com.example.event_app.ui.fragment.DetailPhotoFragment
import io.reactivex.subjects.BehaviorSubject

class PicturesViewPagerAdapter(val fragmentManager: FragmentManager, val pictures: List<String>, val idEvent: String, val idOrganizer: String): FragmentStatePagerAdapter(fragmentManager) {

    val idItemPosition: BehaviorSubject<String> = BehaviorSubject.create()

    override fun getItem(position: Int): Fragment {
        return DetailPhotoFragment.newInstance(pictures.get(position), idEvent, idOrganizer)
    }

    override fun getItemPosition(item: Any): Int {
        val position = super.getItemPosition(item)
        idItemPosition.onNext(pictures.get(position))
        return position
    }

    override fun getCount(): Int {
        return pictures.size
    }
}
