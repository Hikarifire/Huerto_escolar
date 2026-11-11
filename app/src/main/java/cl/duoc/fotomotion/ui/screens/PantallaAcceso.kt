package cl.duoc.fotomotion.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cl.duoc.fotomotion.domain.model.*
import cl.duoc.fotomotion.ui.viewmodel.*

@Composable
fun PantallaAcceso(controlAcceso: AccesoViewModel, alIngresar: (CuentaHuerto) -> Unit) {
    val estadoVista by controlAcceso.estadoAcceso.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            HeaderHuertoAcceso()
        }
        item {
            TabRowAcceso(
                modoActual = estadoVista.modoAcceso,
                onModoSelected = controlAcceso::cambiarModoAcceso,
            )
        }
        item {
            AnimatedContent(
                targetState = estadoVista.modoAcceso,
                transitionSpec = { fadeIn(spring()) togetherWith fadeOut(spring()) },
                label = "ModoAccesoAnimado",
            ) { modo ->
                when (modo) {
                    ModoAcceso.LOGIN -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            SeccionIniciarSesion(
                                busqueda = estadoVista.usuarioBusqueda,
                                errorLogin = estadoVista.errorLogin,
                                onBusquedaChange = controlAcceso::escribirUsuarioBusqueda,
                                onIniciarSesion = {
                                    controlAcceso.iniciarSesionPorBusqueda()?.let(alIngresar)
                                },
                            )
                            Text("O selecciona una cuenta registrada", style = MaterialTheme.typography.titleMedium)
                            estadoVista.cuentasDisponibles.forEach { cuenta ->
                                TarjetaCuentaItem(cuenta = cuenta, alIngresar = alIngresar)
                            }
                        }
                    }
                    ModoAcceso.REGISTRO -> {
                        SeccionRegistroUsuario(
                            nombreRegistro = estadoVista.nombreRegistro,
                            tipoCuenta = estadoVista.tipoCuentaRegistro,
                            errorRegistro = estadoVista.errorRegistroNombre,
                            onNombreChange = controlAcceso::escribirNombreRegistro,
                            onTipoCuentaChange = controlAcceso::cambiarTipoCuentaRegistro,
                            onRegistrar = {
                                controlAcceso.registrarNuevoUsuario()?.let(alIngresar)
                            },
                        )
                    }
                    ModoAcceso.RAPIDO -> {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("Selección rápida de perfiles", style = MaterialTheme.typography.titleMedium)
                            estadoVista.cuentasDisponibles.forEach { cuenta ->
                                TarjetaCuentaItem(cuenta = cuenta, alIngresar = alIngresar)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderHuertoAcceso() {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.size(52.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Huerto Escolar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Huerto Escolar",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    "Accede a tu taller para organizar tareas y evidencias",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                )
            }
        }
    }
}

@Composable
private fun TabRowAcceso(
    modoActual: ModoAcceso,
    onModoSelected: (ModoAcceso) -> Unit,
) {
    PrimaryTabRow(selectedTabIndex = modoActual.ordinal) {
        Tab(
            selected = modoActual == ModoAcceso.LOGIN,
            onClick = { onModoSelected(ModoAcceso.LOGIN) },
            text = { Text("Iniciar Sesión") },
        )
        Tab(
            selected = modoActual == ModoAcceso.REGISTRO,
            onClick = { onModoSelected(ModoAcceso.REGISTRO) },
            text = { Text("Registrarse") },
        )
        Tab(
            selected = modoActual == ModoAcceso.RAPIDO,
            onClick = { onModoSelected(ModoAcceso.RAPIDO) },
            text = { Text("Ver Todos") },
        )
    }
}

@Composable
private fun SeccionIniciarSesion(
    busqueda: String,
    errorLogin: String,
    onBusquedaChange: (String) -> Unit,
    onIniciarSesion: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Ingresa con tu usuario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Escribe tu nombre de usuario o código (ej. Mateo Rojas o USR-002)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = busqueda,
                onValueChange = onBusquedaChange,
                label = { Text("Nombre o Código de usuario") },
                leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Usuario") },
                isError = errorLogin.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (errorLogin.isNotEmpty()) {
                Text(
                    errorLogin,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(
                onClick = onIniciarSesion,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Iniciar Sesión")
            }
        }
    }
}

@Composable
private fun SeccionRegistroUsuario(
    nombreRegistro: String,
    tipoCuenta: TipoCuenta,
    errorRegistro: String,
    onNombreChange: (String) -> Unit,
    onTipoCuentaChange: (TipoCuenta) -> Unit,
    onRegistrar: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                "Registro de Usuario",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "Crea una nueva cuenta para participar en el taller del huerto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text("Tipo de perfil", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                FilterChip(
                    selected = tipoCuenta == TipoCuenta.ALUMNO,
                    onClick = { onTipoCuentaChange(TipoCuenta.ALUMNO) },
                    label = { Text("Estudiante (Alumno)") },
                    leadingIcon = {
                        if (tipoCuenta == TipoCuenta.ALUMNO) Icon(Icons.Default.CheckCircle, contentDescription = null)
                    },
                )
                FilterChip(
                    selected = tipoCuenta == TipoCuenta.DOCENTE,
                    onClick = { onTipoCuentaChange(TipoCuenta.DOCENTE) },
                    label = { Text("Profesor (Docente)") },
                    leadingIcon = {
                        if (tipoCuenta == TipoCuenta.DOCENTE) Icon(Icons.Default.CheckCircle, contentDescription = null)
                    },
                )
            }

            OutlinedTextField(
                value = nombreRegistro,
                onValueChange = onNombreChange,
                label = { Text("Nombre completo") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Nombre") },
                isError = errorRegistro.isNotEmpty(),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            if (errorRegistro.isNotEmpty()) {
                Text(
                    errorRegistro,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Button(
                onClick = onRegistrar,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (tipoCuenta == TipoCuenta.DOCENTE) "Registrarse e ingresar como docente"
                    else "Registrarse e ingresar como estudiante",
                )
            }
        }
    }
}

@Composable
private fun TarjetaCuentaItem(cuenta: CuentaHuerto, alIngresar: (CuentaHuerto) -> Unit) {
    val esDocente = cuenta.tipoCuenta == TipoCuenta.DOCENTE
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    cuenta.nombreCuenta,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                AssistChip(
                    onClick = {},
                    label = { Text(if (esDocente) "DOCENTE" else "ALUMNO") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (esDocente) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
                        labelColor = if (esDocente) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                )
            }
            Text(
                if (esDocente)
                    "Administra sectores, cultivos y revisa tareas (${cuenta.codigoCuenta})."
                else
                    "Consulta tareas asignadas y sube tu evidencia (${cuenta.codigoCuenta}).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(
                onClick = { alIngresar(cuenta) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (esDocente) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                ),
            ) {
                Text(
                    if (esDocente) "Ingresar como docente" else "Ingresar como estudiante",
                )
            }
        }
    }
}
