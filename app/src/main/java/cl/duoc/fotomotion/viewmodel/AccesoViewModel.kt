package cl.duoc.fotomotion.viewmodel

import androidx.lifecycle.ViewModel
import cl.duoc.fotomotion.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccesoViewModel : ViewModel() {
    val cuentasPrueba = listOf(
        CuentaHuerto("USR-001", "Profesor Carlos Silva", TipoCuenta.DOCENTE),
        CuentaHuerto("USR-002", "Estudiante Mateo Rojas", TipoCuenta.ALUMNO)
    )
    private val cuentaEstado = MutableStateFlow<CuentaHuerto?>(null)
    val cuentaActiva = cuentaEstado.asStateFlow()

    fun entrarConCuenta(cuentaElegida: CuentaHuerto) { cuentaEstado.value = cuentaElegida }
    fun salirDeCuenta() { cuentaEstado.value = null }
}
