package com.babyMonitor.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.babyMonitor.BuildConfig
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    const val FORMAT_DATE_AND_TIME = "yyyyMMddHHmmss"
    const val FORMAT_DATE = "yyyyMMdd"
    const val FORMAT_DATE_TYPE_HEADER = "EEEE - dd MMM yyyy"
    const val FORMAT_HOURS_MINUTES = "HH:mm"

    /**
     * Format a double value into a text value with only one decimal place
     */
    fun getDoubleToStringWithOneDecimal(value: Double): String {
        val df = DecimalFormat("0.0")
        df.roundingMode = RoundingMode.HALF_EVEN
        return df.format(value)
    }

    /**
     * Convert a date and time in a given format to milliseconds
     */
    fun convertDateToMillis(givenDateString: String, format: String): Long {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        var timeInMilliseconds: Long = 1
        try {
            // Convert the UTC given time in the given format to millis
            var parsedDate: Date? = sdf.parse(givenDateString)
            parsedDate ?: kotlin.run { throw KotlinNullPointerException() }
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

        return timeInMilliseconds
    }

    /**
     * Convert a date and time in milliseconds to a given text format
     */
    fun convertMillisToDateFormat(timeInMillis: Long, dateFormat: String): String {
        // Set a new calendar with the default timezone and the given time
        val tz = TimeZone.getDefault()
        val calendar = Calendar.getInstance(tz)
        calendar.timeInMillis = timeInMillis

        // Set the output format
        val formatter = SimpleDateFormat(dateFormat, Locale.getDefault())
        formatter.timeZone = tz

        return formatter.format(calendar.time)
    }

    /**
     * Get the current device date and time in milliseconds
     */
    fun getCurrentDateTimeInMillis(): Long {
        return Calendar.getInstance().timeInMillis
    }

    /**
     * Get a unique identifiers associated to the device
     */
    @SuppressLint("HardwareIds")
    fun getAndroidDeviceId(context: Context): String {
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        )
    }

    /**
     * Get the URI of a resource placed in the raw folder based on the given id which corresponds to
     * its name
     */
    fun getRawUri(rawResourceId: Int): Uri? {
        return Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + BuildConfig.APPLICATION_ID + "/" + rawResourceId
        )
    }

    /**
     * Get a bitmap from a drawable resource id
     */
    fun getBitmapFromDrawable(context: Context, drawableId: Int): Bitmap {
        return BitmapFactory.decodeResource(context.resources, drawableId)
    }

}