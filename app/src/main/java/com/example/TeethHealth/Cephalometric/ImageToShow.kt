package com.example.TeethHealth.Cephalometric

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class ImageToShow (val guid: UUID, val date: String, val status: String) : java.io.Serializable{

    fun GetGuid(): UUID {
        return this.guid
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun GetDate(): LocalDateTime
    {
        return LocalDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun GetDateString(): String
    {
        return this.GetDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    }
    fun GetStatus():String
    {
        if (status == "processing")
            return "На обработке"
        else if (status == "complete")
            return "Обработано"
        return "Не известно"
    }
    fun GetStatusCode():String
    {
        return status
    }
}