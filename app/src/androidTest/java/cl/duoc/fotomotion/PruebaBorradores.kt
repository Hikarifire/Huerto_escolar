package cl.duoc.fotomotion

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import cl.duoc.fotomotion.data.BaseBorradores
import cl.duoc.fotomotion.data.BorradorLocal
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PruebaBorradores {
    @Test
    fun conservaBorradorTrasReabrirRoomYSeparaUsuarios() = runBlocking {
        val contexto = ApplicationProvider.getApplicationContext<Context>()
        val nombre = "prueba_borradores_${System.nanoTime()}.db"
        val dato = BorradorLocal(7, 42, "Riego listo", "/foto.png")
        try {
            val primera = Room.databaseBuilder(contexto, BaseBorradores::class.java, nombre).build()
            try {
                primera.dao().guardar(dato)
            } finally {
                primera.close()
            }
            val reabierta = Room.databaseBuilder(contexto, BaseBorradores::class.java, nombre).build()
            try {
                assertEquals(dato, reabierta.dao().leer(7, 42))
                assertNull(reabierta.dao().leer(9, 42))
                reabierta.dao().eliminar(7, 42)
                assertNull(reabierta.dao().leer(7, 42))
            } finally {
                reabierta.close()
            }
        } finally {
            contexto.deleteDatabase(nombre)
        }
        Unit
    }
}
