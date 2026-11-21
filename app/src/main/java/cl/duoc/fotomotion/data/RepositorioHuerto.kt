package cl.duoc.fotomotion.data

import android.content.Context
import cl.duoc.fotomotion.BuildConfig
import cl.duoc.fotomotion.domain.*
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.Response
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.IOException

class RepositorioHuerto(contexto: Context) : Repositorio {
    private val api = conectar(BuildConfig.HUERTO_URL).create(HuertoApi::class.java)
    private val climaApi = conectar("https://api.open-meteo.com/").create(ClimaApi::class.java)
    private val preferencias = contexto.getSharedPreferences("sesion_huerto", Context.MODE_PRIVATE)
    private val borradores = BaseBorradores.abrir(contexto).dao()
    private val carpetaFotos = File(contexto.filesDir, "fotos").canonicalFile

    override suspend fun acceder(datos: Acceso) = remoto(sesionActiva = false) {
        if (datos.nombre == null) api.login(datos) else api.registro(datos)
    }

    override suspend fun perfil(token: String) = remoto { api.perfil(bearer(token)) }
    override fun tokenGuardado() = preferencias.getString("token", null)
    override fun guardarToken(token: String?) = preferencias.edit().putString("token", token).apply()
    override suspend fun salir(token: String) = remoto { comprobarRespuesta(api.logout(bearer(token))) }

    override suspend fun consultar(sesion: Sesion): Datos = remoto {
        val token = bearer(sesion.token)
        coroutineScope {
            val sectores = async { api.sectores(token) }
            val cultivos = async { api.cultivos(token) }
            val tareas = async { api.tareas(token) }
            val reportes = async { api.reportes(token) }
            val lecturas = async { api.lecturas(token) }
            val estudiantes = async { if (sesion.usuario.puedeGestionarHuerto) api.estudiantes(token) else emptyList() }
            Datos(sectores.await(), cultivos.await(), tareas.await(), estudiantes.await(), reportes.await(), lecturas.await())
        }
    }

    override suspend fun ejecutar(sesion: Sesion, accion: Accion) {
        val token = bearer(sesion.token)
        remoto {
            when (accion) {
                is Accion.CrearCuenta -> api.crearCuenta(token, accion.rol.name, accion.datos)
                is Accion.GuardarSector -> if (accion.id == null) api.crearSector(token, accion.datos)
                    else api.actualizarSector(token, accion.id, accion.datos)
                is Accion.GuardarCultivo -> if (accion.id == null) api.crearCultivo(token, accion.datos)
                    else api.actualizarCultivo(token, accion.id, accion.datos)
                is Accion.GuardarTarea -> if (accion.id == null) api.crearTarea(token, accion.datos)
                    else api.actualizarTarea(token, accion.id, accion.datos)
                is Accion.Eliminar -> comprobarRespuesta(api.eliminar(token, accion.recurso.ruta, accion.id))
                is Accion.Iniciar -> api.iniciar(token, accion.id)
                is Accion.Entregar -> api.entregar(token, accion.id,
                    parteFoto(requireNotNull(accion.borrador.foto)), texto(accion.borrador.comentario.trim()))
                is Accion.Revisar -> api.revisar(token, accion.id, accion.revision)
                is Accion.NuevoReporte -> api.crearReporte(token, texto(requireNotNull(accion.sectorId).toString()),
                    accion.cultivoId?.let { texto(it.toString()) }, texto(accion.descripcion.trim()), accion.foto?.let(::parteFoto))
                is Accion.Atender -> api.atender(token, accion.id, accion.seguimiento)
                is Accion.Simular -> api.simular(token, accion.lectura)
            }
        }
        if (accion is Accion.Entregar) runCatching { borradores.eliminar(sesion.usuario.id, accion.id) }
    }

    override suspend fun evidencias(sesion: Sesion, tarea: Long) =
        remoto { api.evidencias(bearer(sesion.token), tarea) }

    override suspend fun foto(sesion: Sesion, ruta: String): ByteArray {
        require(ruta.matches(Regex("/api/(evidencias|reportes)/[0-9]+/foto")))
        return withContext(Dispatchers.IO) {
            remoto { api.foto(bearer(sesion.token), ruta).use { it.bytes() } }
        }
    }

    override suspend fun clima(): Clima = remoto(sesionActiva = false) {
        val actual = climaApi.pronostico().current
        Clima(actual.time, actual.temperatura, actual.humedad, actual.precipitation)
    }

    override suspend fun borrador(usuario: Long, tarea: Long) = borradores.leer(usuario, tarea)?.toDomain()
    override suspend fun guardarBorrador(usuario: Long, tarea: Long, borrador: Borrador) {
        borradores.guardar(BorradorLocal(usuario, tarea, borrador.comentario, borrador.foto))
    }

    private fun parteFoto(ruta: String): MultipartBody.Part {
        val archivo = File(ruta).canonicalFile
        require(archivo.parentFile == carpetaFotos && archivo.isFile) { "Vuelve a seleccionar la fotografía." }
        require(archivo.length() in 1..5_242_880) { "La fotografía debe pesar hasta 5 MB." }
        return MultipartBody.Part.createFormData("foto", archivo.name,
            RequestBody.create(MediaType.parse("application/octet-stream"), archivo))
    }

    private fun comprobarRespuesta(respuesta: Response<Void>) {
        if (!respuesta.isSuccessful) throw HttpException(respuesta)
    }

    private fun conectar(url: String): Retrofit =
        Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build()
    private fun bearer(token: String) = "Bearer $token"
    private fun texto(valor: String) = RequestBody.create(MediaType.parse("text/plain"), valor)

    private suspend fun <T> remoto(sesionActiva: Boolean = true, operacion: suspend () -> T): T {
        try {
            return operacion()
        } catch (ex: HttpException) {
            if (sesionActiva && ex.code() == 401) throw SesionVencida()
            val detalle = runCatching {
                Gson().fromJson(ex.response()?.errorBody()?.string(), MensajeApi::class.java)?.mensaje
            }.getOrNull()
            val mensaje = detalle ?: when (ex.code()) {
                401 -> "Correo o contraseña incorrectos."
                403 -> "Tu cuenta no puede realizar esta operación."
                413 -> "La fotografía supera el tamaño permitido."
                else -> "El servicio respondió con el código ${ex.code()}."
            }
            throw IllegalStateException(mensaje, ex)
        } catch (ex: IOException) {
            throw IllegalStateException("No se pudo conectar con el servicio. Revisa tu conexión.", ex)
        }
    }

    private data class MensajeApi(val mensaje: String?)
}
