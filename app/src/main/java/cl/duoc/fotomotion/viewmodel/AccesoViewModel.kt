package cl.duoc.fotomotion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import cl.duoc.fotomotion.data.local.GestorUsuariosLocal
import cl.duoc.fotomotion.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AccesoViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val gestorUsuarios = GestorUsuariosLocal(aplicacion)
    private val estadoMutable = MutableStateFlow(EstadoAcceso())
    val estadoAcceso: StateFlow<EstadoAcceso> = estadoMutable.asStateFlow()

    val cuentasDisponibles: List<CuentaHuerto>
        get() = estadoMutable.value.cuentasDisponibles

    val cuentasPrueba: List<CuentaHuerto>
        get() = estadoMutable.value.cuentasDisponibles

    private val cuentaEstado = MutableStateFlow<CuentaHuerto?>(null)
    val cuentaActiva: StateFlow<CuentaHuerto?> = cuentaEstado.asStateFlow()

    init {
        val usuariosGuardados = gestorUsuarios.cargarUsuarios()
        estadoMutable.value = estadoMutable.value.copy(cuentasDisponibles = usuariosGuardados)
    }

    fun cambiarModoAcceso(modo: ModoAcceso) {
        estadoMutable.value = estadoMutable.value.copy(
            modoAcceso = modo,
            errorLogin = "",
            errorRegistroNombre = "",
        )
    }

    fun escribirUsuarioBusqueda(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(
            usuarioBusqueda = textoNuevo,
            errorLogin = "",
        )
    }

    fun iniciarSesionPorBusqueda(): CuentaHuerto? {
        val busqueda = estadoMutable.value.usuarioBusqueda.trim()
        if (busqueda.isBlank()) {
            estadoMutable.value = estadoMutable.value.copy(errorLogin = "Ingresa tu nombre o código de usuario.")
            return null
        }

        val coincidencia = estadoMutable.value.cuentasDisponibles.find { cuenta ->
            cuenta.codigoCuenta.equals(busqueda, ignoreCase = true) ||
                    cuenta.nombreCuenta.contains(busqueda, ignoreCase = true)
        }

        return if (coincidencia != null) {
            entrarConCuenta(coincidencia)
            coincidencia
        } else {
            estadoMutable.value = estadoMutable.value.copy(
                errorLogin = "No se encontró ninguna cuenta registrada con '$busqueda'.",
            )
            null
        }
    }

    fun escribirNombreRegistro(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(
            nombreRegistro = textoNuevo,
            errorRegistroNombre = "",
        )
    }

    fun cambiarTipoCuentaRegistro(tipoNuevo: TipoCuenta) {
        estadoMutable.value = estadoMutable.value.copy(tipoCuentaRegistro = tipoNuevo)
    }

    fun registrarNuevoUsuario(): CuentaHuerto? {
        val formulario = estadoMutable.value
        val (esValido, error) = ValidadorCuenta.validarNombre(formulario.nombreRegistro, formulario.tipoCuentaRegistro)
        if (!esValido) {
            estadoMutable.value = formulario.copy(errorRegistroNombre = error)
            return null
        }

        val nombreFormateado = ValidadorCuenta.formatearNombre(formulario.nombreRegistro, formulario.tipoCuentaRegistro)
        val prefijoId = if (formulario.tipoCuentaRegistro == TipoCuenta.DOCENTE) "DOC" else "USR"
        val nuevaCuenta = CuentaHuerto(
            codigoCuenta = "$prefijoId-${System.currentTimeMillis() % 10000}",
            nombreCuenta = nombreFormateado,
            tipoCuenta = formulario.tipoCuentaRegistro,
        )

        val listaActualizada = gestorUsuarios.agregarUsuario(nuevaCuenta)
        cuentaEstado.value = nuevaCuenta
        estadoMutable.value = formulario.copy(
            cuentasDisponibles = listaActualizada,
            cuentaActiva = nuevaCuenta,
            nombreRegistro = "",
            errorRegistroNombre = "",
        )
        return nuevaCuenta
    }

    fun escribirNuevoNombreAlumno(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(
            nuevoNombreAlumno = textoNuevo,
            errorNombreAlumno = "",
        )
    }

    fun entrarConCuenta(cuentaElegida: CuentaHuerto) {
        cuentaEstado.value = cuentaElegida
        estadoMutable.value = estadoMutable.value.copy(cuentaActiva = cuentaElegida)
    }

    fun registrarEIngresarNuevoAlumno(): CuentaHuerto? {
        estadoMutable.value = estadoMutable.value.copy(
            nombreRegistro = estadoMutable.value.nuevoNombreAlumno,
            tipoCuentaRegistro = TipoCuenta.ALUMNO,
        )
        return registrarNuevoUsuario()
    }

    fun salirDeCuenta() {
        cuentaEstado.value = null
        estadoMutable.value = estadoMutable.value.copy(cuentaActiva = null)
    }
}
