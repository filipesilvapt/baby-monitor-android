package com.babyMonitor.ui.sleepMonitor

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babyMonitor.R
import com.babyMonitor.models.SleepStateModel
import com.babyMonitor.utils.Utils

class SleepStateAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var sleepStateRows: MutableList<SleepStateRow> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (RowType.values()[viewType]) {
            RowType.Header -> SleepStateHeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_header_sleep_monitor, parent, false)
            )

            RowType.Item -> SleepStateItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.row_item_sleep_monitor, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return sleepStateRows.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return when (val sleepStateRow = sleepStateRows[position]) {
            is SleepStateRow.Header -> (holder as SleepStateHeaderViewHolder).bind(sleepStateRow)
            is SleepStateRow.Item -> (holder as SleepStateItemViewHolder).bind(sleepStateRow)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return sleepStateRows[position].rowType.ordinal
    }

    fun updateItems(listSleepStates: List<SleepStateModel>?) {
        listSleepStates?.let {
            sleepStateRows.clear()

            var timestampMillis: Long
            var lastDateHeader = ""
            var currentDate: String
            var isFirstInDay: Boolean
            var isLastInDay: Boolean

            // Go through the received sleep states in a reversed order so that the newest will
            // end at the top of the list
            for (sleepState in listSleepStates.asReversed()) {
                timestampMillis =
                    Utils.convertDateToMillis(sleepState.timestamp, Utils.FORMAT_DATE_AND_TIME)

                currentDate = Utils.convertMillisToDateFormat(
                    timestampMillis,
                    Utils.FORMAT_DATE
                )

                // Create a new header whenever the date changes
                if (currentDate != lastDateHeader) {
                    lastDateHeader = currentDate
                    isFirstInDay = true
                    isLastInDay = false

                    // Make sure to mark the last entry of the previous day as the last of that day
                    if (sleepStateRows.isNotEmpty()) {
                        (sleepStateRows.last() as SleepStateRow.Item).isLastInDay = true
                    }

                    // Add the header as a new row
                    sleepStateRows.add(
                        SleepStateRow.Header(
                            Utils.convertMillisToDateFormat(
                                timestampMillis,
                                Utils.FORMAT_DATE_TYPE_HEADER
                            )
                        )
                    )
                } else {
                    isFirstInDay = false
                    isLastInDay = false
                }

                // Add the sleep state item as a new row
                sleepStateRows.add(
                    SleepStateRow.Item(
                        sleepState.state,
                        sleepState.timestamp,
                        isFirstInDay,
                        isLastInDay
                    )
                )
            }

            // Make sure to mark the last entry of the list as the last of the day
            if (sleepStateRows.isNotEmpty()) {
                (sleepStateRows.last() as SleepStateRow.Item).isLastInDay = true
            }

            notifyDataSetChanged()
        }
    }

}