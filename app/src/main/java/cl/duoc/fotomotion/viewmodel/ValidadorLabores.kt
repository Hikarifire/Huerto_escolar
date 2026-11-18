package cl.duoc.fotomotion.ui.viewmodel

import cl.duoc.fotomotion.domain.model.ParcelaHuerto
import java.time.LocalDate
import java.time.format.DateTimeParseException

class ValidadorLabores {
    data class ResultadoValidacionLabor(
        val errorDetalle: String = "",
        val errorFecha: String = "",
        val errorSector: String = "",
        val errorEstudiante: String = "",
    ) {
        val esValido: Boolean
            get() = errorDetalle.isEmpty() && errorFecha.isEmpty() && errorSector.isEmpty() && errorEstudiante.isEmpty()
    }

    data class ResultadoValidacionEntrega(
        val errorFoto: String = "",
        val errorComentario: String = "",
    ) {
        val esValido: Boolean
            get() = errorFoto.isEmpty() && errorComentario.isEmpty()
    }

    fun validarLabor(
        textoDetalle: String,
        textoFecha: String,
        sectorTrabajo: Int?,
        sectoresDisponibles: List<ParcelaHuerto>,
        estudianteAsignado: String
    ): ResultadoValidacionLabor {
        var problemaDetalle = ""
        var problemaFecha = ""
        var problemaSector = ""
        var problemaEstudiante = ""

        if (textoDetalle.isBlank()) {
            problemaDetalle = "Explica qué hay que hacer."
        } else if (textoDetalle.trim().length < 5 || textoDetalle.length > 150) {
            problemaDetalle = "Usa entre 5 y 150 caracteres."
        }

        if (textoFecha.isBlank()) {
            problemaFecha = "Escribe una fecha."
        } else if (textoFecha.length != 10) {
            problemaFecha = "Usa el formato AAAA-MM-DD."
        } else {
            try {
                LocalDate.parse(textoFecha)
            } catch (_: DateTimeParseException) {
                problemaFecha = "Escribe una fecha real: AAAA-MM-DD."
            }
        }

        if (sectorTrabajo == null) {
            problemaSector = "Selecciona el sector de la tarea."
        } else if (sectoresDisponibles.none { parcela -> parcela.codigoParcela == sectorTrabajo }) {
            problemaSector = "El sector ya no está disponible."
        }

        if (estudianteAsignado.isBlank()) {
            problemaEstudiante = "Selecciona un estudiante para la tarea."
        }

        return ResultadoValidacionLabor(
            errorDetalle = problemaDetalle,
            errorFecha = problemaFecha,
            errorSector = problemaSector,
            errorEstudiante = problemaEstudiante
        )
    }

    fun validarEntrega(
        direccionFoto: String,
        textoComentario: String
    ): ResultadoValidacionEntrega {
        var problemaFoto = ""
        var problemaComentario = ""

        if (direccionFoto.isBlank()) {
            problemaFoto = "Adjunta una foto con cámara o galería."
        }

        if (textoComentario.isBlank()) {
            problemaComentario = "Cuenta qué hiciste en el huerto."
        } else if (textoComentario.trim().length < 5 || textoComentario.length > 300) {
            problemaComentario = "Usa entre 5 y 300 caracteres."
        }

        return ResultadoValidacionEntrega(
            errorFoto = problemaFoto,
            errorComentario = problemaComentario
        )
    }
}
