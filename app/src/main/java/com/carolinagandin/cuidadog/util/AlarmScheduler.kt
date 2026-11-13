package com.carolinagandin.cuidadog.util

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import java.time.LocalDate
import java.time.ZoneId

@SuppressLint("NewApi")
object AlarmScheduler {
    fun scheduleCareAlarm(context: Context, cuidado: CuidadoEntity, mascotaNombre: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CuidadoAlarmReceiver::class.java).apply {
                putExtra("titulo", cuidado.titulo)
                putExtra("tipo", cuidado.tipo)
                putExtra("hora", if (cuidado.hora.isNullOrBlank()) "08:00 AM" else cuidado.hora)
                putExtra("notas", cuidado.notas ?: "")
                putExtra("mascotaNombre", mascotaNombre)
            }

            val requestCode = cuidado.id.toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val dateStr = cuidado.fecha // YYYY-MM-DD
            val timeStr = if (!cuidado.hora.isNullOrBlank()) {
                val cleanTime = cuidado.hora.substringBefore(" ").trim()
                if (cleanTime.length >= 5) cleanTime.substring(0, 5) else "08:00"
            } else {
                "08:00" // Default 08:00 AM if unassigned
            }

            val localDate = LocalDate.parse(dateStr)
            val parts = timeStr.split(":")
            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val targetDateTime = localDate.atTime(hour, minute)
            val triggerMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            if (triggerMillis > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelCareAlarm(context: Context, cuidadoId: Long) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, CuidadoAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                cuidadoId.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
