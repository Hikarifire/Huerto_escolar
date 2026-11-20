package cl.duoc.fotomotion.data

import android.content.Context
import androidx.room.*
import cl.duoc.fotomotion.domain.Borrador

@Entity(tableName = "borradores", primaryKeys = ["usuario", "tarea"])
data class BorradorLocal(val usuario: Long, val tarea: Long, val comentario: String, val foto: String?) {
    fun toDomain() = Borrador(comentario, foto)
}

@Dao
interface BorradorDao {
    @Query("SELECT * FROM borradores WHERE usuario = :usuario AND tarea = :tarea")
    suspend fun leer(usuario: Long, tarea: Long): BorradorLocal?

    @Upsert suspend fun guardar(dato: BorradorLocal)

    @Query("DELETE FROM borradores WHERE usuario = :usuario AND tarea = :tarea")
    suspend fun eliminar(usuario: Long, tarea: Long)
}

@Database(entities = [BorradorLocal::class], version = 1, exportSchema = true)
abstract class BaseBorradores : RoomDatabase() {
    abstract fun dao(): BorradorDao

    companion object {
        @Volatile private var instancia: BaseBorradores? = null

        fun abrir(contexto: Context): BaseBorradores = instancia ?: synchronized(this) {
            instancia ?: Room.databaseBuilder(contexto.applicationContext, BaseBorradores::class.java,
                "borradores_huerto.db").build().also { instancia = it }
        }
    }
}
