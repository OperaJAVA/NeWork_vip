package ru.netology.nework.date

import ru.netology.nework.enumeration.MeetingType

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DateEvent(
    val date: String? = null,
    val dateForSending: String? = null,
    val meetingType: MeetingType = MeetingType.ONLINE
)

// Функция, возвращающая текущее время в формате "yyyy-MM-dd HH:mm:ss"
fun getTime(): String {
    val currentTime = System.currentTimeMillis()
    val dateFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return dateFormatter.format(Date(currentTime))
}