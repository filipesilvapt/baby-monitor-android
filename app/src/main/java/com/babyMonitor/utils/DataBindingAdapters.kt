package com.babyMonitor.utils

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter

@BindingAdapter("bind:imageResourceId")
fun ImageView.setImageResourceId(resource: Int?) {
    resource?.let { this.setImageResource(it) }
}

@BindingAdapter("bind:textResourceId")
fun TextView.setTextResourceId(resource: Int?) {
    resource?.let { this.setText(it) }
}