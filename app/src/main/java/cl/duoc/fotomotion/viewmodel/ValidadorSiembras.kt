package cl.duoc.fotomotion.ui.viewmodel

import cl.duoc.fotomotion.domain.model.ParcelaHuerto

class ValidadorSiembras {
    data class ResultadoValidacionSiembra(
        val errorSiembra: String = "",
        val errorSeleccion: String = ""
    ) {
        val esValido: Boolean
            get() = errorSiembra.isEmpty() && errorSeleccion.isEmpty()
    }

    fun validarSiembra(
        textoSiembra: String,
        parcelaElegida: Int?,
        opcionesParcela: List<ParcelaHuerto>
    ): ResultadoValidacionSiembra {
        var problemaNombre = ""
        var problemaSeleccion = ""

        if (textoSiembra.isBlank()) {
            problemaNombre = "Escribe el nombre del cultivo."
        } else if (textoSiembra.trim().length < 3 || textoSiembra.length > 40) {
            problemaNombre = "Usa entre 3 y 40 caracteres."
        }

        if (parcelaElegida == null) {
            problemaSeleccion = "Selecciona un sector."
        } else if (opcionesParcela.none { parcela -> parcela.codigoParcela == parcelaElegida }) {
            problemaSeleccion = "El sector ya no está disponible."
        }

        return ResultadoValidacionSiembra(
            errorSiembra = problemaNombre,
            errorSeleccion = problemaSeleccion
        )
    }
}
