package cl.duoc.fotomotion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.fotomotion.data.local.BaseHuerto
import cl.duoc.fotomotion.data.repository.RepositorioHuerto
import cl.duoc.fotomotion.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class LaboresViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repositorio = RepositorioHuerto(BaseHuerto.abrirBase(aplicacion).consultasHuerto())
    private val validadorLabores = ValidadorLabores()
    private val estadoMutable = MutableStateFlow(EstadoLabores())
    val estadoLabores = estadoMutable.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.leerLabores().collect { laboresRecibidas ->
                estadoMutable.value = estadoMutable.value.copy(listaLabores = laboresRecibidas)
            }
        }
        viewModelScope.launch {
            repositorio.leerParcelas().collect { parcelasRecibidas ->
                estadoMutable.value = estadoMutable.value.copy(sectoresDisponibles = parcelasRecibidas)
            }
        }
    }

    fun actualizarEstudiantesDisponibles(estudiantes: List<CuentaHuerto>) {
        estadoMutable.value = estadoMutable.value.copy(estudiantesDisponibles = estudiantes)
    }

    fun elegirEstudianteEncargado(codigoCuenta: String) {
        estadoMutable.value = estadoMutable.value.copy(estudianteAsignado = codigoCuenta, errorEstudiante = "")
    }

    fun escribirDetalle(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(textoDetalle = textoNuevo, errorDetalle = "", avisoLabores = "")
    }

    fun escribirFecha(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(textoFecha = textoNuevo, errorFecha = "")
    }

    fun elegirSectorTrabajo(codigoElegido: Int) {
        estadoMutable.value = estadoMutable.value.copy(sectorTrabajo = codigoElegido, errorSector = "")
    }

    fun revisarLabor(): Boolean {
        val formulario = estadoMutable.value
        val resultado = validadorLabores.validarLabor(
            textoDetalle = formulario.textoDetalle,
            textoFecha = formulario.textoFecha,
            sectorTrabajo = formulario.sectorTrabajo,
            sectoresDisponibles = formulario.sectoresDisponibles,
            estudianteAsignado = formulario.estudianteAsignado
        )
        estadoMutable.value = formulario.copy(
            errorDetalle = resultado.errorDetalle,
            errorFecha = resultado.errorFecha,
            errorSector = resultado.errorSector,
            errorEstudiante = resultado.errorEstudiante,
        )
        return resultado.esValido
    }

    fun confirmarLabor(cuentaResponsable: CuentaHuerto) {
        if (cuentaResponsable.tipoCuenta != TipoCuenta.DOCENTE || estadoMutable.value.guardandoLabor || !revisarLabor()) return
        val formulario = estadoMutable.value
        val codigoSector = formulario.sectorTrabajo ?: return
        val laborAnterior = formulario.laborEnEdicion
        val encargado = formulario.estudianteAsignado.ifBlank { "USR-002" }
        val registroParaGuardar = (laborAnterior ?: LaborHuerto(
            claseLabor = "CUIDADO",
            parcelaTrabajo = codigoSector,
            diaCreacion = LocalDate.now().toString(),
            autorLabor = cuentaResponsable.codigoCuenta,
            encargadoLabor = encargado,
            situacionLabor = "PENDIENTE",
        )).copy(
            detalleLabor = formulario.textoDetalle.trim(),
            diaPlanificado = formulario.textoFecha,
            parcelaTrabajo = codigoSector,
            encargadoLabor = encargado,
        )
        estadoMutable.value = formulario.copy(guardandoLabor = true)
        viewModelScope.launch {
            try {
                if (laborAnterior == null) repositorio.registrarLabor(registroParaGuardar)
                else repositorio.corregirLabor(registroParaGuardar)
                limpiarLabor()
                informarLabor("Tarea guardada.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                informarLabor("No se pudo guardar la tarea.")
            } finally { estadoMutable.value = estadoMutable.value.copy(guardandoLabor = false) }
        }
    }

    fun prepararLabor(registroElegido: LaborHuerto) {
        estadoMutable.value = estadoMutable.value.copy(
            laborEnEdicion = registroElegido,
            textoDetalle = registroElegido.detalleLabor,
            textoFecha = registroElegido.diaPlanificado,
            sectorTrabajo = registroElegido.parcelaTrabajo,
            estudianteAsignado = registroElegido.encargadoLabor,
            errorDetalle = "", errorFecha = "", errorSector = "", errorEstudiante = "",
            avisoLabores = "Editando tarea.",
        )
    }

    fun limpiarLabor() {
        estadoMutable.value = EstadoLabores(
            listaLabores = estadoMutable.value.listaLabores,
            sectoresDisponibles = estadoMutable.value.sectoresDisponibles,
            estudiantesDisponibles = estadoMutable.value.estudiantesDisponibles,
            estudianteAsignado = estadoMutable.value.estudiantesDisponibles.firstOrNull()?.codigoCuenta ?: "USR-002",
        )
    }

    fun iniciarEntrega(registroElegido: LaborHuerto) {
        estadoMutable.value = estadoMutable.value.copy(
            laborEnEntrega = registroElegido,
            direccionFoto = "", textoComentario = "", errorFoto = "", errorComentario = "", avisoLabores = "",
        )
    }

    fun adjuntarImagen(direccionElegida: String) {
        estadoMutable.value = estadoMutable.value.copy(direccionFoto = direccionElegida, errorFoto = "")
    }

    fun escribirComentario(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(textoComentario = textoNuevo, errorComentario = "")
    }

    fun informarLabor(mensajeNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(avisoLabores = mensajeNuevo)
    }

    fun revisarEntrega(): Boolean {
        val formulario = estadoMutable.value
        val resultado = validadorLabores.validarEntrega(
            direccionFoto = formulario.direccionFoto,
            textoComentario = formulario.textoComentario
        )
        estadoMutable.value = formulario.copy(errorFoto = resultado.errorFoto, errorComentario = resultado.errorComentario)
        return resultado.esValido
    }

    fun entregarLabor(cuentaResponsable: CuentaHuerto) {
        if (cuentaResponsable.tipoCuenta != TipoCuenta.ALUMNO || estadoMutable.value.guardandoLabor || !revisarEntrega()) return
        val formulario = estadoMutable.value
        val laborElegida = formulario.laborEnEntrega ?: return
        estadoMutable.value = formulario.copy(guardandoLabor = true)
        viewModelScope.launch {
            try {
                repositorio.corregirLabor(laborElegida.copy(
                    situacionLabor = "PENDIENTE_DE_VALIDACION",
                    imagenLabor = formulario.direccionFoto,
                    notaEntrega = formulario.textoComentario.trim(),
                    encargadoLabor = cuentaResponsable.codigoCuenta,
                    momentoEntrega = LocalDateTime.now().toString(),
                    notaRevision = null, momentoRevision = null, revisorLabor = null,
                ))
                limpiarLabor()
                informarLabor("Evidencia enviada al docente.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                informarLabor("No se pudo enviar la evidencia. Intenta nuevamente.")
            } finally { estadoMutable.value = estadoMutable.value.copy(guardandoLabor = false) }
        }
    }

    fun revisarTrabajo(registroElegido: LaborHuerto, aceptarTrabajo: Boolean, cuentaResponsable: CuentaHuerto) {
        if (cuentaResponsable.tipoCuenta != TipoCuenta.DOCENTE || estadoMutable.value.guardandoLabor) return
        estadoMutable.value = estadoMutable.value.copy(guardandoLabor = true)
        viewModelScope.launch {
            try {
                repositorio.corregirLabor(registroElegido.copy(
                    situacionLabor = if (aceptarTrabajo) "VALIDADA" else "RECHAZADA",
                    notaRevision = if (aceptarTrabajo) "Trabajo aprobado." else "Revisa el trabajo y envía otra evidencia.",
                    revisorLabor = cuentaResponsable.codigoCuenta,
                    momentoRevision = LocalDateTime.now().toString(),
                ))
                informarLabor(if (aceptarTrabajo) "Tarea aprobada." else "Tarea devuelta al estudiante.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                informarLabor("No se pudo revisar la tarea.")
            } finally { estadoMutable.value = estadoMutable.value.copy(guardandoLabor = false) }
        }
    }

    fun eliminarLabor(registroElegido: LaborHuerto) {
        viewModelScope.launch {
            try {
                repositorio.borrarLabor(registroElegido)
                if (estadoMutable.value.laborEnEdicion?.codigoLabor == registroElegido.codigoLabor) limpiarLabor()
                informarLabor("Tarea eliminada.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                informarLabor("No se pudo eliminar la tarea.")
            }
        }
    }
}
