package cl.duoc.fotomotion.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsultasHuerto {
    @Insert suspend fun agregarParcela(registroParcela: ParcelaHuerto)
    @Update suspend fun modificarParcela(registroParcela: ParcelaHuerto)
    @Delete suspend fun quitarParcela(registroParcela: ParcelaHuerto)
    @Query("SELECT * FROM zonas_huerto ORDER BY id DESC")
    fun observarParcelas(): Flow<List<ParcelaHuerto>>

    @Insert suspend fun agregarSiembra(registroSiembra: SiembraHuerto)
    @Update suspend fun modificarSiembra(registroSiembra: SiembraHuerto)
    @Delete suspend fun quitarSiembra(registroSiembra: SiembraHuerto)
    @Query("SELECT * FROM plantaciones ORDER BY id DESC")
    fun observarSiembras(): Flow<List<SiembraHuerto>>

    @Insert suspend fun agregarLabor(registroLabor: LaborHuerto)
    @Update suspend fun modificarLabor(registroLabor: LaborHuerto)
    @Delete suspend fun quitarLabor(registroLabor: LaborHuerto)
    @Query("SELECT * FROM actividades_huerto ORDER BY id DESC")
    fun observarLabores(): Flow<List<LaborHuerto>>

    @Query("SELECT COUNT(*) FROM plantaciones WHERE sectorId = :codigoBuscado")
    suspend fun contarSiembrasDeParcela(codigoBuscado: Int): Int
    @Query("SELECT COUNT(*) FROM actividades_huerto WHERE sectorId = :codigoBuscado")
    suspend fun contarLaboresDeParcela(codigoBuscado: Int): Int
    @Query("SELECT COUNT(*) FROM actividades_huerto WHERE cultivoId = :codigoBuscado")
    suspend fun contarLaboresDeSiembra(codigoBuscado: Int): Int
}
