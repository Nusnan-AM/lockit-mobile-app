package com.example.lockit_mobile_app

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater

class LoadingAlert(private val activity: Activity) {
    private var dialog: AlertDialog? = null

    fun startLoading() {
        val builder = AlertDialog.Builder(activity)
        val inflater = LayoutInflater.from(activity)
        builder.setView(inflater.inflate(R.layout.loding_alert, null))
        builder.setCancelable(false)
        dialog = builder.create()
        dialog?.show()
    }

    fun stopLoading() {
        dialog?.dismiss()
    }
}
