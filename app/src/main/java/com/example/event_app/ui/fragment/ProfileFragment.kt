package com.example.event_app.ui.fragment

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.bumptech.glide.GenericTransitionOptions
import com.bumptech.glide.signature.MediaStoreSignature
import com.example.event_app.R
import com.example.event_app.manager.PermissionManager
import com.example.event_app.manager.PermissionManager.Companion.PERMISSION_CAMERA
import com.example.event_app.model.NumberEvent
import com.example.event_app.model.UserProfile
import com.example.event_app.ui.activity.LoginActivity
import com.example.event_app.utils.GlideApp
import com.example.event_app.viewmodel.ProfileViewModel
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.fragment_profile.*
import org.kodein.di.generic.instance
import timber.log.Timber
import java.io.File
import java.io.IOException

class ProfileFragment : BaseFragment() {

    private val viewModel: ProfileViewModel by instance(arg = this)
    private var userId: String? = null

    companion object {
        fun newInstance(): ProfileFragment = ProfileFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        setVisibilityNavBar(true)

        iv_photo_fragment_profile.setOnClickListener {
            openPopUp()
        }

        viewModel.user.subscribe(
            {
                initUser(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)

        viewModel.eventCount.subscribe(
            {
                initNumberEvent(it)
            },
            {
                Timber.e(it)
            }
        ).addTo(viewDisposable)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.disconnect_account -> {
                actionDisconnection()
                true
            }
            R.id.delete_account -> {
                actionDeleteAccount()
                true
            }
            else -> false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, returnIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, returnIntent)

        when (requestCode) {

            PermissionManager.IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    returnIntent?.extras
                    val galleryUri = returnIntent?.data!!
                    val galeryBitmap = viewModel.getBitmapWithResolver(context!!.contentResolver, galleryUri)
                    userId?.let { userId ->
                        viewModel.putImageWithBitmap(galeryBitmap, userId, true)
                    }
                }
            }

            PermissionManager.CAPTURE_PHOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    val capturedBitmap = viewModel.getBitmapWithPath()
                    userId?.let { userId ->
                        viewModel.putImageWithBitmap(capturedBitmap, userId, false)
                    }
                }
            }

        }
    }

    private fun openPopUp() {
        val popup = ProfilePhotoSourceDialogFragment(
            choiceSelectedListener = {
                if (permissionManager.checkPermissions(arrayOf(Manifest.permission.CAMERA))) {
                    if (it) {
                        takePhotoByCamera()
                    }
                } else {
                    requestPermissions()
                }

                if(!it) {
                takePhotoByGallery()
            }
            }
        )
        popup.show(requireFragmentManager(), "profilePhotoSource")
    }

    private fun takePhotoByCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Create the File where the photo should go
            val photoFile: File? = try {
                viewModel.createImageFile(context!!)
            } catch (ex: IOException) {
                // Error occurred while creating the File
                null
            }
            // Continue only if the File was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    context!!,
                    getString(R.string.fileProvider),
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, PermissionManager.CAPTURE_PHOTO)
            }
        }
    }

    private fun takePhotoByGallery() {
        viewModel.pickImageFromGallery().also { galleryIntent ->
            val chooser =
                Intent.createChooser(galleryIntent, "My Gallery")
            startActivityForResult(chooser, PermissionManager.IMAGE_PICK_CODE)
        }
    }


    private fun actionDisconnection() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(R.string.tv_title_dialog_logout)
            .setMessage(R.string.tv_message_dialog_logout)
            .setNegativeButton(R.string.b_cancel_dialog) { _ , _ -> }
            .setPositiveButton(R.string.b_validate_dialog) { _ , _ ->
                viewModel.logout()
                LoginActivity.start(activity!!)
                activity!!.finish()
            }.show()
    }

    private fun actionDeleteAccount() {
        val dialog = AlertDialog.Builder(activity!!)
        dialog.setTitle(R.string.tv_title_dialog_delete_account)
            .setMessage(R.string.tv_message_dialog_delete_account)
            .setNegativeButton(R.string.b_cancel_dialog) { _ , _ -> }
            .setPositiveButton(R.string.b_validate_dialog) { _ , _ ->
                viewModel.deleteAccount()
                LoginActivity.start(activity!!)
                activity!!.finish()
            }.show()
    }

    private fun initUser(user: UserProfile) {
        userId = user.id

        tv_name_fragment_profile.text = user.name
        tv_email_fragment_profile.text = user.email

        GlideApp
            .with(context!!)
            .load(user.photoReference)
            .transition(GenericTransitionOptions.with(R.anim.fade_in))
            .signature(MediaStoreSignature("", user.photoReference?.metadata.hashCode().toLong(), 0))
            .circleCrop()
            .placeholder(R.drawable.ic_profile)
            .into(iv_photo_fragment_profile)
    }

    private fun initNumberEvent(numberEvent: NumberEvent) {
        tv_event_invitation_fragment_profile.text =
            resources.getString(R.string.tv_number_invitation_profile_fragment, numberEvent.invitation)
        tv_event_participate_fragment_profile.text =
            resources.getString(R.string.tv_number_participate_profile_fragment, numberEvent.participate)
        tv_event_organizer_fragment_profile.text =
            resources.getString(R.string.tv_number_organizer_profile_fragment, numberEvent.organizer)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA
        )
        requestPermissions(permissions, PERMISSION_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CAMERA && !grantResults.contains(-1)) {
            takePhotoByCamera()
        }
    }

    override fun onStart() {
        super.onStart()
        setTitleToolbar(getString(R.string.title_profile))
    }
}
