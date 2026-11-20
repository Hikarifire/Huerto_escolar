package cl.duoc.fotomotion.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class HuertoUiState(
    val sesion: Sesion? = null,
    val datos: Datos = Datos(),
    val ocupado: Boolean = false,
    val error: String? = null,
    val aviso: String? = null,
    val campos: Map<String, String> = emptyMap(),
    val evidencia: Map<Long, List<Evidencia>> = emptyMap(),
    val clima: Clima? = null,
    val cargandoClima: Boolean = false,
    val errorClima: String? = null,
)

class HuertoStore(private val repo: Repositorio) {
    private val mutable = MutableStateFlow(HuertoUiState())
    val estado = mutable.asStateFlow()
    private val operaciones = Mutex()

    suspend fun restaurar() {
        val token = repo.tokenGuardado() ?: return
        operar {
            val sesion = Sesion(token, repo.perfil(token))
            mutable.update { it.copy(sesion = sesion) }
            recargar(sesion)
        }
    }

    suspend fun acceder(datos: Acceso) {
        val campos = Reglas.acceso(datos)
        mutable.update { it.copy(campos = campos, error = null, aviso = null) }
        if (campos.isNotEmpty()) return
        operar {
            val sesion = repo.acceder(datos.copy(correo = datos.correo.trim(), nombre = datos.nombre?.trim()))
            repo.guardarToken(sesion.token)
            mutable.update { it.copy(sesion = sesion, datos = Datos(), evidencia = emptyMap()) }
            recargar(sesion)
        }
    }

    suspend fun salir() {
        operar {
            estado.value.sesion?.let { repo.salir(it.token) }
            repo.guardarToken(null)
            mutable.value = HuertoUiState()
        }
    }

    suspend fun actualizar() {
        operar { estado.value.sesion?.let { recargar(it) } }
    }

    suspend fun ejecutar(accion: Accion): Boolean {
        val campos = Reglas.validar(accion, estado.value.datos)
        mutable.update { it.copy(campos = campos, error = null, aviso = null) }
        if (campos.isNotEmpty()) return false
        var guardado = false
        operar {
            val sesion = estado.value.sesion ?: return@operar
            repo.ejecutar(sesion, accion)
            guardado = true
            mutable.update { it.copy(aviso = "Operación guardada en el servidor.") }
            recargar(sesion)
            when (accion) {
                is Accion.Entregar -> cargarEvidencias(sesion, accion.id)
                is Accion.Revisar -> estado.value.evidencia.entries
                    .firstOrNull { (_, lista) -> lista.any { it.id == accion.id } }
                    ?.let { cargarEvidencias(sesion, it.key) }
                else -> Unit
            }
        }
        return guardado
    }

    suspend fun evidencias(tarea: Long) {
        operar { estado.value.sesion?.let { cargarEvidencias(it, tarea) } }
    }

    suspend fun historial() {
        operar {
            val sesion = estado.value.sesion ?: return@operar
            estado.value.datos.tareas.forEach { cargarEvidencias(sesion, it.id) }
        }
    }

    suspend fun clima() {
        if (estado.value.cargandoClima) return
        mutable.update { it.copy(cargandoClima = true, errorClima = null) }
        try {
            val clima = repo.clima()
            mutable.update { it.copy(clima = clima) }
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            mutable.update { it.copy(errorClima = ex.message ?: "No se pudo consultar el clima.") }
        } finally {
            mutable.update { it.copy(cargandoClima = false) }
        }
    }

    suspend fun foto(ruta: String): ByteArray? = estado.value.sesion?.let { repo.foto(it, ruta) }

    suspend fun borrador(tarea: Long): Borrador =
        estado.value.sesion?.usuario?.let { repo.borrador(it.id, tarea) } ?: Borrador()

    suspend fun guardarBorrador(tarea: Long, borrador: Borrador): Boolean {
        var escrito = false
        operar {
            val usuario = estado.value.sesion?.usuario ?: return@operar
            repo.guardarBorrador(usuario.id, tarea, borrador)
            escrito = true
            mutable.update { it.copy(aviso = "Borrador guardado en el dispositivo.") }
        }
        return escrito
    }

    fun limpiarMensajes() = mutable.update { it.copy(campos = emptyMap(), error = null, aviso = null) }

    private suspend fun recargar(sesion: Sesion) {
        val datos = repo.consultar(sesion)
        mutable.update { it.copy(datos = datos) }
    }

    private suspend fun cargarEvidencias(sesion: Sesion, tarea: Long) {
        val entregas = repo.evidencias(sesion, tarea)
        mutable.update { it.copy(evidencia = it.evidencia + (tarea to entregas)) }
    }

    private suspend fun operar(operacion: suspend () -> Unit) = operaciones.withLock {
        mutable.update { it.copy(ocupado = true, error = null) }
        try {
            operacion()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: SesionVencida) {
            repo.guardarToken(null)
            mutable.value = HuertoUiState(error = ex.message)
        } catch (ex: Exception) {
            mutable.update { it.copy(error = ex.message ?: "No se pudo completar la operación.") }
        } finally {
            mutable.update { it.copy(ocupado = false) }
        }
    }
}
