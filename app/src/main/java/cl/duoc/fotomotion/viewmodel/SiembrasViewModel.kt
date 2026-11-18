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

class SiembrasViewModel(aplicacion: Application) : AndroidViewModel(aplicacion) {
    private val repositorio = RepositorioHuerto(BaseHuerto.abrirBase(aplicacion).consultasHuerto())
    private val validadorSiembras = ValidadorSiembras()
    private val estadoMutable = MutableStateFlow(EstadoSiembras())
    val estadoSiembras = estadoMutable.asStateFlow()

    init {
        viewModelScope.launch {
            repositorio.leerSiembras().collect { siembrasRecibidas ->
                estadoMutable.value = estadoMutable.value.copy(listaSiembras = siembrasRecibidas)
            }
        }
        viewModelScope.launch {
            repositorio.leerParcelas().collect { parcelasRecibidas ->
                estadoMutable.value = estadoMutable.value.copy(opcionesParcela = parcelasRecibidas)
            }
        }
    }
    fun escribirSiembra(textoNuevo: String) {
        estadoMutable.value = estadoMutable.value.copy(textoSiembra = textoNuevo, errorSiembra = "", avisoSiembras = "")
    }
    fun elegirParcela(codigoElegido: Int) {
        estadoMutable.value = estadoMutable.value.copy(parcelaElegida = codigoElegido, errorSeleccion = "")
    }
    fun revisarSiembra(): Boolean {
        val formulario = estadoMutable.value
        val resultado = validadorSiembras.validarSiembra(formulario.textoSiembra, formulario.parcelaElegida, formulario.opcionesParcela)
        estadoMutable.value = formulario.copy(errorSiembra = resultado.errorSiembra, errorSeleccion = resultado.errorSeleccion)
        return resultado.esValido
    }
    fun confirmarSiembra() {
        if (estadoMutable.value.guardandoSiembra || !revisarSiembra()) return
        val formulario = estadoMutable.value
        val codigoParcela = formulario.parcelaElegida ?: return
        val siembraAnterior = formulario.siembraEnEdicion
        val registroParaGuardar = (siembraAnterior ?: SiembraHuerto(tituloSiembra = "",
            parcelaAsociada = codigoParcela, intervaloRiego = 1, situacionSiembra = "SEMBRADO",
            diaSiembra = LocalDate.now().toString())).copy(
                tituloSiembra = formulario.textoSiembra.trim(), parcelaAsociada = codigoParcela)
        estadoMutable.value = formulario.copy(guardandoSiembra = true)
        viewModelScope.launch {
            try {
                if (siembraAnterior == null) repositorio.registrarSiembra(registroParaGuardar)
                else repositorio.corregirSiembra(registroParaGuardar)
                limpiarSiembra()
                estadoMutable.value = estadoMutable.value.copy(avisoSiembras = "Cultivo guardado.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                estadoMutable.value = estadoMutable.value.copy(avisoSiembras = "No se pudo guardar el cultivo.")
            } finally {
                estadoMutable.value = estadoMutable.value.copy(guardandoSiembra = false)
            }
        }
    }
    fun prepararSiembra(registroElegido: SiembraHuerto) {
        estadoMutable.value = estadoMutable.value.copy(siembraEnEdicion = registroElegido,
            textoSiembra = registroElegido.tituloSiembra, parcelaElegida = registroElegido.parcelaAsociada,
            errorSiembra = "", errorSeleccion = "", avisoSiembras = "Editando cultivo.")
    }
    fun limpiarSiembra() {
        estadoMutable.value = EstadoSiembras(listaSiembras = estadoMutable.value.listaSiembras,
            opcionesParcela = estadoMutable.value.opcionesParcela)
    }
    fun eliminarSiembra(registroElegido: SiembraHuerto) {
        viewModelScope.launch {
            try {
                val eliminacionCorrecta = repositorio.borrarSiembra(registroElegido)
                if (eliminacionCorrecta && estadoMutable.value.siembraEnEdicion?.codigoSiembra == registroElegido.codigoSiembra) limpiarSiembra()
                estadoMutable.value = estadoMutable.value.copy(avisoSiembras = if (eliminacionCorrecta)
                    "Cultivo eliminado." else "Primero elimina las tareas de este cultivo.")
            } catch (problema: Exception) {
                if (problema is CancellationException) throw problema
                estadoMutable.value = estadoMutable.value.copy(avisoSiembras = "No se pudo eliminar el cultivo.")
            }
        }
    }
}
