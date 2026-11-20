package cl.duoc.fotomotion.domain

import java.time.LocalDate

object Reglas {
    val tiposDeTarea = listOf("RIEGO", "SIEMBRA", "MANTENIMIENTO", "OBSERVACION", "COSECHA")
    val estadosDeCultivo = listOf("SEMBRADO", "EN_CRECIMIENTO", "LISTO_PARA_COSECHA", "COSECHADO")
    val estadosDeReporte = listOf("ABIERTO", "EN_ATENCION", "RESUELTO")
    private val estadosAbiertos = setOf("PENDIENTE", "EN_DESARROLLO", "RECHAZADA")
    private val correoValido = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

    fun acceso(datos: Acceso): Map<String, String> = buildMap {
        if (!correoValido.matches(datos.correo.trim()) || datos.correo.length > 160)
            put("correo", "Escribe un correo válido.")
        if (datos.password.isBlank() || datos.password.toByteArray().size > 72 ||
            (datos.nombre != null && datos.password.length < 8))
            put("contraseña", "Usa una contraseña válida de hasta 72 bytes. El registro requiere 8 caracteres.")
        datos.nombre?.let { texto("nombre", it, 100) }
    }

    fun validar(accion: Accion, datos: Datos): Map<String, String> = buildMap {
        when (accion) {
            is Accion.GuardarSector -> with(accion.datos) {
                texto("nombre", nombre, 100)
                texto("ubicación", ubicacion, 150)
                opcional("descripción", descripcion, 500)
            }
            is Accion.GuardarCultivo -> with(accion.datos) {
                texto("nombre", nombre, 100)
                opcional("variedad", tipo, 80)
                opcional("descripción", descripcion, 500)
                sector(sectorId, datos)
                if (estado !in estadosDeCultivo) put("estado", "Selecciona un estado válido.")
                if (frecuenciaRiegoDias == null || frecuenciaRiegoDias <= 0)
                    put("riego", "Usa una frecuencia de riego positiva.")
                val siembra = fecha("siembra", fechaSiembra)
                val trasplante = fechaTrasplante?.let { fecha("trasplante", it) }
                val cosecha = fechaCosechaEstimada?.let { fecha("cosecha", it) }
                if (siembra != null && cosecha != null && cosecha < siembra)
                    put("cosecha", "La cosecha debe ser posterior o igual a la siembra.")
                if (trasplante != null && ((siembra != null && trasplante < siembra) ||
                    (cosecha != null && trasplante > cosecha)))
                    put("trasplante", "El trasplante debe quedar entre la siembra y la cosecha.")
            }
            is Accion.GuardarTarea -> with(accion.datos) {
                texto("título", titulo, 140)
                texto("instrucciones", descripcion, 1000)
                fecha("fecha", fechaLimite)
                sector(sectorId, datos)
                cultivo(cultivoId, sectorId, datos)
                if (datos.alumnos.none { it.id == estudianteId })
                    put("estudiante", "Selecciona un estudiante disponible.")
                if (tipo !in tiposDeTarea) put("tipo", "Selecciona un tipo de tarea.")
            }
            is Accion.Entregar -> {
                texto("comentario", accion.borrador.comentario, 1000)
                if (accion.borrador.foto.isNullOrBlank()) put("foto", "Adjunta una fotografía.")
            }
            is Accion.Revisar -> {
                val observacion = accion.revision.observacion.orEmpty()
                if (accion.revision.aprobada) opcional("motivo", observacion, 1000)
                else texto("motivo", observacion, 1000)
            }
            is Accion.NuevoReporte -> {
                texto("descripción", accion.descripcion, 1000)
                sector(accion.sectorId, datos)
                cultivo(accion.cultivoId, accion.sectorId, datos)
            }
            is Accion.Atender -> with(accion.seguimiento) {
                texto("seguimiento", seguimiento, 1000)
                if (estado !in estadosDeReporte) put("estado", "Selecciona un estado válido.")
            }
            is Accion.Simular -> with(accion.lectura) {
                if (humedad !in 0.0..100.0) put("humedad", "La humedad del suelo debe estar entre 0 y 100.")
                if (humedadAmbiental !in 0.0..100.0)
                    put("humedad ambiental", "La humedad ambiental debe estar entre 0 y 100.")
                if (temperatura !in -20.0..60.0) put("temperatura", "La temperatura debe estar entre −20 y 60 °C.")
            }
            is Accion.Eliminar, is Accion.Iniciar -> Unit
        }
    }

    private fun MutableMap<String, String>.texto(campo: String, valor: String, max: Int) {
        if (valor.isBlank()) put(campo, "Completa este campo.") else opcional(campo, valor, max)
    }

    private fun MutableMap<String, String>.opcional(campo: String, valor: String, max: Int) {
        if (valor.length > max) put(campo, "Usa como máximo $max caracteres.")
    }

    private fun MutableMap<String, String>.fecha(campo: String, valor: String): LocalDate? =
        leerFecha(valor).also { if (it == null) put(campo, "Escribe una fecha válida en formato AAAA-MM-DD.") }

    private fun MutableMap<String, String>.sector(id: Long?, datos: Datos) {
        if (datos.sectores.none { it.id == id }) put("sector", "Selecciona un sector válido.")
    }

    private fun MutableMap<String, String>.cultivo(id: Long?, sector: Long?, datos: Datos) {
        if (id != null && datos.cultivos.none { it.id == id && it.sectorId == sector })
            put("cultivo", "Selecciona un cultivo del sector indicado.")
    }

    fun atrasada(tarea: Tarea, hoy: LocalDate): Boolean =
        tarea.estado in estadosAbiertos && leerFecha(tarea.fechaLimite)?.isBefore(hoy) == true

    fun alertas(datos: Datos, hoy: LocalDate): List<Evento> {
        val recordatorios = datos.tareas.filter { atrasada(it, hoy) || (it.tipo == "MANTENIMIENTO" && it.estado in estadosAbiertos) }
            .map { Evento(if (atrasada(it, hoy)) "Tarea atrasada" else "Mantenimiento", it.titulo, it.fechaLimite) }
        val cultivos = datos.cultivos.filter { it.estado != "COSECHADO" }.flatMap { cultivo ->
            buildList {
                if (cultivo.fechaCosechaEstimada != null)
                    add(Evento("Cosecha estimada", cultivo.nombre, cultivo.fechaCosechaEstimada))
                val inicio = leerFecha(cultivo.fechaSiembra)
                val intervalo = cultivo.frecuenciaRiegoDias?.takeIf { it > 0 }?.toLong()
                if (inicio != null && intervalo != null) {
                    val dias = (hoy.toEpochDay() - inicio.toEpochDay()).coerceAtLeast(0)
                    val proximo = inicio.plusDays((dias / intervalo + 1) * intervalo)
                    add(Evento("Riego programado", cultivo.nombre, proximo.toString()))
                }
            }
        }
        return (recordatorios + cultivos).sortedBy { it.fecha }
    }

    fun historial(datos: Datos, evidencias: List<Evidencia>): List<Evento> {
        val tareas = datos.tareas.map { Evento("Tarea creada", it.titulo, it.fechaCreacion) }
        val entregas = evidencias.flatMap { evidencia ->
            buildList {
                add(Evento("Evidencia entregada", evidencia.comentario, evidencia.fechaRegistro))
                evidencia.fechaRevision?.let {
                    add(Evento("Revisión · ${evidencia.estado}", evidencia.observacionRevision ?: evidencia.revisadoPor.orEmpty(), it))
                }
            }
        }
        val problemas = datos.reportes.flatMap { reporte ->
            buildList {
                add(Evento("Problema reportado", reporte.descripcion, reporte.creadoEn))
                reporte.actualizadoEn?.let { add(Evento("Seguimiento de reporte", reporte.seguimiento.orEmpty(), it)) }
            }
        }
        return (tareas + entregas + problemas).sortedByDescending { it.fecha }
    }

    private fun leerFecha(valor: String?): LocalDate? = valor?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}
