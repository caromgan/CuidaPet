package com.carolinagandin.cuidadog.util

import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
object DateUtils {
    private val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE // YYYY-MM-DD
    private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun parseAnyDate(dateStr: String?): LocalDate? {
        if (dateStr.isNullOrBlank()) return null
        val cleaned = dateStr.trim()
        try {
            return LocalDate.parse(cleaned, isoFormatter)
        } catch (_: Exception) {}

        try {
            return LocalDate.parse(cleaned, displayFormatter)
        } catch (_: Exception) {}

        try {
            return LocalDate.parse(cleaned, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } catch (_: Exception) {}

        return null
    }

    fun toDdMmYyyy(isoDateStr: String?): String {
        val date = parseAnyDate(isoDateStr) ?: return isoDateStr?.trim() ?: ""
        return date.format(displayFormatter)
    }

    fun toIsoDate(displayDateStr: String?): String {
        val date = parseAnyDate(displayDateStr) ?: return LocalDate.now().toString()
        return date.format(isoFormatter)
    }

    fun obtenerFechaNacimientoTexto(isoDateStr: String?): String {
        val fechaNac = parseAnyDate(isoDateStr) ?: return "Sin información"
        return fechaNac.format(displayFormatter)
    }

    fun obtenerEdadTexto(isoDateStr: String?, edadAproximada: Int?): String {
        if (edadAproximada != null && edadAproximada >= 0) {
            return "$edadAproximada años"
        }
        val fechaNac = parseAnyDate(isoDateStr) ?: return "Sin información"
        return try {
            val hoy = LocalDate.now()
            val anios = java.time.Period.between(fechaNac, hoy).years
            if (anios >= 0) "$anios años" else "Sin información"
        } catch (_: Exception) {
            "Sin información"
        }
    }

    fun formatearNacimientoConEdad(isoDateStr: String?): String {
        val fechaNac = parseAnyDate(isoDateStr) ?: return isoDateStr?.trim() ?: ""
        return try {
            val hoy = LocalDate.now()
            val anios = java.time.Period.between(fechaNac, hoy).years
            val fechaFmt = fechaNac.format(displayFormatter)
            if (anios >= 0) {
                "$fechaFmt, $anios años"
            } else {
                fechaFmt
            }
        } catch (_: Exception) {
            toDdMmYyyy(isoDateStr)
        }
    }

    fun esFechaHoraValida(fechaStr: String, horaStr: String?): Boolean {
        return try {
            val fecha = parseAnyDate(fechaStr) ?: return true
            val hoy = LocalDate.now()
            if (fecha.isBefore(hoy)) return false

            if (fecha.isEqual(hoy) && !horaStr.isNullOrBlank()) {
                val cleanTime = horaStr.trim().uppercase()
                val timePart = cleanTime.substringBefore(" ").trim()
                val parts = timePart.split(":")
                val hour = parts.getOrNull(0)?.toIntOrNull() ?: return true
                val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0

                var adjustedHour = hour
                if (cleanTime.contains("PM") && hour < 12) adjustedHour += 12
                if (cleanTime.contains("AM") && hour == 12) adjustedHour = 0

                val horaElegida = LocalTime.of(adjustedHour, minute)
                val ahora = LocalTime.now()
                if (horaElegida.isBefore(ahora)) return false
            }
            true
        } catch (_: Exception) {
            true
        }
    }
}
