package com.babyMonitor.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.File
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object Utils {

    const val FORMAT_DATE_AND_TIME = "yyyyMMddHHmmss"

    fun getDoubleOneDecimal(value: Double): String {
        val df = DecimalFormat("0.0")
        df.roundingMode = RoundingMode.HALF_EVEN
        return df.format(value)
    }

    fun getDateInMilliSeconds(givenDateString: String, format: String): Long {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        var timeInMilliseconds: Long = 1
        try {
            // Convert the UTC given time in the given format to millis
            var parsedDate: Date = sdf.parse(givenDateString)
            timeInMilliseconds = parsedDate.time

            // Create a calendar to add the timezone offset
            val calendar = Calendar.getInstance()
            val tz = TimeZone.getDefault()
            calendar.timeInMillis = timeInMilliseconds
            calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.timeInMillis))

            // Get the time in millis now with the offset
            parsedDate = calendar.time as Date
            timeInMilliseconds = parsedDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        Log.d("TEST", "date: $givenDateString millis: $timeInMilliseconds")
        return timeInMilliseconds
    }

    @SuppressLint("HardwareIds")
    fun getAndroidDeviceId(context: Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    fun getRawUri(context: Context, filename: String): Uri? {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator.toString() + context.packageName.toString() + "/raw/" + filename
        )
    }

    fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, drawableId)
    }

}