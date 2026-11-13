package com.carolinagandin.cuidadog.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alimentacion_historial",
    indices = [Index(value = ["mascotaId"])]
)
data class AlimentacionHistorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mascotaId: Long,
    val fecha: String,
    val dietaTipo: String,
    val marcaAlimento: String,
    val cantidadPorRacion: String,
    val racionesPorDia: Int,
    val horarios: String? = null,
    val restricciones: String? = null,
    val observaciones: String? = null
)
