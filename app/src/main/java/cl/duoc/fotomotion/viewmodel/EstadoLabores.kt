package cl.duoc.fotomotion.ui.viewmodel

import cl.duoc.fotomotion.domain.model.CuentaHuerto
import cl.duoc.fotomotion.domain.model.LaborHuerto
import cl.duoc.fotomotion.domain.model.ParcelaHuerto
import java.time.LocalDate

data class EstadoLabores(
    val listaLabores: List<LaborHuerto> = emptyList(),
    val sectoresDisponibles: List<ParcelaHuerto> = emptyList(),
    val estudiantesDisponibles: List<CuentaHuerto> = emptyList(),
    val estudianteAsignado: String = "USR-002",
    val textoDetalle: String = "",
    val textoFecha: String = LocalDate.now().toString(),
    val sectorTrabajo: Int? = null,
    val errorDetalle: String = "",
    val errorFecha: String = "",
    val errorSector: String = "",
    val errorEstudiante: String = "",
    val laborEnEdicion: LaborHuerto? = null,
    val laborEnEntrega: LaborHuerto? = null,
    val direccionFoto: String = "",
    val textoComentario: String = "",
    val errorFoto: String = "",
    val errorComentario: String = "",
    val avisoLabores: String = "",
    val guardandoLabor: Boolean = false,
)
