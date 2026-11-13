package com.carolinagandin.cuidadog.util

import android.annotation.SuppressLint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@SuppressLint("NewApi")
object CeloUtils {
    private val displayFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

    fun parsearHistorial(historialStr: String?): List<LocalDate> {
        if (historialStr.isNullOrBlank()) return emptyList()
        return historialStr.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapNotNull {
                try {
                    LocalDate.parse(it)
                } catch (_: Exception) {
                    null
                }
            }
            .sortedDescending()
    }

    fun serializarHistorial(fechas: List<LocalDate>): String {
        return fechas.sortedDescending().joinToString(",") { it.toString() }
    }

    fun calcularProximoCelo(historialStr: String?): LocalDate? {
        val lista = parsearHistorial(historialStr)
        val ultimo = lista.firstOrNull() ?: return null
        return ultimo.plusDays(180) // 6 months / 180 days
    }

    fun formatearFecha(date: LocalDate?): String {
        return date?.format(displayFormatter) ?: ""
    }
}
