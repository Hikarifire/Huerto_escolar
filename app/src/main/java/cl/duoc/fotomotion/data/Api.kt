package cl.duoc.fotomotion.data

import cl.duoc.fotomotion.domain.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

private const val AUTH = "Authorization"

interface HuertoApi {
    @POST("api/usuarios/cuentas/{rol}")
    suspend fun crearCuenta(@Header(AUTH) token: String, @Path("rol") rol: String,
                            @Body datos: Acceso): Usuario
    @POST("api/auth/login") suspend fun login(@Body datos: Acceso): Sesion
    @POST("api/auth/registro") suspend fun registro(@Body datos: Acceso): Sesion
    @GET("api/auth/me") suspend fun perfil(@Header(AUTH) token: String): Usuario
    @POST("api/auth/logout") suspend fun logout(@Header(AUTH) token: String): Response<Void>

    @GET("api/sectores") suspend fun sectores(@Header(AUTH) token: String): List<Sector>
    @GET("api/cultivos") suspend fun cultivos(@Header(AUTH) token: String): List<Cultivo>
    @GET("api/tareas") suspend fun tareas(@Header(AUTH) token: String): List<Tarea>
    @GET("api/usuarios/estudiantes") suspend fun estudiantes(@Header(AUTH) token: String): List<Usuario>
    @GET("api/reportes") suspend fun reportes(@Header(AUTH) token: String): List<Reporte>
    @GET("api/lecturas") suspend fun lecturas(@Header(AUTH) token: String): List<Lectura>
    @GET("api/tareas/{id}/evidencias")
    suspend fun evidencias(@Header(AUTH) token: String, @Path("id") id: Long): List<Evidencia>

    @POST("api/sectores") suspend fun crearSector(@Header(AUTH) token: String, @Body datos: SectorForm): Sector
    @PUT("api/sectores/{id}")
    suspend fun actualizarSector(@Header(AUTH) token: String, @Path("id") id: Long, @Body datos: SectorForm): Sector
    @POST("api/cultivos") suspend fun crearCultivo(@Header(AUTH) token: String, @Body datos: CultivoForm): Cultivo
    @PUT("api/cultivos/{id}")
    suspend fun actualizarCultivo(@Header(AUTH) token: String, @Path("id") id: Long, @Body datos: CultivoForm): Cultivo
    @POST("api/tareas") suspend fun crearTarea(@Header(AUTH) token: String, @Body datos: TareaForm): Tarea
    @PUT("api/tareas/{id}")
    suspend fun actualizarTarea(@Header(AUTH) token: String, @Path("id") id: Long, @Body datos: TareaForm): Tarea
    @DELETE("api/{recurso}/{id}")
    suspend fun eliminar(@Header(AUTH) token: String, @Path("recurso") recurso: String, @Path("id") id: Long): Response<Void>

    @POST("api/tareas/{id}/iniciar")
    suspend fun iniciar(@Header(AUTH) token: String, @Path("id") id: Long): Tarea
    @Multipart @POST("api/tareas/{id}/evidencias")
    suspend fun entregar(@Header(AUTH) token: String, @Path("id") id: Long,
                         @Part foto: MultipartBody.Part, @Part("comentario") comentario: RequestBody): Evidencia
    @POST("api/evidencias/{id}/revision")
    suspend fun revisar(@Header(AUTH) token: String, @Path("id") id: Long, @Body datos: Revision): Evidencia
    @Multipart @POST("api/reportes")
    suspend fun crearReporte(@Header(AUTH) token: String, @Part("sectorId") sector: RequestBody,
                             @Part("cultivoId") cultivo: RequestBody?, @Part("descripcion") descripcion: RequestBody,
                             @Part foto: MultipartBody.Part?): Reporte
    @PATCH("api/reportes/{id}")
    suspend fun atender(@Header(AUTH) token: String, @Path("id") id: Long, @Body datos: Seguimiento): Reporte
    @POST("api/lecturas/simuladas")
    suspend fun simular(@Header(AUTH) token: String, @Body datos: LecturaForm): Lectura
    @GET suspend fun foto(@Header(AUTH) token: String, @Url ruta: String): ResponseBody
}

data class Pronostico(val current: Actual)
data class Actual(
    val time: String,
    @SerializedName("temperature_2m") val temperatura: Double,
    @SerializedName("relative_humidity_2m") val humedad: Int,
    val precipitation: Double,
)

interface ClimaApi {
    @GET("v1/forecast")
    suspend fun pronostico(
        @Query("latitude") latitud: Double = -33.595,
        @Query("longitude") longitud: Double = -70.699,
        @Query("current") campos: String = "temperature_2m,relative_humidity_2m,precipitation",
        @Query("timezone") zona: String = "America/Santiago",
    ): Pronostico
}
