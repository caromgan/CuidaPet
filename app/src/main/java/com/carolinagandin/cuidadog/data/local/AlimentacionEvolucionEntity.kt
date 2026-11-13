package com.carolinagandin.cuidadog.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "alimentacion_evoluciones",
    indices = [Index(value = ["pruebaId"]), Index(value = ["mascotaId"])]
)
data class AlimentacionEvolucionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pruebaId: Long,
    val mascotaId: Long,
    val fecha: String,
    val valoracion: String, // "Acepta bien", "Regular", "No acepta"
    val observacion: String? = null
)
