package com.example.event_app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.event_app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.dialog_fragment_profile_photo_source.*

class ProfilePhotoSourceDialogFragment(private val choiceSelectedListener: (Boolean) -> Unit): BottomSheetDialogFragment() {

    private var viewDisposable: CompositeDisposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_profile_photo_source, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cameraTriggers = listOf(b_validate_camera_source_dialog_fragment,
            tv_camera_profile_photo_source_dialog_fragment)
        val galleryTriggers = listOf(b_validate_gallery_source_dialog_fragment,
            tv_gallery_profile_photo_source_dialog_fragment)

        cameraTriggers.forEach {
            it.setOnClickListener {
                choiceSelectedListener(true)
                dismiss()
            }
        }

        galleryTriggers.forEach {
            it.setOnClickListener {
                choiceSelectedListener(false)
                dismiss()
            }
        }


    }

    override fun onDestroyView() {
        viewDisposable.dispose()
        super.onDestroyView()
    }
}