package com.carolinagandin.cuidadog.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cuidados",
    indices = [Index(value = ["mascotaId"])]
)
data class CuidadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mascotaId: Long = 1,
    val tipo: String,
    val titulo: String,
    val fecha: String,
    val hora: String? = null,
    val tieneRepeticion: Boolean = false,
    val repeticionCantidad: Int? = null,
    val repeticionUnidad: String? = null,
    val diasSemana: String? = null,
    val notas: String? = null,
    val valorPeso: Double? = null,
    val realizado: Boolean = false
)
