package cl.duoc.fotomotion.ui.viewmodel

import cl.duoc.fotomotion.domain.model.TipoCuenta

object ValidadorCuenta {
    fun validarNombre(nombreRaw: String, tipo: TipoCuenta): Pair<Boolean, String> {
        val nombre = nombreRaw.trim()
        val rol = if (tipo == TipoCuenta.DOCENTE) "docente" else "estudiante"
        return when {
            nombre.isBlank() -> false to "Escribe el nombre del $rol."
            nombre.length !in 3..50 -> false to "El nombre debe tener entre 3 y 50 caracteres."
            else -> true to ""
        }
    }

    fun formatearNombre(nombreRaw: String, tipo: TipoCuenta): String {
        val nombre = nombreRaw.trim()
        val prefijo = if (tipo == TipoCuenta.DOCENTE) "Profesor " else "Estudiante "
        return if (nombre.startsWith("Profesor ", ignoreCase = true) || nombre.startsWith("Estudiante ", ignoreCase = true)) {
            nombre
        } else {
            "$prefijo$nombre"
        }
    }
}
