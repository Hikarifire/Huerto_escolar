package cl.duoc.fotomotion.ui.viewmodel

class ValidadorParcelas {
    data class ResultadoValidacionParcela(
        val errorParcela: String = "",
        val errorLugar: String = ""
    ) {
        val esValido: Boolean
            get() = errorParcela.isEmpty() && errorLugar.isEmpty()
    }

    fun validarParcela(textoParcela: String, textoLugar: String): ResultadoValidacionParcela {
        var problemaNombre = ""
        var problemaLugar = ""

        if (textoParcela.isBlank()) {
            problemaNombre = "Escribe el nombre del sector."
        } else if (textoParcela.trim().length < 3 || textoParcela.length > 40) {
            problemaNombre = "Usa entre 3 y 40 caracteres."
        }

        if (textoLugar.isBlank()) {
            problemaLugar = "Escribe dónde está el sector."
        } else if (textoLugar.trim().length < 3 || textoLugar.length > 80) {
            problemaLugar = "Usa entre 3 y 80 caracteres."
        }

        return ResultadoValidacionParcela(
            errorParcela = problemaNombre,
            errorLugar = problemaLugar
        )
    }
}
