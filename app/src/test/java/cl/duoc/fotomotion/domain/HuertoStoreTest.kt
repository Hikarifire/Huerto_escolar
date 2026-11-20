package cl.duoc.fotomotion.domain

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class HuertoStoreTest {
    private val repo = FakeRepositorio()
    private val store = HuertoStore(repo)
    private val acceso = Acceso("  docente@huerto.cl ", "contraseña")
    private val estado get() = store.estado.value

    @Test
    fun validaFormularioSinInvocarApi() = runBlocking {
        store.acceder(Acceso("docente", ""))
        assertNull(estado.sesion)
        assertEquals(setOf("correo", "contraseña"), estado.campos.keys)
        assertNull(repo.ultimoAcceso)
        assertFalse(store.ejecutar(Accion.GuardarSector(null, SectorForm())))
        assertEquals(setOf("nombre", "ubicación"), estado.campos.keys)
        assertTrue(repo.acciones.isEmpty())
        store.limpiarMensajes()
        assertTrue(estado.campos.isEmpty())
    }

    @Test
    fun cargaSesionYRefrescaDespuesDeGuardarEnServidor() = runBlocking {
        store.acceder(acceso)
        assertEquals("docente@huerto.cl", repo.ultimoAcceso?.correo)
        assertEquals("demo", repo.tokenGuardado())
        assertEquals(repo.usuario, estado.sesion?.usuario)
        assertTrue(estado.datos.sectores.isEmpty())

        val accion = Accion.GuardarSector(null, SectorForm("Patio", "Norte"))
        assertTrue(store.ejecutar(accion))
        assertEquals(listOf(accion), repo.acciones)
        assertEquals("Patio", estado.datos.sectores.single().nombre)
        assertNotNull(estado.aviso)
        assertFalse(estado.ocupado)

        repo.datos = Datos()
        store.actualizar()
        assertTrue(estado.datos.sectores.isEmpty())
        store.salir()
        assertEquals("demo", repo.tokenRevocado)
        assertNull(estado.sesion)
        assertNull(repo.tokenGuardado())
    }

    @Test
    fun restauraPerfilYExpiracionDeSesion() = runBlocking {
        store.restaurar()
        assertNull(estado.sesion)
        repo.guardarToken("anterior")
        store.restaurar()
        assertEquals("anterior", estado.sesion?.token)
        repo.falloConsulta = SesionVencida()
        store.actualizar()
        assertNull(estado.sesion)
        assertNull(repo.tokenGuardado())
        assertNotNull(estado.error)
        assertFalse(estado.ocupado)
    }

    @Test
    fun errorDeEscrituraConservaDatosYExitoDeEscrituraNoSeReintentaPorFalloDeConsulta() = runBlocking {
        store.acceder(acceso)
        val sector = Accion.GuardarSector(null, SectorForm("Patio", "Norte"))
        repo.falloEscritura = IllegalStateException("Servidor no disponible")
        assertFalse(store.ejecutar(sector))
        assertEquals("Servidor no disponible", estado.error)
        assertTrue(estado.datos.sectores.isEmpty())
        repo.falloEscritura = null
        repo.falloConsulta = IllegalStateException("Actualización pendiente")
        assertTrue(store.ejecutar(sector))
        assertEquals("Actualización pendiente", estado.error)
        assertEquals(1, repo.acciones.size)
        repo.falloConsulta = null
        store.actualizar()
        assertEquals(1, estado.datos.sectores.size)
    }

    @Test
    fun borradoresPorUsuarioYEntregaActualizanEvidencias() = runBlocking {
        assertEquals(Borrador(), store.borrador(6))
        assertFalse(store.guardarBorrador(6, Borrador("texto")))
        assertNull(store.foto("/foto"))
        assertFalse(store.ejecutar(Accion.Iniciar(6)))
        store.evidencias(6)
        store.historial()

        repo.datos = Datos(tareas = listOf(tarea(6, "EN_DESARROLLO")))
        store.acceder(acceso)
        assertEquals(Borrador(), store.borrador(6))
        val borrador = Borrador("Primer riego", "/fotos/huerto.png")
        assertTrue(store.guardarBorrador(6, borrador))
        assertEquals(borrador, store.borrador(6))
        assertEquals(5L to 6L, repo.borradores.keys.single())
        assertTrue(store.ejecutar(Accion.Entregar(6, borrador)))
        assertEquals("EN_REVISION", estado.evidencia[6]?.single()?.estado)
        assertArrayEquals(byteArrayOf(8), store.foto("/foto"))
        assertTrue(store.ejecutar(Accion.Revisar(42, Revision(false, "Otra foto"))))
        store.historial()
        assertEquals("RECHAZADA", estado.evidencia[6]?.single()?.estado)
        assertEquals(3, repo.consultasEvidencias)
    }

    @Test
    fun climaTieneFalloIndependienteYRecuperacion() = runBlocking {
        store.acceder(acceso)
        repo.falloClima = IllegalStateException("Sin pronóstico")
        store.clima()
        assertNull(estado.clima)
        assertEquals("Sin pronóstico", estado.errorClima)
        assertNotNull(estado.sesion)
        assertFalse(estado.cargandoClima)
        repo.falloClima = null
        store.clima()
        assertEquals(19.5, estado.clima?.temperatura)
        assertNull(estado.errorClima)
    }

    @Test
    fun cancelacionSePropagaYLiberaIndicador() = runBlocking {
        store.acceder(acceso)
        repo.falloConsulta = CancellationException()
        try {
            store.actualizar()
            fail("La cancelación debe propagarse")
        } catch (_: CancellationException) {
            assertFalse(estado.ocupado)
        }
    }

    private class FakeRepositorio : Repositorio {
        val usuario = Usuario(5, "Docente", "docente@huerto.cl", "DOCENTE")
        var datos = Datos()
        var ultimoAcceso: Acceso? = null
        var tokenRevocado: String? = null
        private var token: String? = null
        val acciones = mutableListOf<Accion>()
        val borradores = mutableMapOf<Pair<Long, Long>, Borrador>()
        var consultasEvidencias = 0
        private var entregas = listOf<Evidencia>()
        var falloConsulta: Exception? = null
        var falloEscritura: Exception? = null
        var falloClima: Exception? = null

        override suspend fun acceder(datos: Acceso): Sesion {
            ultimoAcceso = datos
            return Sesion("demo", usuario)
        }
        override suspend fun perfil(token: String) = usuario
        override fun tokenGuardado() = token
        override fun guardarToken(token: String?) { this.token = token }
        override suspend fun salir(token: String) { tokenRevocado = token }
        override suspend fun consultar(sesion: Sesion): Datos {
            falloConsulta?.let { throw it }
            return datos
        }
        override suspend fun ejecutar(sesion: Sesion, accion: Accion) {
            falloEscritura?.let { throw it }
            acciones += accion
            when (accion) {
                is Accion.GuardarSector -> datos = datos.copy(sectores = listOf(
                    Sector(1, accion.datos.nombre, accion.datos.ubicacion, null, true)))
                is Accion.Entregar -> entregas = listOf(evidencia().copy(tareaId = accion.id))
                is Accion.Revisar -> entregas = entregas.map { it.copy(estado = "RECHAZADA") }
                else -> Unit
            }
        }
        override suspend fun evidencias(sesion: Sesion, tarea: Long): List<Evidencia> {
            consultasEvidencias++
            return entregas
        }
        override suspend fun foto(sesion: Sesion, ruta: String) = byteArrayOf(8)
        override suspend fun clima(): Clima {
            falloClima?.let { throw it }
            return Clima("2026-11-20T10:00", 19.5, 53, 0.0)
        }
        override suspend fun borrador(usuario: Long, tarea: Long) = borradores[usuario to tarea]
        override suspend fun guardarBorrador(usuario: Long, tarea: Long, borrador: Borrador) {
            borradores[usuario to tarea] = borrador
        }
    }
}
