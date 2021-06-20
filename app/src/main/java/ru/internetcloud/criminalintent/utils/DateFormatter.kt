package ru.internetcloud.criminalintent.utils

import java.text.SimpleDateFormat
import java.util.*

const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss"
const val TIME_FORMAT = "HH : mm"
const val DATE_FORMAT = "dd MMMM yyyy, EE"
// день Месяц словом, год 4 знака, день недели 2 буквы: // https://javarush.ipnodns.ru/lesson/lect40.html


class DateFormatter {

    companion object {
        fun getDateString(date: Date?): String {
            val sdf = SimpleDateFormat(DATE_FORMAT)
            return date?.let { sdf.format(date)} ?: ""
        }

        fun getTimeString(date: Date?): String {
            val sdf = SimpleDateFormat(TIME_FORMAT)
            return date?.let { sdf.format(date)} ?: ""
        }
    }
}