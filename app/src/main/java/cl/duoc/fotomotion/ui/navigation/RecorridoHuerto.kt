package cl.duoc.fotomotion.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import cl.duoc.fotomotion.domain.model.TipoCuenta
import cl.duoc.fotomotion.ui.screens.*
import cl.duoc.fotomotion.ui.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecorridoHuerto() {
    val controlAcceso: AccesoViewModel = viewModel()
    val controlParcelas: ParcelasViewModel = viewModel()
    val controlSiembras: SiembrasViewModel = viewModel()
    val controlLabores: LaboresViewModel = viewModel()
    val controlLecturas: LecturasViewModel = viewModel()
    val cuentaEnSesion by controlAcceso.cuentaActiva.collectAsState()
    val estadoAcceso by controlAcceso.estadoAcceso.collectAsState()
    val navegacionHuerto = rememberNavController()
    val entradaNavegacion by navegacionHuerto.currentBackStackEntryAsState()
    val rutaActual = entradaNavegacion?.destination?.route ?: "acceso"
    val opcionesMenu = listOf("parcelas", "siembras", "labores", "lecturas")

    LaunchedEffect(estadoAcceso.cuentasDisponibles) {
        val alumnos = estadoAcceso.cuentasDisponibles.filter { it.tipoCuenta == TipoCuenta.ALUMNO }
        controlLabores.actualizarEstudiantesDisponibles(alumnos)
    }

    LaunchedEffect(cuentaEnSesion) {
        if (cuentaEnSesion == null && rutaActual != "acceso") {
            navegacionHuerto.navigate("acceso") { popUpTo(navegacionHuerto.graph.id) { inclusive = true } }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = rutaActual,
                        transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                        label = "TituloAnimado",
                    ) { ruta ->
                        Text(
                            when (ruta) {
                                "parcelas" -> "Sectores del huerto"
                                "siembras" -> "Cultivos del huerto"
                                "labores" -> "Tareas del huerto"
                                "lecturas" -> "Lecturas de ejemplo"
                                else -> "Huerto Escolar"
                            },
                        )
                    }
                },
                actions = {
                    if (cuentaEnSesion != null) {
                        TextButton(onClick = {
                            controlParcelas.limpiarParcela()
                            controlSiembras.limpiarSiembra()
                            controlLabores.limpiarLabor()
                            controlAcceso.salirDeCuenta()
                        }) { Text("Salir") }
                    }
                },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                visible = (cuentaEnSesion != null && rutaActual != "acceso"),
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                NavigationBar {
                    opcionesMenu.forEach { rutaMenu ->
                        val nombreMenu = when (rutaMenu) {
                            "parcelas" -> "Sectores"
                            "siembras" -> "Cultivos"
                            "labores" -> "Tareas"
                            else -> "Lecturas"
                        }
                        NavigationBarItem(
                            selected = (rutaActual == rutaMenu),
                            onClick = {
                                navegacionHuerto.navigate(rutaMenu) {
                                    popUpTo("parcelas")
                                    launchSingleTop = true
                                }
                            },
                            label = { Text(nombreMenu) },
                            icon = {
                                Icon(
                                    when (rutaMenu) {
                                        "parcelas" -> Icons.Default.Home
                                        "siembras" -> Icons.AutoMirrored.Filled.List
                                        "labores" -> Icons.Default.Build
                                        else -> Icons.Default.Warning
                                    },
                                    contentDescription = nombreMenu,
                                )
                            },
                        )
                    }
                }
            }
        },
    ) { espacioPantalla ->
        NavHost(
            navController = navegacionHuerto,
            startDestination = "acceso",
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioPantalla)
                .consumeWindowInsets(espacioPantalla)
                .imePadding(),
        ) {
            composable("acceso") {
                PantallaAcceso(controlAcceso) { cuentaElegida ->
                    controlAcceso.entrarConCuenta(cuentaElegida)
                    navegacionHuerto.navigate("parcelas") { popUpTo("acceso") { inclusive = true } }
                }
            }
            composable("parcelas") { cuentaEnSesion?.let { cuenta -> PantallaParcelas(controlParcelas, cuenta) } }
            composable("siembras") { cuentaEnSesion?.let { cuenta -> PantallaSiembras(controlSiembras, cuenta) } }
            composable("labores") { cuentaEnSesion?.let { cuenta -> PantallaLabores(controlLabores, cuenta) } }
            composable("lecturas") { if (cuentaEnSesion != null) PantallaLecturas(controlLecturas) }
        }
    }
}
