package com.example.event_app.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class HomeViewPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager) {

    private val listFragment = arrayListOf<Fragment>()
    private val listTitle = arrayListOf<String>()

    override fun getItem(position: Int): Fragment = listFragment[position]
    override fun getCount(): Int = listFragment.size
    override fun getPageTitle(position: Int): CharSequence = listTitle[position].toUpperCase()

    fun addFragment(fragment: Fragment, title: String) {
        listFragment.add(fragment)
        listTitle.add(title)
    }
}