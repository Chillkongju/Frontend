package com.example.baba.data.record

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object WatchedDateManager {
    private var sharedPreferences: SharedPreferences? = null
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("watched_dates", Context.MODE_PRIVATE)
        }
    }

    fun setWatchedDate(diaryId: Long, date: LocalDate) {
        sharedPreferences?.edit()
            ?.putString("diary_$diaryId", date.format(dateFormatter))
            ?.apply()
    }

    fun getWatchedDate(diaryId: Long): LocalDate? {
        val dateString = sharedPreferences?.getString("diary_$diaryId", null)
        return dateString?.let { LocalDate.parse(it, dateFormatter) }
    }

    fun removeWatchedDate(diaryId: Long) {
        sharedPreferences?.edit()
            ?.remove("diary_$diaryId")
            ?.apply()
    }

    fun getAllWatchedDates(): Map<Long, LocalDate> {
        val result = mutableMapOf<Long, LocalDate>()
        sharedPreferences?.all?.forEach { (key, value) ->
            if (key.startsWith("diary_") && value is String) {
                val diaryId = key.removePrefix("diary_").toLongOrNull()
                val date = try {
                    LocalDate.parse(value, dateFormatter)
                } catch (e: Exception) {
                    null
                }
                if (diaryId != null && date != null) {
                    result[diaryId] = date
                }
            }
        }
        return result
    }
}