package com.carolinagandin.cuidadog.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alimentacion_pruebas",
    indices = [Index(value = ["mascotaId"])]
)
data class AlimentacionPruebaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mascotaId: Long,
    val tipoDieta: String,
    val marcaAlimento: String,
    val cantidadPorRacion: String,
    val unidad: String,
    val racionesPorDia: Int,
    val horarios: String? = null,
    val fechaInicio: String,
    val fechaFin: String,
    val observaciones: String? = null,
    val estado: String // "En curso", "Aprobada", "Descartada", "Finalizada"
)
