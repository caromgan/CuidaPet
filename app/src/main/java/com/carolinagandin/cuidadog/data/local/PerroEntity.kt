package com.carolinagandin.cuidadog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "perro")
data class PerroEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val especie: String = "Perro",
    val raza: String = "",
    val sexo: String = "No especificado",
    val fechaNacimientoIso: String? = null,
    val edadAproximadaAnios: Int? = null,
    val talla: String = "Mediana",
    val pesoKg: Double = 0.0,
    val color: String? = null,
    val castrado: Boolean? = null,
    val numeroMicrochip: String? = null,
    val alergias: String? = null,
    val medicacionHabitual: String? = null,
    val veterinario: String? = null,
    val observaciones: String? = null,
    val fotoUri: String? = null,
    val tipoDieta: String? = null,
    val marcaAlimento: String? = null,
    val cantidadPorRacion: String? = null,
    val racionesPorDia: Int? = null,
    val horariosAlimentacion: String? = null,
    val restriccionesAlimentarias: String? = null,
    val urgencias24h: String? = null,
    val contactoPrincipal: String? = null,
    val contactoSecundario: String? = null,
    val contactoAlternativo: String? = null,
    val vacunasInfo: String? = null,
    val desparasitacionInfo: String? = null,
    val enfermedadesCronicas: String? = null,
    val historialCelos: String? = null
)
