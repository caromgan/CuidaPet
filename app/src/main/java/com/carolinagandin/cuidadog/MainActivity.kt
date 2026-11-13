package com.carolinagandin.cuidadog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.carolinagandin.cuidadog.data.local.CuidadoEntity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.carolinagandin.cuidadog.ui.AgregarCuidadoScreen
import com.carolinagandin.cuidadog.ui.AgregarPruebaAlimentacionScreen
import com.carolinagandin.cuidadog.ui.AlimentacionScreen
import com.carolinagandin.cuidadog.ui.CrearPerroScreen
import com.carolinagandin.cuidadog.ui.CuidaDogViewModel
import com.carolinagandin.cuidadog.ui.EditarCuidadoScreen
import com.carolinagandin.cuidadog.ui.EditarMascotaScreen
import com.carolinagandin.cuidadog.ui.HoyScreen
import com.carolinagandin.cuidadog.ui.PerfilMascotaScreen
import com.carolinagandin.cuidadog.ui.TodosCuidadosScreen
import com.carolinagandin.cuidadog.ui.components.AlimentacionIcon
import com.carolinagandin.cuidadog.ui.theme.CuidaDogTheme

enum class Pantalla {
    HOY,
    AGREGAR_CUIDADO,
    TODOS_CUIDADOS,
    PERFIL_MASCOTA,
    EDITAR_PERFIL,
    ALIMENTACION,
    AGREGAR_PRUEBA_ALIMENTACION,
    EDITAR_CUIDADO
}

class MainActivity : ComponentActivity() {
    private val viewModel: CuidaDogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CuidaDogTheme {
                val mascotas by viewModel.mascotas.collectAsState()
                val mascotaActiva by viewModel.mascotaActiva.collectAsState()
                val cuidadosHoy by viewModel.cuidadosHoy.collectAsState()
                val proximosDiezDias by viewModel.proximosDiezDias.collectAsState()
                val todosLosCuidados by viewModel.todosLosCuidados.collectAsState()
                val pruebasAlimentacion by viewModel.pruebasAlimentacion.collectAsState()
                val ultimaPruebaAlimentacion by viewModel.ultimaPruebaAlimentacion.collectAsState()
                val historialAlimentacion by viewModel.historialAlimentacion.collectAsState()

                var pantallaActual by remember { mutableStateOf(Pantalla.HOY) }
                var creandoMascotaNueva by remember { mutableStateOf(false) }
                var cuidadoEnEdicion by remember { mutableStateOf<CuidadoEntity?>(null) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val mascotaActual = mascotaActiva
                        if (mascotaActual != null && mascotas.isNotEmpty() && !creandoMascotaNueva && pantallaActual != Pantalla.AGREGAR_CUIDADO && pantallaActual != Pantalla.EDITAR_PERFIL && pantallaActual != Pantalla.AGREGAR_PRUEBA_ALIMENTACION && pantallaActual != Pantalla.EDITAR_CUIDADO) {
                            NavigationBar {
                                NavigationBarItem(
                                    icon = { Text("🏠") },
                                    label = { Text("Inicio") },
                                    selected = pantallaActual == Pantalla.HOY,
                                    onClick = { pantallaActual = Pantalla.HOY }
                                )
                                NavigationBarItem(
                                    icon = { AlimentacionIcon(size = 24.dp) },
                                    label = { Text("Alimentación") },
                                    selected = pantallaActual == Pantalla.ALIMENTACION,
                                    onClick = { pantallaActual = Pantalla.ALIMENTACION }
                                )
                                NavigationBarItem(
                                    icon = { Text("📋") },
                                    label = { Text("Ver cuidados") },
                                    selected = pantallaActual == Pantalla.TODOS_CUIDADOS,
                                    onClick = { pantallaActual = Pantalla.TODOS_CUIDADOS }
                                )
                                NavigationBarItem(
                                    icon = { Text("🐾") },
                                    label = { Text("Perfil") },
                                    selected = pantallaActual == Pantalla.PERFIL_MASCOTA,
                                    onClick = { pantallaActual = Pantalla.PERFIL_MASCOTA }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (mascotas.isEmpty() || creandoMascotaNueva) {
                            CrearPerroScreen(
                                onVolver = if (mascotas.isNotEmpty()) {
                                    { creandoMascotaNueva = false }
                                } else {
                                    null
                                },
                                onGuardar = { nombre, especie, raza, sexo, fNac, edad, talla, peso, color, castrado, microchip, alergias, med, vet, obs, fotoUri, tipoDieta, marca, cantRacion, racDia, horarios, restricciones, urgencias, contacto1, contacto2, contacto3, vacunas, desparasitacion, cronicas, historialCelos ->
                                    viewModel.guardarMascota(
                                        nombre, especie, raza, sexo, fNac, edad, talla, peso, color, castrado, microchip, alergias, med, vet, obs, fotoUri,
                                        tipoDieta, marca, cantRacion, racDia, horarios, restricciones, urgencias, contacto1, contacto2, contacto3, vacunas, desparasitacion, cronicas, historialCelos
                                    ) { id ->
                                        viewModel.seleccionarMascota(id)
                                        creandoMascotaNueva = false
                                        pantallaActual = Pantalla.HOY
                                    }
                                }
                            )
                        } else {
                            val mascotaActual = mascotaActiva
                            if (mascotaActual != null) {
                                when (pantallaActual) {
                                    Pantalla.HOY -> {
                                        HoyScreen(
                                            nombrePerro = mascotaActual.nombre,
                                            fotoUri = mascotaActual.fotoUri,
                                            listaMascotas = mascotas,
                                            mascotaActiva = mascotaActual,
                                            onSeleccionarMascota = { id ->
                                                viewModel.seleccionarMascota(id)
                                            },
                                            onAgregarMascotaClick = {
                                                creandoMascotaNueva = true
                                            },
                                            cuidadosHoy = cuidadosHoy,
                                            cuidadosProximos10Dias = proximosDiezDias,
                                            onAgregarCuidadoClick = {
                                                pantallaActual = Pantalla.AGREGAR_CUIDADO
                                            },
                                            onVerTodosCuidadosClick = {
                                                pantallaActual = Pantalla.TODOS_CUIDADOS
                                            },
                                            onCambiarFotoPerro = { nuevaFotoUri ->
                                                viewModel.actualizarFotoMascota(mascotaActual.id, nuevaFotoUri)
                                            },
                                            onToggleRealizado = { c ->
                                                viewModel.toggleRealizado(c)
                                            },
                                            onMarcarTodosRealizados = { cuidados ->
                                                viewModel.marcarTodosRealizados(cuidados)
                                            },
                                            onEditarCuidado = { c ->
                                                cuidadoEnEdicion = c
                                                pantallaActual = Pantalla.EDITAR_CUIDADO
                                            }
                                        )
                                    }
                                    Pantalla.AGREGAR_CUIDADO -> {
                                        AgregarCuidadoScreen(
                                            onVolver = {
                                                pantallaActual = Pantalla.HOY
                                            },
                                            onGuardar = { nuevoCuidado ->
                                                viewModel.guardarCuidado(nuevoCuidado) {
                                                    pantallaActual = Pantalla.HOY
                                                }
                                            }
                                        )
                                    }
                                    Pantalla.EDITAR_CUIDADO -> {
                                        val cuidado = cuidadoEnEdicion
                                        if (cuidado != null) {
                                            EditarCuidadoScreen(
                                                cuidadoInicial = cuidado,
                                                onVolver = {
                                                    pantallaActual = Pantalla.HOY
                                                },
                                                onGuardarCambios = { actualizado ->
                                                    viewModel.actualizarCuidado(actualizado) {
                                                        pantallaActual = Pantalla.HOY
                                                    }
                                                }
                                            )
                                        }
                                    }
                                    Pantalla.TODOS_CUIDADOS -> {
                                        TodosCuidadosScreen(
                                            todosLosCuidados = todosLosCuidados,
                                            onVolver = {
                                                pantallaActual = Pantalla.HOY
                                            },
                                            onEliminarCuidado = { cuidado ->
                                                viewModel.eliminarCuidado(cuidado)
                                            },
                                            onToggleRealizado = { c ->
                                                viewModel.toggleRealizado(c)
                                            },
                                            onMarcarTodosRealizados = { cuidados ->
                                                viewModel.marcarTodosRealizados(cuidados)
                                            },
                                            onEditarCuidado = { c ->
                                                cuidadoEnEdicion = c
                                                pantallaActual = Pantalla.EDITAR_CUIDADO
                                            }
                                        )
                                    }
                                    Pantalla.PERFIL_MASCOTA -> {
                                        PerfilMascotaScreen(
                                            mascota = mascotaActual,
                                            cuidadosRealizados = todosLosCuidados.filter { it.realizado },
                                            ultimaPruebaAlimentacion = ultimaPruebaAlimentacion,
                                            onVolver = {
                                                pantallaActual = Pantalla.HOY
                                            },
                                            onEditarClick = {
                                                pantallaActual = Pantalla.EDITAR_PERFIL
                                            },
                                            onActualizarHistorialCelos = { mascota, nuevoHistorial ->
                                                viewModel.actualizarHistorialCelos(mascota, nuevoHistorial)
                                            }
                                        )
                                    }
                                    Pantalla.ALIMENTACION -> {
                                        AlimentacionScreen(
                                            mascota = mascotaActual,
                                            pruebas = pruebasAlimentacion,
                                            ultimaPrueba = ultimaPruebaAlimentacion,
                                            historial = historialAlimentacion,
                                            onActualizarEstadoPrueba = { prueba, nuevoEstado, continuarCuidados ->
                                                viewModel.actualizarEstadoPrueba(prueba, nuevoEstado, continuarCuidados)
                                            },
                                            onActualizarAlimentacionPerfil = { tipo, marca, cant, raciones, horarios, restricciones ->
                                                viewModel.actualizarAlimentacionPerfil(tipo, marca, cant, raciones, horarios, restricciones) {
                                                    android.widget.Toast.makeText(baseContext, "Alimentación del perfil actualizada correctamente", android.widget.Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            onAgregarEvolucion = { pruebaId, fecha, valo, obs ->
                                                viewModel.agregarEvolucion(pruebaId, fecha, valo, obs)
                                            },
                                            getEvoluciones = { id ->
                                                viewModel.getEvolucionesPorPrueba(id)
                                            },
                                            onAgregarPruebaClick = {
                                                pantallaActual = Pantalla.AGREGAR_PRUEBA_ALIMENTACION
                                            },
                                            onVolver = {
                                                pantallaActual = Pantalla.HOY
                                            }
                                        )
                                    }
                                    Pantalla.AGREGAR_PRUEBA_ALIMENTACION -> {
                                        AgregarPruebaAlimentacionScreen(
                                            onVolver = {
                                                pantallaActual = Pantalla.ALIMENTACION
                                            },
                                            onGuardar = { tipo, marca, cant, unidad, raciones, horarios, fechaIni, fechaFin, obs, estado ->
                                                viewModel.guardarPrueba(tipo, marca, cant, unidad, raciones, horarios, fechaIni, fechaFin, obs, estado)
                                                pantallaActual = Pantalla.ALIMENTACION
                                            }
                                        )
                                    }
                                    Pantalla.EDITAR_PERFIL -> {
                                        EditarMascotaScreen(
                                            mascotaInicial = mascotaActual,
                                            onVolver = {
                                                pantallaActual = Pantalla.PERFIL_MASCOTA
                                            },
                                            onGuardarCambios = { mascotaActualizada ->
                                                viewModel.actualizarMascota(mascotaActualizada) {
                                                    pantallaActual = Pantalla.PERFIL_MASCOTA
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
