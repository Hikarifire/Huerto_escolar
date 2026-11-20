package cl.duoc.fotomotion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.duoc.fotomotion.domain.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ListaTareas(estado: HuertoUiState, docente: Boolean, hoy: LocalDate, navegar: (String) -> Unit) {
    var filtro by rememberSaveable { mutableStateOf("TODAS") }
    var responsable by rememberSaveable { mutableStateOf(0L) }
    var fecha by rememberSaveable { mutableStateOf("") }
    val tareas = estado.datos.tareas.filter {
        (filtro == "TODAS" || it.estado == filtro || (filtro == "ATRASADAS" && Reglas.atrasada(it, hoy))) &&
            (responsable == 0L || it.estudianteId == responsable) &&
            (fecha.isBlank() || it.fechaLimite == fecha.trim())
    }
    Pagina {
        Encabezado(if (docente) "Tareas del huerto" else "Mis tareas")
        if (docente) Guardar("Nueva tarea", !estado.ocupado) { navegar("tareaEditar/0") }
        Selector("Estado", listOf("TODAS", "PENDIENTE", "EN_DESARROLLO", "EN_REVISION", "APROBADA", "RECHAZADA",
            "ATRASADAS").map { it to etiqueta(it) }, filtro) { filtro = it }
        if (docente) Selector("Responsable", listOf(0L to "Todos") + estado.datos.alumnos.map { it.id to it.nombre },
            responsable) { responsable = it }
        Campo("Filtrar fecha (AAAA-MM-DD)", fecha, { fecha = it })
        if (tareas.isEmpty()) Text("No hay tareas para esta búsqueda.")
        tareas.forEach { tarea ->
            val situacion = if (Reglas.atrasada(tarea, hoy)) "Atrasada" else etiqueta(tarea.estado)
            Tarjeta(tarea.titulo, "$situacion · ${tarea.fechaLimite}\n${tarea.sectorNombre} · ${tarea.estudianteNombre}") {
                navegar("tarea/${tarea.id}")
            }
        }
    }
}

@Composable
fun DetalleTarea(tarea: Tarea, estado: HuertoUiState, store: HuertoStore, hoy: LocalDate,
                 navegar: (String) -> Unit, guardar: (Accion) -> Unit) {
    val docente = estado.sesion?.usuario?.esDocente == true
    val scope = rememberCoroutineScope()
    val evidencias = estado.evidencia[tarea.id].orEmpty()
    var entrega by rememberSaveable(tarea.id) { mutableStateOf("") }
    var foto by rememberSaveable(tarea.id) { mutableStateOf<String?>(null) }
    var borradorListo by remember(tarea.id) { mutableStateOf(false) }

    LaunchedEffect(tarea.id, tarea.estado) {
        if (!docente && tarea.estado == "EN_DESARROLLO" && !borradorListo) {
            val borrador = store.borrador(tarea.id)
            entrega = borrador.comentario
            foto = borrador.foto
            borradorListo = true
        }
        store.evidencias(tarea.id)
    }

    Pagina {
        Encabezado(tarea.titulo, "${etiqueta(tarea.tipo)} · ${etiqueta(tarea.estado)}")
        Text(tarea.descripcion)
        Text("Fecha programada · ${tarea.fechaLimite}")
        Text("Lugar · ${tarea.sectorNombre}${tarea.cultivoNombre?.let { " · $it" }.orEmpty()}")
        Text("Responsable · ${tarea.estudianteNombre}")
        AnimatedVisibility(Reglas.atrasada(tarea, hoy)) {
            Text("Tarea atrasada", color = MaterialTheme.colorScheme.error)
        }
        if (docente) {
            if (tarea.estado !in setOf("EN_REVISION", "APROBADA")) {
                Guardar("Editar tarea", !estado.ocupado) { navegar("tareaEditar/${tarea.id}") }
            }
            if (estado.evidencia.containsKey(tarea.id) && evidencias.isEmpty()) {
                Borrar("Eliminar tarea", !estado.ocupado) { guardar(Accion.Eliminar(Recurso.TAREA, tarea.id)) }
            }
        } else {
            when (tarea.estado) {
                "PENDIENTE", "RECHAZADA" -> Guardar(
                    if (tarea.estado == "RECHAZADA") "Iniciar corrección" else "Iniciar tarea", !estado.ocupado,
                ) { guardar(Accion.Iniciar(tarea.id)) }
                "EN_DESARROLLO" -> {
                    Campo("Comentario de la entrega", entrega, { entrega = it }, estado.campos["comentario"], multilinea = true)
                    FotoLocal(foto, estado.campos["foto"]) { foto = it }
                    OutlinedButton(onClick = { scope.launch { store.guardarBorrador(tarea.id, Borrador(entrega, foto)) } },
                        enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth()) { Text("Guardar borrador local") }
                    Guardar("Enviar evidencia", !estado.ocupado) {
                        guardar(Accion.Entregar(tarea.id, Borrador(entrega, foto)))
                    }
                }
            }
        }
        Text("Entregas y revisiones", style = MaterialTheme.typography.titleLarge)
        if (evidencias.isEmpty()) Text("Todavía no hay evidencias.")
        evidencias.forEach { evidencia ->
            Tarjeta(etiqueta(evidencia.estado), "${evidencia.comentario}\n${evidencia.fechaRegistro.replace('T', ' ').take(16)}")
            FotoRemota(evidencia.fotoUrl, store)
            evidencia.observacionRevision?.let { Text("Comentario docente · $it") }
            evidencia.revisadoPor?.let { Text("Revisado por $it") }
            if (docente && tarea.estado == "EN_REVISION" && evidencia.estado == "EN_REVISION") {
                Guardar("Revisar entrega", !estado.ocupado) { navegar("revisar/${tarea.id}/${evidencia.id}") }
            }
        }
    }
}

@Composable
fun Lecturas(estado: HuertoUiState, crear: (Accion) -> Unit) {
    Pagina {
        Encabezado("Lecturas de sensores", "Datos simulados para la demostración académica.")
        if (estado.sesion?.usuario?.esDocente == true) {
            Guardar("Registrar lectura simulada", !estado.ocupado) {
                crear(Accion.Simular(LecturaForm(
                    humedad = (38..75).random().toDouble(),
                    temperatura = (16..28).random().toDouble(),
                    humedadAmbiental = (43..65).random().toDouble(),
                    sectorId = estado.datos.sectores.firstOrNull()?.id,
                )))
            }
        }
        if (estado.datos.lecturas.isEmpty()) Text("Todavía no hay lecturas.")
        estado.datos.lecturas.forEach { lectura ->
            Tarjeta(lectura.sectorNombre ?: "Lectura general",
                "Suelo ${lectura.humedad}% · Aire ${lectura.humedadAmbiental?.let { "$it%" } ?: "sin dato"}" +
                    " · ${lectura.temperatura} °C\n${lectura.fechaRegistro.replace('T', ' ').take(16)}")
        }
    }
}

@Composable
fun ClimaPantalla(estado: HuertoUiState, consultar: () -> Unit) {
    Pagina {
        Encabezado("Clima en San Bernardo", "Datos de Open-Meteo para la comuna del huerto.")
        AnimatedVisibility(estado.cargandoClima) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        estado.clima?.let { clima ->
            Tarjeta("${clima.temperatura} °C", "Temperatura exterior")
            Tarjeta("${clima.humedad}%", "Humedad relativa")
            Tarjeta("${clima.precip} mm", "Precipitación")
            Text("Actualización · ${clima.hora.replace('T', ' ')}",
                style = MaterialTheme.typography.bodySmall)
        }
        estado.errorClima?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(2.dp))
        Guardar("Consultar clima", !estado.cargandoClima, consultar)
    }
}
