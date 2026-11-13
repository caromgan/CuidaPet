package com.carolinagandin.cuidadog.ui

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carolinagandin.cuidadog.data.local.AlimentacionEvolucionEntity
import com.carolinagandin.cuidadog.data.local.AlimentacionHistorialEntity
import com.carolinagandin.cuidadog.data.local.AlimentacionPruebaEntity
import com.carolinagandin.cuidadog.data.local.CuidaDogDatabase
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import com.carolinagandin.cuidadog.data.local.PerroEntity
import com.carolinagandin.cuidadog.util.AlarmScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@SuppressLint("NewApi")
@OptIn(ExperimentalCoroutinesApi::class)
class CuidaDogViewModel(application: Application) : AndroidViewModel(application) {
    private val database = CuidaDogDatabase.getDatabase(application)
    private val perroDao = database.perroDao()
    private val cuidadoDao = database.cuidadoDao()
    private val alimentacionPruebaDao = database.alimentacionPruebaDao()
    private val alimentacionEvolucionDao = database.alimentacionEvolucionDao()
    private val alimentacionHistorialDao = database.alimentacionHistorialDao()

    val mascotas: StateFlow<List<PerroEntity>> = perroDao.getTodasLasMascotas()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _mascotaActivaId = MutableStateFlow<Long?>(null)

    val mascotaActiva: StateFlow<PerroEntity?> = combine(mascotas, _mascotaActivaId) { lista, idActivo ->
        if (lista.isEmpty()) {
            null
        } else {
            lista.find { it.id == idActivo } ?: lista.first()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val pruebasAlimentacion: StateFlow<List<AlimentacionPruebaEntity>> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(emptyList())
        else alimentacionPruebaDao.getPruebasPorMascota(mascota.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val ultimaPruebaAlimentacion: StateFlow<AlimentacionPruebaEntity?> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(null)
        else alimentacionPruebaDao.getUltimaPrueba(mascota.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val historialAlimentacion: StateFlow<List<AlimentacionHistorialEntity>> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(emptyList())
        else alimentacionHistorialDao.getHistorialPorMascota(mascota.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val hoyStr: String = LocalDate.now().toString()
    private val mananaStr: String = LocalDate.now().plusDays(1).toString()
    private val hoyMasDiezStr: String = LocalDate.now().plusDays(10).toString()

    val cuidadosHoy: StateFlow<List<CuidadoEntity>> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(emptyList())
        else cuidadoDao.getCuidadosPorFecha(mascota.id, hoyStr)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val proximosDiezDias: StateFlow<List<CuidadoEntity>> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(emptyList())
        else cuidadoDao.getCuidadosProximos10Dias(mascota.id, mananaStr, hoyMasDiezStr)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val todosLosCuidados: StateFlow<List<CuidadoEntity>> = mascotaActiva.flatMapLatest { mascota ->
        if (mascota == null) flowOf(emptyList())
        else cuidadoDao.getTodosLosCuidados(mascota.id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun seleccionarMascota(id: Long) {
        _mascotaActivaId.value = id
    }

    fun guardarMascota(
        nombre: String,
        especie: String,
        raza: String,
        sexo: String,
        fechaNacimientoIso: String?,
        edadAproximadaAnios: Int?,
        talla: String,
        pesoKg: Double,
        color: String?,
        castrado: Boolean?,
        numeroMicrochip: String?,
        alergias: String?,
        medicacionHabitual: String?,
        veterinario: String?,
        observaciones: String?,
        fotoUri: String?,
        tipoDieta: String?,
        marcaAlimento: String?,
        cantidadPorRacion: String?,
        racionesPorDia: Int?,
        horariosAlimentacion: String?,
        restriccionesAlimentarias: String?,
        urgencias24h: String?,
        contactoPrincipal: String?,
        contactoSecundario: String?,
        contactoAlternativo: String?,
        vacunasInfo: String?,
        desparasitacionInfo: String?,
        enfermedadesCronicas: String?,
        historialCelos: String? = null,
        onGuardadoExitoso: (Long) -> Unit
    ) {
        if (nombre.isNotBlank()) {
            viewModelScope.launch {
                val nuevaMascota = PerroEntity(
                    nombre = nombre.trim(),
                    especie = especie,
                    raza = raza,
                    sexo = sexo,
                    fechaNacimientoIso = fechaNacimientoIso,
                    edadAproximadaAnios = edadAproximadaAnios,
                    talla = talla,
                    pesoKg = pesoKg,
                    color = color,
                    castrado = castrado,
                    numeroMicrochip = numeroMicrochip,
                    alergias = alergias,
                    medicacionHabitual = medicacionHabitual,
                    veterinario = veterinario,
                    observaciones = observaciones,
                    fotoUri = fotoUri,
                    tipoDieta = tipoDieta,
                    marcaAlimento = marcaAlimento,
                    cantidadPorRacion = cantidadPorRacion,
                    racionesPorDia = racionesPorDia,
                    horariosAlimentacion = horariosAlimentacion,
                    restriccionesAlimentarias = restriccionesAlimentarias,
                    urgencias24h = urgencias24h,
                    contactoPrincipal = contactoPrincipal,
                    contactoSecundario = contactoSecundario,
                    contactoAlternativo = contactoAlternativo,
                    vacunasInfo = vacunasInfo,
                    desparasitacionInfo = desparasitacionInfo,
                    enfermedadesCronicas = enfermedadesCronicas,
                    historialCelos = historialCelos
                )
                val id = perroDao.insertMascota(nuevaMascota)
                _mascotaActivaId.value = id
                onGuardadoExitoso(id)
            }
        }
    }

    fun actualizarMascota(perro: PerroEntity, onGuardadoExitoso: () -> Unit) {
        viewModelScope.launch {
            perroDao.updateMascota(perro)
            onGuardadoExitoso()
        }
    }

    fun actualizarHistorialCelos(mascota: PerroEntity, nuevoHistorial: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val actualizado = mascota.copy(historialCelos = nuevoHistorial)
            perroDao.updateMascota(actualizado)
            onComplete()
        }
    }

    // Compatibilidad con onboarding anterior
    fun guardarPerro(nombre: String, fotoUri: String? = null) {
        guardarMascota(
            nombre = nombre,
            especie = "Perro",
            raza = "",
            sexo = "No especificado",
            fechaNacimientoIso = null,
            edadAproximadaAnios = null,
            talla = "Mediana",
            pesoKg = 0.0,
            color = null,
            castrado = null,
            numeroMicrochip = null,
            alergias = null,
            medicacionHabitual = null,
            veterinario = null,
            observaciones = null,
            fotoUri = fotoUri,
            tipoDieta = null,
            marcaAlimento = null,
            cantidadPorRacion = null,
            racionesPorDia = null,
            horariosAlimentacion = null,
            restriccionesAlimentarias = null,
            urgencias24h = null,
            contactoPrincipal = null,
            contactoSecundario = null,
            contactoAlternativo = null,
            vacunasInfo = null,
            desparasitacionInfo = null,
            enfermedadesCronicas = null,
            onGuardadoExitoso = {}
        )
    }

    fun actualizarFotoMascota(mascotaId: Long, fotoUri: String?) {
        viewModelScope.launch {
            perroDao.updateFotoUri(mascotaId, fotoUri)
        }
    }

    fun actualizarFotoPerro(fotoUri: String?) {
        val activa = mascotaActiva.value
        if (activa != null) {
            actualizarFotoMascota(activa.id, fotoUri)
        }
    }

    fun guardarCuidado(cuidado: CuidadoEntity, onGuardadoExitoso: () -> Unit) {
        val activa = mascotaActiva.value
        if (activa != null) {
            viewModelScope.launch {
                val cuidadoConMascota = cuidado.copy(mascotaId = activa.id)
                val id = cuidadoDao.insertCuidado(cuidadoConMascota)
                val insertado = cuidadoConMascota.copy(id = id)
                AlarmScheduler.scheduleCareAlarm(getApplication(), insertado, activa.nombre)

                if (cuidado.tieneRepeticion) {
                    try {
                        val startDate = LocalDate.parse(cuidado.fecha)
                        val horizon = startDate.plusDays(90)
                        val cantidad = cuidado.repeticionCantidad ?: 1
                        val unidad = cuidado.repeticionUnidad?.lowercase() ?: "días"
                        val diasSemanaList = cuidado.diasSemana?.split(",")?.map { it.trim().lowercase() } ?: emptyList()

                        val generatedDates = mutableSetOf<LocalDate>()

                        when (unidad) {
                            "días", "dias" -> {
                                if (diasSemanaList.isNotEmpty()) {
                                    val dayMap = mapOf(
                                        "lunes" to java.time.DayOfWeek.MONDAY,
                                        "martes" to java.time.DayOfWeek.TUESDAY,
                                        "miércoles" to java.time.DayOfWeek.WEDNESDAY,
                                        "miercoles" to java.time.DayOfWeek.WEDNESDAY,
                                        "jueves" to java.time.DayOfWeek.THURSDAY,
                                        "viernes" to java.time.DayOfWeek.FRIDAY,
                                        "sábado" to java.time.DayOfWeek.SATURDAY,
                                        "sabado" to java.time.DayOfWeek.SATURDAY,
                                        "domingo" to java.time.DayOfWeek.SUNDAY
                                    )
                                    val targetDays = diasSemanaList.mapNotNull { dayMap[it] }
                                    if (targetDays.isNotEmpty()) {
                                        var curr = startDate.plusDays(1)
                                        while (!curr.isAfter(horizon)) {
                                            if (curr.dayOfWeek in targetDays) {
                                                generatedDates.add(curr)
                                            }
                                            curr = curr.plusDays(1)
                                        }
                                    }
                                } else {
                                    var curr = startDate.plusDays(cantidad.toLong())
                                    while (!curr.isAfter(horizon)) {
                                        generatedDates.add(curr)
                                        curr = curr.plusDays(cantidad.toLong())
                                    }
                                }
                            }
                            "semanas" -> {
                                var curr = startDate.plusWeeks(cantidad.toLong())
                                while (!curr.isAfter(horizon)) {
                                    generatedDates.add(curr)
                                    curr = curr.plusWeeks(cantidad.toLong())
                                }
                            }
                            "meses" -> {
                                var curr = startDate.plusMonths(cantidad.toLong())
                                while (!curr.isAfter(horizon)) {
                                    generatedDates.add(curr)
                                    curr = curr.plusMonths(cantidad.toLong())
                                }
                            }
                            "años", "anios", "año" -> {
                                var curr = startDate.plusYears(cantidad.toLong())
                                while (!curr.isAfter(horizon)) {
                                    generatedDates.add(curr)
                                    curr = curr.plusYears(cantidad.toLong())
                                }
                            }
                        }

                        for (date in generatedDates) {
                            val recurrentCuidado = cuidadoConMascota.copy(
                                id = 0,
                                fecha = date.toString(),
                                realizado = false
                            )
                            val recId = cuidadoDao.insertCuidado(recurrentCuidado)
                            AlarmScheduler.scheduleCareAlarm(getApplication(), recurrentCuidado.copy(id = recId), activa.nombre)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                onGuardadoExitoso()
            }
        }
    }

    fun toggleRealizado(cuidado: CuidadoEntity) {
        viewModelScope.launch {
            cuidadoDao.insertCuidado(cuidado.copy(realizado = !cuidado.realizado))
        }
    }

    fun eliminarCuidado(cuidado: CuidadoEntity) {
        viewModelScope.launch {
            cuidadoDao.deleteCuidado(cuidado)
        }
    }

    fun actualizarCuidado(cuidadoActualizado: CuidadoEntity, onComplete: () -> Unit) {
        val activa = mascotaActiva.value
        val mascotaNombre = activa?.nombre ?: "Mascota"
        viewModelScope.launch {
            AlarmScheduler.cancelCareAlarm(getApplication(), cuidadoActualizado.id)
            cuidadoDao.updateCuidado(cuidadoActualizado)
            AlarmScheduler.scheduleCareAlarm(getApplication(), cuidadoActualizado, mascotaNombre)
            onComplete()
        }
    }

    fun guardarPrueba(
        tipoDieta: String,
        marcaAlimento: String,
        cantidadPorRacion: String,
        unidad: String,
        racionesPorDia: Int,
        horarios: String?,
        fechaInicio: String,
        fechaFin: String,
        observaciones: String?,
        estado: String = "En curso"
    ) {
        val activa = mascotaActiva.value
        if (activa != null) {
            viewModelScope.launch {
                val prueba = AlimentacionPruebaEntity(
                    mascotaId = activa.id,
                    tipoDieta = tipoDieta,
                    marcaAlimento = marcaAlimento,
                    cantidadPorRacion = cantidadPorRacion,
                    unidad = unidad,
                    racionesPorDia = racionesPorDia,
                    horarios = horarios,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin,
                    observaciones = observaciones,
                    estado = estado
                )
                alimentacionPruebaDao.insertPrueba(prueba)

                if (!horarios.isNullOrBlank()) {
                    val times = horarios.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    try {
                        val start = LocalDate.parse(fechaInicio)
                        val end = LocalDate.parse(fechaFin)
                        var current = start
                        while (!current.isAfter(end)) {
                            for (time in times) {
                                val cuidado = CuidadoEntity(
                                    mascotaId = activa.id,
                                    tipo = "Alimentación",
                                    titulo = "Alimentación: $marcaAlimento",
                                    fecha = current.toString(),
                                    hora = time,
                                    tieneRepeticion = true,
                                    notas = "$tipoDieta - $cantidadPorRacion $unidad",
                                    realizado = false
                                )
                                val cuidadoId = cuidadoDao.insertCuidado(cuidado)
                                AlarmScheduler.scheduleCareAlarm(getApplication(), cuidado.copy(id = cuidadoId), activa.nombre)
                            }
                            current = current.plusDays(1)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun marcarTodosRealizados(cuidados: List<CuidadoEntity>, realizado: Boolean = true) {
        viewModelScope.launch {
            for (c in cuidados) {
                if (c.realizado != realizado) {
                    cuidadoDao.insertCuidado(c.copy(realizado = realizado))
                }
            }
        }
    }

    fun actualizarEstadoPrueba(prueba: AlimentacionPruebaEntity, nuevoEstado: String, continuarCuidados: Boolean = false) {
        viewModelScope.launch {
            val actualizada = prueba.copy(estado = nuevoEstado)
            alimentacionPruebaDao.updatePrueba(actualizada)
            if (nuevoEstado != "Aprobada" || !continuarCuidados) {
                val hoyStr = LocalDate.now().toString()
                cuidadoDao.eliminarCuidadosPendientesFuturos(
                    mascotaId = prueba.mascotaId,
                    titulo = "Alimentación: ${prueba.marcaAlimento}",
                    fechaHoy = hoyStr
                )
            }
        }
    }

    fun agregarEvolucion(pruebaId: Long, fecha: String, valoracion: String, observacion: String?) {
        val activa = mascotaActiva.value
        if (activa != null) {
            viewModelScope.launch {
                val evolucion = AlimentacionEvolucionEntity(
                    pruebaId = pruebaId,
                    mascotaId = activa.id,
                    fecha = fecha,
                    valoracion = valoracion,
                    observacion = observacion
                )
                alimentacionEvolucionDao.insertEvolucion(evolucion)
            }
        }
    }

    fun actualizarAlimentacionPerfil(
        tipoDieta: String?,
        marcaAlimento: String?,
        cantidadPorRacion: String?,
        racionesPorDia: Int?,
        horariosAlimentacion: String?,
        restriccionesAlimentarias: String?,
        onActualizado: () -> Unit = {}
    ) {
        val activa = mascotaActiva.value
        if (activa != null) {
            viewModelScope.launch {
                val perroActualizado = activa.copy(
                    tipoDieta = tipoDieta,
                    marcaAlimento = marcaAlimento,
                    cantidadPorRacion = cantidadPorRacion,
                    racionesPorDia = racionesPorDia,
                    horariosAlimentacion = horariosAlimentacion,
                    restriccionesAlimentarias = restriccionesAlimentarias
                )
                perroDao.updateMascota(perroActualizado)

                val historial = AlimentacionHistorialEntity(
                    mascotaId = activa.id,
                    fecha = LocalDate.now().toString(),
                    dietaTipo = tipoDieta ?: "No especificada",
                    marcaAlimento = marcaAlimento ?: "",
                    cantidadPorRacion = cantidadPorRacion ?: "",
                    racionesPorDia = racionesPorDia ?: 0,
                    horarios = horariosAlimentacion,
                    restricciones = restriccionesAlimentarias,
                    observaciones = "Actualización de perfil desde prueba de alimentación aprobada"
                )
                alimentacionHistorialDao.insertHistorial(historial)

                onActualizado()
            }
        }
    }

    fun getEvolucionesPorPrueba(pruebaId: Long): Flow<List<AlimentacionEvolucionEntity>> {
        return alimentacionEvolucionDao.getEvolucionesPorPrueba(pruebaId)
    }
}
