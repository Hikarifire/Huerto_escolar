package cl.duoc.fotomotion.domain

data class Usuario(val id: Long, val nombre: String, val correo: String, val tipoCuenta: String) {
    val esDocente get() = tipoCuenta == "DOCENTE"
}

data class Sesion(val token: String, val usuario: Usuario)
data class Acceso(val correo: String, val password: String, val nombre: String? = null)

data class Sector(val id: Long, val nombre: String, val ubicacion: String?, val descripcion: String?, val activo: Boolean)
data class Cultivo(
    val id: Long, val nombre: String, val tipo: String?, val descripcion: String?,
    val fechaSiembra: String?, val fechaCosechaEstimada: String?, val fechaTrasplante: String?,
    val frecuenciaRiegoDias: Int?, val estado: String, val sectorId: Long, val sectorNombre: String,
)
data class Tarea(
    val id: Long, val titulo: String, val descripcion: String, val fechaLimite: String,
    val estado: String, val tipo: String, val sectorId: Long, val sectorNombre: String,
    val cultivoId: Long?, val cultivoNombre: String?, val estudianteId: Long,
    val estudianteNombre: String, val fechaCreacion: String,
)
data class Evidencia(
    val id: Long, val tareaId: Long, val fotoUrl: String, val comentario: String,
    val estado: String, val observacionRevision: String?, val revisadoPor: String?,
    val fechaRegistro: String, val fechaRevision: String?,
)
data class Reporte(
    val id: Long, val descripcion: String, val estado: String, val sectorId: Long, val sectorNombre: String,
    val cultivoId: Long?, val cultivoNombre: String?, val autor: String, val seguimiento: String?,
    val atendidoPor: String?, val fotoUrl: String?, val creadoEn: String, val actualizadoEn: String?,
)
data class Lectura(
    val id: Long, val humedad: Double, val temperatura: Double, val humedadAmbiental: Double?,
    val sectorNombre: String?, val simulada: Boolean, val fechaRegistro: String,
)
data class Clima(
    val hora: String, val temperatura: Double, val humedad: Int, val precip: Double,
)
data class Datos(
    val sectores: List<Sector> = emptyList(),
    val cultivos: List<Cultivo> = emptyList(),
    val tareas: List<Tarea> = emptyList(),
    val alumnos: List<Usuario> = emptyList(),
    val reportes: List<Reporte> = emptyList(),
    val lecturas: List<Lectura> = emptyList(),
)

data class SectorForm(val nombre: String = "", val ubicacion: String = "",
                      val descripcion: String = "", val activo: Boolean = true)
data class CultivoForm(
    val nombre: String = "", val tipo: String = "", val descripcion: String = "",
    val fechaSiembra: String = "", val fechaCosechaEstimada: String? = null, val fechaTrasplante: String? = null,
    val frecuenciaRiegoDias: Int? = null, val estado: String = "SEMBRADO", val sectorId: Long? = null,
)
data class TareaForm(
    val titulo: String = "", val descripcion: String = "", val fechaLimite: String = "",
    val sectorId: Long? = null, val cultivoId: Long? = null, val estudianteId: Long? = null,
    val tipo: String = "OBSERVACION",
)
data class Revision(val aprobada: Boolean, val observacion: String? = null)
data class Seguimiento(val estado: String = "EN_ATENCION", val seguimiento: String = "")
data class LecturaForm(
    val humedad: Double, val temperatura: Double, val humedadAmbiental: Double, val sectorId: Long? = null,
)
data class Borrador(val comentario: String = "", val foto: String? = null)
data class Evento(val titulo: String, val descripcion: String, val fecha: String)

enum class Recurso(val ruta: String) {
    SECTOR("sectores"), CULTIVO("cultivos"), TAREA("tareas")
}

sealed interface Accion {
    data class GuardarSector(val id: Long?, val datos: SectorForm) : Accion
    data class GuardarCultivo(val id: Long?, val datos: CultivoForm) : Accion
    data class GuardarTarea(val id: Long?, val datos: TareaForm) : Accion
    data class Eliminar(val recurso: Recurso, val id: Long) : Accion
    data class Iniciar(val id: Long) : Accion
    data class Entregar(val id: Long, val borrador: Borrador) : Accion
    data class Revisar(val id: Long, val revision: Revision) : Accion
    data class NuevoReporte(val sectorId: Long?, val cultivoId: Long?, val descripcion: String, val foto: String?) : Accion
    data class Atender(val id: Long, val seguimiento: Seguimiento) : Accion
    data class Simular(val lectura: LecturaForm) : Accion
}

interface Repositorio {
    suspend fun acceder(datos: Acceso): Sesion
    suspend fun perfil(token: String): Usuario
    fun tokenGuardado(): String?
    fun guardarToken(token: String?)
    suspend fun salir(token: String)
    suspend fun consultar(sesion: Sesion): Datos
    suspend fun ejecutar(sesion: Sesion, accion: Accion)
    suspend fun evidencias(sesion: Sesion, tarea: Long): List<Evidencia>
    suspend fun foto(sesion: Sesion, ruta: String): ByteArray
    suspend fun clima(): Clima
    suspend fun borrador(usuario: Long, tarea: Long): Borrador?
    suspend fun guardarBorrador(usuario: Long, tarea: Long, borrador: Borrador)
}

class SesionVencida : IllegalStateException("La sesión expiró. Vuelve a ingresar.")
