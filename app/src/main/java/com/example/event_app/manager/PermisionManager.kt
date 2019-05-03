package com.example.event_app.manager

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

class PermissionManager(val context: Context) {

    companion object {
        const val REQUEST_PERMISSION_CAMERA = 1001
    }

    fun requestCameraPermission(activity: Activity): Boolean {
        return requestPermission(Manifest.permission.CAMERA, REQUEST_PERMISSION_CAMERA, activity)
    }

    private fun requestPermission(permission: String, code: Int, activity: Activity): Boolean {
        return requestPermissions(arrayOf(permission), code, activity)
    }

    private fun requestPermissions(permissions: Array<String>, code: Int, activity: Activity): Boolean {
        return if (!checkPermissions(permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, code)
            false
        } else {
            true
        }
    }


    private fun checkPermissions(permissions: Array<out String>): Boolean {
        var permissionGranted = false
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
            }
        }
        return permissionGranted
    }

}