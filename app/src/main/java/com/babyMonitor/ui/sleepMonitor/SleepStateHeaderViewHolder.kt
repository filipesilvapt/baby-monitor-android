package com.babyMonitor.ui.sleepMonitor

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.babyMonitor.R

class SleepStateHeaderViewHolder(private val sleepStateHeaderView: View) :
    RecyclerView.ViewHolder(sleepStateHeaderView) {

    fun bind(header: SleepStateRow.Header) {
        val dateHeaderTextView = sleepStateHeaderView.findViewById<TextView>(R.id.text_date_header)

        dateHeaderTextView.text = header.date
    }

}