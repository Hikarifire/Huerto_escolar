package cl.duoc.fotomotion.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.fotomotion.model.*
import cl.duoc.fotomotion.viewmodel.LaboresViewModel
import cl.duoc.fotomotion.ui.components.*
import kotlinx.coroutines.launch

@Composable
fun PantallaLabores(controlLabores: LaboresViewModel, cuentaEnSesion: CuentaHuerto) {
    val datosVista by controlLabores.estadoLabores.collectAsState()
    val permiteRevision = cuentaEnSesion.tipoCuenta == TipoCuenta.DOCENTE
    var laborPorBorrar by remember { mutableStateOf<LaborHuerto?>(null) }
    val posicionLista = rememberLazyListState()
    val accionesPantalla = rememberCoroutineScope()

    LazyColumn(state = posicionLista, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AvisoHuerto(datosVista.avisoLabores) }
        if (permiteRevision) item {
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp)) {
                    Text(if (datosVista.laborEnEdicion == null) "Nueva tarea" else "Editar tarea", style = MaterialTheme.typography.titleMedium)
                    CampoHuerto(datosVista.textoDetalle, "Descripción de la tarea", datosVista.errorDetalle,
                        controlLabores::escribirDetalle, !datosVista.guardandoLabor)
                    CampoHuerto(datosVista.textoFecha, "Fecha (AAAA-MM-DD)", datosVista.errorFecha,
                        controlLabores::escribirFecha, !datosVista.guardandoLabor)
                    ElegirSector(datosVista.sectoresDisponibles, datosVista.sectorTrabajo, datosVista.errorSector,
                        controlLabores::elegirSectorTrabajo, !datosVista.guardandoLabor)
                    Button(onClick = { controlLabores.confirmarLabor(cuentaEnSesion) }, enabled = !datosVista.guardandoLabor) { Text("Guardar tarea") }
                    if (datosVista.laborEnEdicion != null) TextButton(onClick = controlLabores::limpiarLabor, enabled = !datosVista.guardandoLabor) { Text("Cancelar edición") }
                }
            }
        }
        if (!permiteRevision && datosVista.laborEnEntrega != null) item {
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Entregar: ${datosVista.laborEnEntrega?.detalleLabor}", style = MaterialTheme.typography.titleMedium)
                    AnimatedVisibility(visible = datosVista.direccionFoto.isNotBlank()) {
                        FotoHuerto(datosVista.direccionFoto)
                    }
                    AdjuntarFoto(controlLabores::adjuntarImagen, controlLabores::informarLabor, !datosVista.guardandoLabor)
                    if (datosVista.errorFoto.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Warning, "Error de fotografía", tint = MaterialTheme.colorScheme.error)
                        Text(datosVista.errorFoto, color = MaterialTheme.colorScheme.error)
                    }
                    CampoHuerto(datosVista.textoComentario, "Comentario del trabajo", datosVista.errorComentario,
                        controlLabores::escribirComentario, !datosVista.guardandoLabor)
                    Button(onClick = { controlLabores.entregarLabor(cuentaEnSesion) }, enabled = !datosVista.guardandoLabor) { Text("Enviar evidencia") }
                    TextButton(onClick = controlLabores::limpiarLabor, enabled = !datosVista.guardandoLabor) { Text("Cancelar entrega") }
                }
            }
        }
        item { Text("Tareas registradas", style = MaterialTheme.typography.titleLarge) }
        if (datosVista.listaLabores.isEmpty()) item { Text("Todavía no hay tareas. Ingresa como docente para crear una.") }
        items(datosVista.listaLabores, key = { labor -> labor.codigoLabor }) { labor ->
            Card(Modifier.fillMaxWidth().animateContentSize()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(labor.detalleLabor, style = MaterialTheme.typography.titleMedium)
                    val nombreSector = datosVista.sectoresDisponibles.find { parcela -> parcela.codigoParcela == labor.parcelaTrabajo }?.tituloParcela ?: "Sector no disponible"
                    Text("Sector: $nombreSector · ${labor.diaPlanificado}")
                    Text("Estado: " + when (labor.situacionLabor) {
                        "VALIDADA" -> "Aprobada"
                        "RECHAZADA" -> "Requiere corrección"
                        "PENDIENTE_DE_VALIDACION" -> "En revisión"
                        else -> "Pendiente"
                    })
                    if (!labor.imagenLabor.isNullOrBlank()) FotoHuerto(labor.imagenLabor)
                    if (!labor.notaEntrega.isNullOrBlank()) Text("Estudiante: ${labor.notaEntrega}")
                    if (!labor.notaRevision.isNullOrBlank()) Text("Docente: ${labor.notaRevision}")
                    if (!permiteRevision && (labor.situacionLabor == "PENDIENTE" || labor.situacionLabor == "RECHAZADA")) {
                        Button(onClick = {
                            controlLabores.iniciarEntrega(labor)
                            accionesPantalla.launch { posicionLista.animateScrollToItem(0) }
                        }, enabled = !datosVista.guardandoLabor) { Text("Realizar tarea") }
                    }
                    if (permiteRevision && labor.situacionLabor == "PENDIENTE_DE_VALIDACION") {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { controlLabores.revisarTrabajo(labor, true, cuentaEnSesion) }, enabled = !datosVista.guardandoLabor) { Text("Aprobar") }
                            OutlinedButton(onClick = { controlLabores.revisarTrabajo(labor, false, cuentaEnSesion) }, enabled = !datosVista.guardandoLabor) { Text("Rechazar") }
                        }
                    }
                    if (permiteRevision) Row {
                        TextButton(onClick = {
                            controlLabores.prepararLabor(labor)
                            accionesPantalla.launch { posicionLista.animateScrollToItem(0) }
                        }, enabled = !datosVista.guardandoLabor) { Text("Editar tarea") }
                        TextButton(onClick = { laborPorBorrar = labor }, enabled = !datosVista.guardandoLabor) { Text("Borrar tarea") }
                    }
                }
            }
        }
    }
    laborPorBorrar?.let { labor ->
        ConfirmarBorrado(labor.detalleLabor, alBorrar = {
            controlLabores.eliminarLabor(labor)
            laborPorBorrar = null
        }, alCancelar = { laborPorBorrar = null })
    }
}
