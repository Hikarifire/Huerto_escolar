package cl.duoc.fotomotion.repository

import cl.duoc.fotomotion.model.*

class RepositorioHuerto(private val consultasGuardadas: ConsultasHuerto) {
    fun leerParcelas() = consultasGuardadas.observarParcelas()
    suspend fun registrarParcela(datoParcela: ParcelaHuerto) = consultasGuardadas.agregarParcela(datoParcela)
    suspend fun corregirParcela(datoParcela: ParcelaHuerto) = consultasGuardadas.modificarParcela(datoParcela)
    suspend fun borrarParcela(datoParcela: ParcelaHuerto): Boolean {
        if (consultasGuardadas.contarSiembrasDeParcela(datoParcela.codigoParcela) > 0 ||
            consultasGuardadas.contarLaboresDeParcela(datoParcela.codigoParcela) > 0) return false
        consultasGuardadas.quitarParcela(datoParcela)
        return true
    }

    fun leerSiembras() = consultasGuardadas.observarSiembras()
    suspend fun registrarSiembra(datoSiembra: SiembraHuerto) = consultasGuardadas.agregarSiembra(datoSiembra)
    suspend fun corregirSiembra(datoSiembra: SiembraHuerto) = consultasGuardadas.modificarSiembra(datoSiembra)
    suspend fun borrarSiembra(datoSiembra: SiembraHuerto): Boolean {
        if (consultasGuardadas.contarLaboresDeSiembra(datoSiembra.codigoSiembra) > 0) return false
        consultasGuardadas.quitarSiembra(datoSiembra)
        return true
    }

    fun leerLabores() = consultasGuardadas.observarLabores()
    suspend fun registrarLabor(datoLabor: LaborHuerto) = consultasGuardadas.agregarLabor(datoLabor)
    suspend fun corregirLabor(datoLabor: LaborHuerto) = consultasGuardadas.modificarLabor(datoLabor)
    suspend fun borrarLabor(datoLabor: LaborHuerto) = consultasGuardadas.quitarLabor(datoLabor)
}
