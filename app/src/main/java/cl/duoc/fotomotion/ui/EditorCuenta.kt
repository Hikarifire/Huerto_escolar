package cl.duoc.fotomotion.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import cl.duoc.fotomotion.domain.Acceso
import cl.duoc.fotomotion.domain.Accion
import cl.duoc.fotomotion.domain.HuertoUiState
import cl.duoc.fotomotion.domain.RolNuevaCuenta

@Composable
fun EditorCuenta(estado: HuertoUiState, guardar: (Accion.CrearCuenta) -> Unit) {
    var rol by rememberSaveable { mutableStateOf(RolNuevaCuenta.ALUMNO) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var intentoGuardar by rememberSaveable { mutableStateOf(false) }

    Pagina {
        Encabezado("Nueva cuenta", "Registra a un docente o estudiante del huerto.")
        Selector("Perfil", RolNuevaCuenta.entries.map { it to it.etiqueta }, rol) { rol = it }
        Campo("Nombre completo", nombre, { nombre = it }, estado.campos["nombre"])
        Campo("Correo", correo, { correo = it }, estado.campos["correo"], teclado = KeyboardType.Email)
        Campo("Contraseña inicial", clave, { clave = it }, estado.campos["contraseña"],
            teclado = KeyboardType.Password, secreto = true)
        val errorConfirmacion = "Las contraseñas deben coincidir."
            .takeIf { intentoGuardar && clave != confirmacion }
        Campo("Repite la contraseña", confirmacion, { confirmacion = it }, errorConfirmacion,
            teclado = KeyboardType.Password, secreto = true)
        Text("La persona podrá ingresar con el correo y la contraseña que registres.",
            style = MaterialTheme.typography.bodySmall)
        Guardar("Registrar cuenta", !estado.ocupado) {
            intentoGuardar = true
            if (clave == confirmacion) guardar(Accion.CrearCuenta(rol, Acceso(correo.trim(), clave, nombre.trim())))
        }
    }
}
