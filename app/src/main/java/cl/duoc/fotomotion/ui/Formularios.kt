package cl.duoc.fotomotion.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.fotomotion.domain.*

@Composable
fun EditorSector(sector: Sector?, estado: HuertoUiState, guardar: (Accion) -> Unit) {
    var nombre by rememberSaveable(sector?.id) { mutableStateOf(sector?.nombre.orEmpty()) }
    var ubicacion by rememberSaveable(sector?.id) { mutableStateOf(sector?.ubicacion.orEmpty()) }
    var descripcion by rememberSaveable(sector?.id) { mutableStateOf(sector?.descripcion.orEmpty()) }
    var activo by rememberSaveable(sector?.id) { mutableStateOf(sector?.activo ?: true) }
    val e = estado.campos

    Pagina {
        Encabezado(if (sector == null) "Nuevo sector" else "Editar sector")
        Campo("Nombre", nombre, { nombre = it }, e["nombre"])
        Campo("Ubicación", ubicacion, { ubicacion = it }, e["ubicación"])
        Campo("Descripción", descripcion, { descripcion = it }, e["descripción"], multilinea = true)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Sector activo")
            Switch(activo, { activo = it })
        }
        Guardar("Guardar sector", !estado.ocupado) {
            guardar(Accion.GuardarSector(sector?.id, SectorForm(nombre, ubicacion, descripcion, activo)))
        }
    }
}

@Composable
fun EditorCultivo(cultivo: Cultivo?, estado: HuertoUiState, guardar: (Accion) -> Unit) {
    var nombre by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.nombre.orEmpty()) }
    var tipo by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.tipo.orEmpty()) }
    var descripcion by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.descripcion.orEmpty()) }
    var siembra by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.fechaSiembra.orEmpty()) }
    var trasplante by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.fechaTrasplante.orEmpty()) }
    var cosecha by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.fechaCosechaEstimada.orEmpty()) }
    var riego by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.frecuenciaRiegoDias?.toString().orEmpty()) }
    var desarrollo by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.estado ?: "SEMBRADO") }
    var sector by rememberSaveable(cultivo?.id) { mutableStateOf(cultivo?.sectorId) }
    val e = estado.campos

    Pagina {
        Encabezado(if (cultivo == null) "Registrar cultivo" else "Editar cultivo")
        Campo("Nombre", nombre, { nombre = it }, e["nombre"])
        Campo("Variedad", tipo, { tipo = it }, e["variedad"])
        Campo("Descripción", descripcion, { descripcion = it }, e["descripción"], multilinea = true)
        Selector("Sector", estado.datos.sectores.map { it.id to it.nombre }, sector, e["sector"]) { sector = it }
        Selector("Estado", Reglas.estadosDeCultivo.map { it to etiqueta(it) }, desarrollo, e["estado"]) { desarrollo = it }
        Campo("Siembra (AAAA-MM-DD)", siembra, { siembra = it }, e["siembra"])
        Campo("Trasplante (opcional)", trasplante, { trasplante = it }, e["trasplante"])
        Campo("Cosecha estimada (opcional)", cosecha, { cosecha = it }, e["cosecha"])
        Campo("Riego cada cuántos días", riego, { riego = it }, e["riego"], teclado = KeyboardType.Number)
        Guardar("Guardar cultivo", !estado.ocupado) {
            guardar(Accion.GuardarCultivo(cultivo?.id, CultivoForm(
                nombre = nombre, tipo = tipo, descripcion = descripcion, fechaSiembra = siembra.trim(),
                fechaCosechaEstimada = cosecha.trim().takeIf(String::isNotEmpty),
                fechaTrasplante = trasplante.trim().takeIf(String::isNotEmpty), frecuenciaRiegoDias = riego.toIntOrNull(),
                estado = desarrollo, sectorId = sector,
            )))
        }
    }
}

@Composable
fun DetalleCultivo(cultivo: Cultivo, docente: Boolean, ocupado: Boolean, editar: () -> Unit, borrar: () -> Unit) {
    Pagina {
        Encabezado(cultivo.nombre, "${cultivo.sectorNombre} · ${etiqueta(cultivo.estado)}")
        cultivo.tipo?.takeIf(String::isNotBlank)?.let { Text("Variedad · $it") }
        cultivo.descripcion?.takeIf(String::isNotBlank)?.let { Text(it) }
        cultivo.fechaSiembra?.let { Text("Siembra · $it") }
        cultivo.fechaTrasplante?.let { Text("Trasplante · $it") }
        cultivo.fechaCosechaEstimada?.let { Text("Cosecha estimada · $it") }
        cultivo.frecuenciaRiegoDias?.let { Text("Riego cada $it días") }
        if (docente) {
            Guardar("Editar cultivo", !ocupado, editar)
            Borrar("Eliminar cultivo", !ocupado, borrar)
        }
    }
}

@Composable
fun EditorTarea(tarea: Tarea?, estado: HuertoUiState, guardar: (Accion) -> Unit) {
    var titulo by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.titulo.orEmpty()) }
    var descripcion by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.descripcion.orEmpty()) }
    var fecha by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.fechaLimite.orEmpty()) }
    var sector by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.sectorId) }
    var cultivo by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.cultivoId) }
    var alumno by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.estudianteId) }
    var tipo by rememberSaveable(tarea?.id) { mutableStateOf(tarea?.tipo ?: "OBSERVACION") }
    val datos = estado.datos
    val e = estado.campos

    Pagina {
        Encabezado(if (tarea == null) "Nueva tarea" else "Editar tarea")
        Campo("Título", titulo, { titulo = it }, e["título"])
        Campo("Instrucciones", descripcion, { descripcion = it }, e["instrucciones"], multilinea = true)
        Campo("Fecha programada (AAAA-MM-DD)", fecha, { fecha = it }, e["fecha"])
        Selector("Tipo", Reglas.tiposDeTarea.map { it to etiqueta(it) }, tipo, e["tipo"]) { tipo = it }
        Selector("Sector", datos.sectores.map { it.id to it.nombre }, sector, e["sector"]) {
            sector = it
            cultivo = null
        }
        SelectorCultivo(datos, sector, cultivo, e["cultivo"]) { cultivo = it }
        Selector("Estudiante", datos.alumnos.map { it.id to it.nombre }, alumno, e["estudiante"]) { alumno = it }
        Guardar("Guardar tarea", !estado.ocupado) {
            guardar(Accion.GuardarTarea(tarea?.id, TareaForm(titulo, descripcion, fecha.trim(), sector, cultivo, alumno, tipo)))
        }
    }
}

@Composable
private fun SelectorCultivo(datos: Datos, sector: Long?, cultivo: Long?, error: String?, cambiar: (Long?) -> Unit) {
    val opciones = listOf(0L to "Sin cultivo") + datos.cultivos.filter { it.sectorId == sector }.map { it.id to it.nombre }
    Selector("Cultivo", opciones, cultivo ?: 0L, error) { cambiar(it.takeIf { id -> id != 0L }) }
}

@Composable
fun EditorRevision(evidencia: Evidencia, estado: HuertoUiState, store: HuertoStore, guardar: (Accion) -> Unit) {
    var comentario by rememberSaveable(evidencia.id) { mutableStateOf("") }
    Pagina {
        Encabezado("Revisar entrega")
        Text(evidencia.comentario)
        FotoRemota(evidencia.fotoUrl, store)
        Campo("Motivo de rechazo o comentario", comentario, { comentario = it }, estado.campos["motivo"], multilinea = true)
        Guardar("Aprobar entrega", !estado.ocupado) {
            guardar(Accion.Revisar(evidencia.id, Revision(true, comentario.trim().takeIf(String::isNotEmpty))))
        }
        OutlinedButton(onClick = { guardar(Accion.Revisar(evidencia.id, Revision(false, comentario.trim()))) },
            enabled = !estado.ocupado, modifier = Modifier.fillMaxWidth()) { Text("Rechazar entrega") }
    }
}

@Composable
fun EditorReporte(estado: HuertoUiState, guardar: (Accion) -> Unit) {
    var descripcion by rememberSaveable { mutableStateOf("") }
    var sector by rememberSaveable { mutableStateOf<Long?>(null) }
    var cultivo by rememberSaveable { mutableStateOf<Long?>(null) }
    var foto by rememberSaveable { mutableStateOf<String?>(null) }
    val datos = estado.datos
    val e = estado.campos

    Pagina {
        Encabezado("Reportar un problema")
        Campo("Describe el problema", descripcion, { descripcion = it }, e["descripción"], multilinea = true)
        Selector("Sector", datos.sectores.map { it.id to it.nombre }, sector, e["sector"]) {
            sector = it
            cultivo = null
        }
        SelectorCultivo(datos, sector, cultivo, e["cultivo"]) { cultivo = it }
        Text("Fotografía opcional")
        FotoLocal(foto) { foto = it }
        Guardar("Enviar reporte", !estado.ocupado) { guardar(Accion.NuevoReporte(sector, cultivo, descripcion, foto)) }
    }
}

@Composable
fun DetalleReporte(reporte: Reporte, estado: HuertoUiState, store: HuertoStore, guardar: (Accion) -> Unit) {
    var situacion by rememberSaveable(reporte.id, reporte.estado) { mutableStateOf(reporte.estado) }
    var seguimiento by rememberSaveable(reporte.id, reporte.seguimiento) { mutableStateOf(reporte.seguimiento.orEmpty()) }
    Pagina {
        Encabezado("Detalle del reporte", "${reporte.sectorNombre} · ${etiqueta(reporte.estado)}")
        Text(reporte.descripcion)
        Text("Reportado por ${reporte.autor}")
        reporte.cultivoNombre?.let { Text("Cultivo · $it") }
        reporte.fotoUrl?.let { FotoRemota(it, store) }
        reporte.seguimiento?.let { Text("Seguimiento · $it") }
        reporte.atendidoPor?.let { Text("Última revisión de $it") }
        if (estado.sesion?.usuario?.esDocente == true) {
            Selector("Estado", Reglas.estadosDeReporte.map { it to etiqueta(it) }, situacion, estado.campos["estado"]) {
                situacion = it
            }
            Campo("Seguimiento", seguimiento, { seguimiento = it }, estado.campos["seguimiento"], multilinea = true)
            Guardar("Actualizar reporte", !estado.ocupado) {
                guardar(Accion.Atender(reporte.id, Seguimiento(situacion, seguimiento)))
            }
        }
    }
}
