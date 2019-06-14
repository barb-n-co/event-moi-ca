package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.bumptech.glide.GenericTransitionOptions
import com.example.event_app.R
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.DetailPhotoViewModel
import kotlinx.android.synthetic.main.bottom_sheet_detail_event_map.*
import kotlinx.android.synthetic.main.fragment_photo_fullscreen.*
import org.kodein.di.generic.instance

class PhotoFullscreenFragment: BaseFragment() {

    private val viewModel: DetailPhotoViewModel by instance(arg = this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_photo_fullscreen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageRef = arguments?.let {
            PhotoFullscreenFragmentArgs.fromBundle(it).photoUrl
        }

        imageRef?.let {
            GlideApp
                .with(requireContext())
                .load(viewModel.getStorageRef(it))
                .transition(GenericTransitionOptions.with(R.anim.fade_in))
                .into(iv_photo_fullscreen_fragment)
        }
    }

    override fun onResume() {
        super.onResume()
        setTitleToolbar("")
    }

}
