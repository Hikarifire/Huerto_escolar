package cl.duoc.fotomotion.ui.viewmodel

import cl.duoc.fotomotion.domain.model.CuentaHuerto
import cl.duoc.fotomotion.domain.model.TipoCuenta

enum class ModoAcceso { LOGIN, REGISTRO }

data class EstadoAcceso(
    val modoAcceso: ModoAcceso = ModoAcceso.LOGIN,
    val cuentasDisponibles: List<CuentaHuerto> = emptyList(),
    val cuentaActiva: CuentaHuerto? = null,
    val usuarioBusqueda: String = "",
    val errorLogin: String = "",
    val nombreRegistro: String = "",
    val tipoCuentaRegistro: TipoCuenta = TipoCuenta.ALUMNO,
    val errorRegistroNombre: String = "",
    val nuevoNombreAlumno: String = "",
    val errorNombreAlumno: String = "",
)
