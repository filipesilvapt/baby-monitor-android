package com.babyMonitor.ui.sleepMonitor

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.babyMonitor.R
import com.babyMonitor.utils.SleepState
import com.babyMonitor.utils.Utils

class SleepStateItemViewHolder(private val sleepStateItemView: View) :
    RecyclerView.ViewHolder(sleepStateItemView) {

    fun bind(item: SleepStateRow.Item) {
        // Set the hours
        val hoursTextView = sleepStateItemView.findViewById<TextView>(R.id.text_hours)

        hoursTextView.text = Utils.convertMillisToDateFormat(
            Utils.convertDateToMillis(item.timestamp, Utils.FORMAT_DATE_AND_TIME),
            Utils.FORMAT_HOURS_MINUTES
        )

        // Set the state text and image according to the state flag
        val sleepStateTextView = sleepStateItemView.findViewById<TextView>(R.id.text_sleep_state)
        val sleepStateImageView = sleepStateItemView.findViewById<ImageView>(R.id.image_sleep_state)

        when (item.state) {
            // Baby is agitated
            SleepState.AGITATED.value -> {
                sleepStateTextView.setText(R.string.emotion_state_agitated)
                sleepStateImageView.background = ContextCompat.getDrawable(
                    sleepStateImageView.context,
                    R.drawable.ic_emotion_state_agitated
                )
            }

            // Baby is disturbed
            SleepState.DISTURBED.value -> {
                sleepStateTextView.setText(R.string.emotion_state_disturbed)
                sleepStateImageView.background = ContextCompat.getDrawable(
                    sleepStateImageView.context,
                    R.drawable.ic_emotion_state_disturbed
                )
            }

            // Value 0 or default is baby sleeping
            else -> {
                sleepStateTextView.setText(R.string.emotion_state_sleep)
                sleepStateImageView.background = ContextCompat.getDrawable(
                    sleepStateImageView.context,
                    R.drawable.ic_emotion_state_sleep
                )
            }
        }

        // Show or hide the top divider according to the item being the first of the day or not
        sleepStateItemView.findViewById<View>(R.id.view_top_divider).visibility =
            when (item.isFirstInDay) {
                true -> View.INVISIBLE
                false -> View.VISIBLE
            }

        // Show or hide the bottom divider according to the item being the last of the day or not
        sleepStateItemView.findViewById<View>(R.id.view_bottom_divider).visibility =
            when (item.isLastInDay) {
                true -> View.INVISIBLE
                false -> View.VISIBLE
            }
    }

}