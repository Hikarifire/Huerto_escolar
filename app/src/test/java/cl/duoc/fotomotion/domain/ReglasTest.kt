package cl.duoc.fotomotion.domain

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

fun tarea(id: Long = 6, estado: String = "PENDIENTE", tipo: String = "RIEGO", fecha: String = "2026-11-18") =
    Tarea(id, "Regar semillero", "Riego manual", fecha, estado, tipo, 1, "Patio", 2, "Tomate", 3, "Alumno", "2026-11-11T10:00")

fun evidencia() = Evidencia(42, 6, "/api/evidencias/42/foto", "Trabajo hecho", "EN_REVISION", null, null, "2026-11-20T08:00", null)

class ReglasTest {
    private val sector = Sector(1, "Patio", "Patio norte", null, true)
    private val cultivo = Cultivo(2, "Tomate", null, null, "2026-11-10", "2026-12-15", null,
        3, "SEMBRADO", 1, "Patio")
    private val alumno = Usuario(3, "Alumno", "alumno@huerto.cl", "ALUMNO")
    private val datos = Datos(listOf(sector), listOf(cultivo), alumnos = listOf(alumno))
    private val hoy = LocalDate.of(2026, 11, 20)
    private val cultivoBase = CultivoForm(nombre = "Tomate", fechaSiembra = "2026-11-10", frecuenciaRiegoDias = 3, sectorId = 1)
    private val tareaBase = TareaForm("Trabajo", "Explicación", "2026-11-21", 1, 2, 3, "RIEGO")

    @Test
    fun accesoAdmiteRegistroValidoYRechazaDatosInvalidos() {
        assertTrue(Reglas.acceso(Acceso(" u@escuela.cl ", "clave123", "Estudiante")).isEmpty())
        assertEquals(setOf("correo", "contraseña", "nombre"), Reglas.acceso(Acceso("a@", "corto", "")).keys)
        assertEquals("contraseña", Reglas.acceso(Acceso("a@b.cl", "á".repeat(40))).keys.single())
    }

    @Test
    fun validaSectoresYCultivos() {
        assertTrue(Reglas.validar(Accion.GuardarSector(1, SectorForm("Patio", "Entrada")), datos).isEmpty())
        assertEquals(setOf("nombre", "ubicación", "descripción"),
            Reglas.validar(Accion.GuardarSector(null, SectorForm("", "", "x".repeat(501))), datos).keys)
        assertTrue(Reglas.validar(Accion.GuardarCultivo(null, cultivoBase), datos).isEmpty())
        val invalido = cultivoBase.copy(nombre = "x".repeat(101), tipo = "x".repeat(81), sectorId = 8, estado = "?",
            frecuenciaRiegoDias = 0, fechaTrasplante = "2026-10-10", fechaCosechaEstimada = "2026-11-02")
        assertEquals(setOf("nombre", "variedad", "sector", "estado", "riego", "trasplante", "cosecha"),
            Reglas.validar(Accion.GuardarCultivo(2, invalido), datos).keys)
        assertEquals("siembra", Reglas.validar(Accion.GuardarCultivo(null, cultivoBase.copy(fechaSiembra = "2026-02-30")),
            datos).keys.single())
    }

    @Test
    fun validaAsignacionYDatosDeTarea() {
        assertTrue(Reglas.validar(Accion.GuardarTarea(null, tareaBase), datos).isEmpty())
        val invalida = tareaBase.copy(titulo = "", descripcion = "", fechaLimite = "", estudianteId = 8, sectorId = 7, tipo = "OTRO")
        assertEquals(setOf("título", "instrucciones", "fecha", "estudiante", "sector", "cultivo", "tipo"),
            Reglas.validar(Accion.GuardarTarea(5, invalida), datos).keys)
        assertTrue(Reglas.validar(Accion.Iniciar(6), datos).isEmpty())
        assertTrue(Reglas.validar(Accion.Eliminar(Recurso.TAREA, 6), datos).isEmpty())
    }

    @Test
    fun validaEntregaRevisionYReportes() {
        assertEquals(setOf("foto", "comentario"), Reglas.validar(Accion.Entregar(5, Borrador()), datos).keys)
        assertTrue(Reglas.validar(Accion.Entregar(5, Borrador("Riego hecho", "/foto.jpg")), datos).isEmpty())
        assertEquals("motivo", Reglas.validar(Accion.Revisar(1, Revision(false)), datos).keys.single())
        assertTrue(Reglas.validar(Accion.Revisar(1, Revision(true)), datos).isEmpty())
        assertTrue(Reglas.validar(Accion.Revisar(1, Revision(false, "Repetir foto")), datos).isEmpty())
        assertEquals(setOf("descripción", "sector", "cultivo"),
            Reglas.validar(Accion.NuevoReporte(null, 4, "", null), datos).keys)
        assertTrue(Reglas.validar(Accion.NuevoReporte(1, 2, "Hojas secas", null), datos).isEmpty())
        assertEquals(setOf("seguimiento", "estado"), Reglas.validar(Accion.Atender(8, Seguimiento("OTRO", "")), datos).keys)
        assertTrue(Reglas.validar(Accion.Atender(8, Seguimiento("RESUELTO", "Atendido")), datos).isEmpty())
    }

    @Test
    fun validaRangosDeLecturasSimuladas() {
        assertTrue(Reglas.validar(Accion.Simular(LecturaForm(0.0, -20.0, 100.0)), datos).isEmpty())
        assertEquals(setOf("humedad", "temperatura", "humedad ambiental"),
            Reglas.validar(Accion.Simular(LecturaForm(-1.0, 61.0, 100.1)), datos).keys)
    }

    @Test
    fun alertasOrdenanAtrasoRiegoCosechaYMantenimiento() {
        assertTrue(Reglas.atrasada(tarea(), hoy))
        assertFalse(Reglas.atrasada(tarea(estado = "APROBADA"), hoy))
        assertFalse(Reglas.atrasada(tarea(fecha = "inválida"), hoy))
        val tareas = listOf(tarea(), tarea(7, tipo = "MANTENIMIENTO", fecha = "2026-11-26"), tarea(8, "APROBADA"))
        val alertas = Reglas.alertas(datos.copy(tareas = tareas), hoy)
        assertEquals(listOf("Tarea atrasada", "Riego programado", "Mantenimiento", "Cosecha estimada"),
            alertas.map { it.titulo })
        assertEquals("2026-11-22", alertas[1].fecha)
        assertTrue(Reglas.alertas(Datos(cultivos = listOf(cultivo.copy(estado = "COSECHADO"))), hoy).isEmpty())
    }

    @Test
    fun datosHistoricosIncompletosYFechasFuturas() {
        val cultivos = listOf(
            cultivo.copy(fechaSiembra = null, fechaCosechaEstimada = null),
            cultivo.copy(fechaSiembra = "2026-11-25", frecuenciaRiegoDias = 2, fechaCosechaEstimada = null),
            cultivo.copy(fechaSiembra = "2026-10-10", frecuenciaRiegoDias = 0, fechaCosechaEstimada = null),
        )
        assertEquals("2026-11-27", Reglas.alertas(Datos(cultivos = cultivos), hoy).single().fecha)
        assertEquals(
            "trasplante",
            Reglas.validar(Accion.GuardarCultivo(null, cultivoBase.copy(fechaTrasplante = "2026-12-01",
                fechaCosechaEstimada = "2026-11-25")), datos).keys.single(),
        )
    }

    @Test
    fun historialCombinaEntregasRevisionesYReportesEnOrdenCronologico() {
        val reporte = Reporte(9, "Falta agua", "RESUELTO", 1, "Patio", null, null, "Alumno", "Regado",
            "Docente", null, "2026-11-16T10:00", "2026-11-20T10:00")
        val entrega = evidencia().copy(estado = "APROBADA", revisadoPor = "Docente", fechaRevision = "2026-11-20T09:00")
        val eventos = Reglas.historial(datos.copy(tareas = listOf(tarea()), reportes = listOf(reporte)), listOf(entrega))
        assertEquals(listOf("Seguimiento de reporte", "Revisión · APROBADA", "Evidencia entregada", "Problema reportado",
            "Tarea creada"), eventos.map { it.titulo })
        assertEquals("Docente", eventos[1].descripcion)
    }
}
