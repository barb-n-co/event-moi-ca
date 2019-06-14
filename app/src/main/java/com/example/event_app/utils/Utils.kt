package com.example.event_app.utils

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.example.event_app.model.AddressMap
import com.google.android.gms.maps.model.LatLng

fun <T> T?.or(default: T): T = this ?: default
fun <T> T?.or(compute: () -> T): T = this ?: compute()

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun AddressMap.getLatLng(): LatLng? {
    return this.lat?.let { it1 -> this.lng?.let { it2 -> LatLng(it1, it2) } }
}

fun Context.toast(message: String, duration: Int) {
    android.widget.Toast.makeText(this, message, duration).show()
}

fun Context.toast(stringRef: Int, duration: Int) {
    android.widget.Toast.makeText(this, this.getString(stringRef), duration).show()
}