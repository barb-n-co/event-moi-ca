package com.example.event_app.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class PermissionManager(val context: Context) {

    companion object {
        const val REQUEST_PERMISSION_CAMERA = 1001
        const val PERMISSION_ALL = 1
        const val PERMISSION_IMPORT = 2
        const val IMAGE_PICK_CODE = 1000
        const val CAPTURE_PHOTO = 104
        const val PERMISSION_LOCATION = 4

    }

    fun requestCameraPermission(activity: Activity): Boolean {
        return requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA, activity)
    }

    private fun requestPermission(permission: String, code: Int, activity: Activity): Boolean {
        return requestPermissions(arrayOf(permission), code, activity)
    }

    fun requestPermissions(permissions: Array<String>, code: Int, activity: Activity): Boolean {
        return if (!checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, code)
            false
        } else {
            true
        }
    }


    fun checkPermissions(permissions: Array<out String>): Boolean {
        var permissionGranted = false
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
            }
        }
        return permissionGranted
    }

}