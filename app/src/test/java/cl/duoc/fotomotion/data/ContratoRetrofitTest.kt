package cl.duoc.fotomotion.data

import cl.duoc.fotomotion.domain.Acceso
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetSocketAddress

class ContratoRetrofitTest {
    @Test
    fun enviaLoginBearerYCodificaEliminacionSinCuerpo() {
        val servidor = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        servidor.createContext("/api/auth/login") { intercambio ->
            val cuerpo = intercambio.requestBody.bufferedReader().use { it.readText() }
            val correcto = intercambio.requestMethod == "POST" && cuerpo.contains("docente@huerto.cl")
            val json = """
                {"token":"token-demo","usuario":{"id":1,"nombre":"Docente","correo":"docente@huerto.cl","tipoCuenta":"DOCENTE"}}
            """.trimIndent().toByteArray()
            intercambio.responseHeaders.set("Content-Type", "application/json")
            intercambio.sendResponseHeaders(if (correcto) 200 else 400, json.size.toLong())
            intercambio.responseBody.use { it.write(json) }
        }
        servidor.createContext("/api/sectores/8") { intercambio ->
            val correcto = intercambio.requestMethod == "DELETE" &&
                intercambio.requestHeaders.getFirst("Authorization") == "Bearer token-demo"
            intercambio.sendResponseHeaders(if (correcto) 204 else 403, -1)
            intercambio.close()
        }
        servidor.start()
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl("http://127.0.0.1:${servidor.address.port}/")
                .addConverterFactory(GsonConverterFactory.create()).build()
            val api = retrofit.create(HuertoApi::class.java)
            runBlocking {
                val sesion = api.login(Acceso("docente@huerto.cl", "clave-demo"))
                assertTrue(sesion.usuario.esDocente)
                val respuesta = api.eliminar("Bearer ${sesion.token}", "sectores", 8)
                assertTrue(respuesta.isSuccessful)
                assertEquals(204, respuesta.code())
                assertNull(respuesta.body())
            }
        } finally {
            servidor.stop(0)
        }
    }
}
