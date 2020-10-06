package com.babyMonitor.ui.sleepMonitor

enum class RowType {
    Header,
    Item
}

sealed class SleepStateRow(val rowType: RowType) {
    data class Header(val date: String) : SleepStateRow(RowType.Header)

    data class Item(
        val state: Int,
        val timestamp: String,
        val isFirstInDay: Boolean,
        var isLastInDay: Boolean
    ) : SleepStateRow(RowType.Item)
}