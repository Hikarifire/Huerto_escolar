package cl.duoc.fotomotion.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import cl.duoc.fotomotion.domain.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Huerto(vm: HuertoViewModel = viewModel()) {
    val store = vm.store
    val estado by store.estado.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val usuario = estado.sesion?.usuario
    val nav = key(usuario?.id) { rememberNavController() }
    val entrada by nav.currentBackStackEntryAsState()
    val ruta = entrada?.destination?.route
    val principales = listOf("Inicio", "Sectores", "Tareas", "Más")
    val iconos = listOf(Icons.Default.Home, Icons.Default.Place, Icons.AutoMirrored.Filled.List, Icons.Default.MoreVert)

    fun navegar(destino: String) {
        store.limpiarMensajes()
        nav.navigate(destino) { launchSingleTop = true }
    }

    fun ejecutar(accion: Accion, alGuardar: (() -> Unit)? = null) {
        scope.launch { if (store.ejecutar(accion)) alGuardar?.invoke() }
    }

    LaunchedEffect(estado.aviso) {
        estado.aviso?.let { snackbar.showSnackbar(it, duration = SnackbarDuration.Short) }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(title = { Text("Huerto Escolar") }, navigationIcon = {
                if (usuario != null && ruta != null && ruta !in principales) {
                    IconButton(onClick = { store.limpiarMensajes(); nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            }, actions = {
                if (usuario != null) TextButton(onClick = { scope.launch { store.actualizar() } }, enabled = !estado.ocupado) {
                    Text("Actualizar")
                }
            })
        },
        bottomBar = {
            if (usuario != null) NavigationBar {
                principales.forEachIndexed { index, destino ->
                    NavigationBarItem(selected = ruta == destino, onClick = {
                        store.limpiarMensajes()
                        nav.navigate(destino) {
                            popUpTo("Inicio")
                            launchSingleTop = true
                        }
                    }, icon = { Icon(iconos[index], null) }, label = { Text(destino) })
                }
            }
        },
    ) { insets ->
        Column(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets).imePadding()) {
            AnimatedVisibility(estado.ocupado) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            AnimatedVisibility(estado.error != null) {
                Text(estado.error.orEmpty(), Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error)
            }
            if (usuario == null) {
                Ingreso(estado) { acceso -> scope.launch { store.acceder(acceso) } }
            } else {
                val datos = estado.datos
                val hoy = LocalDate.now(ZoneId.of("America/Santiago"))
                NavHost(navController = nav, startDestination = "Inicio", modifier = Modifier.weight(1f)) {
                    composable("Inicio") { Inicio(usuario, datos, hoy, ::navegar) }
                    composable("Sectores") {
                        Pagina {
                            Encabezado("Sectores")
                            if (usuario.esDocente) Guardar("Nuevo sector", !estado.ocupado) { navegar("sectorEditar/0") }
                            if (datos.sectores.isEmpty()) Text("Todavía no hay sectores registrados.")
                            datos.sectores.forEach { sector ->
                                Tarjeta(sector.nombre, listOfNotNull(sector.ubicacion, if (sector.activo) "Activo" else "Inactivo")
                                    .joinToString(" · ")) { navegar("sector/${sector.id}") }
                            }
                        }
                    }
                    composable("sector/{id}") { e ->
                        val sector = datos.sectores.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        Pagina {
                            if (sector == null) {
                                Text("Sector no disponible.")
                            } else {
                                Encabezado(sector.nombre, sector.ubicacion)
                                Text(sector.descripcion.orEmpty())
                                Text(if (sector.activo) "Activo" else "Inactivo")
                                if (usuario.esDocente) {
                                    Guardar("Editar sector", !estado.ocupado) { navegar("sectorEditar/${sector.id}") }
                                    Borrar("Eliminar sector", !estado.ocupado) {
                                        ejecutar(Accion.Eliminar(Recurso.SECTOR, sector.id)) { nav.popBackStack() }
                                    }
                                }
                                Text("Cultivos del sector", style = MaterialTheme.typography.titleLarge)
                                val cultivos = datos.cultivos.filter { it.sectorId == sector.id }
                                if (cultivos.isEmpty()) Text("Todavía no hay cultivos en este sector.")
                                cultivos.forEach { Tarjeta(it.nombre, etiqueta(it.estado)) { navegar("cultivo/${it.id}") } }
                            }
                        }
                    }
                    composable("sectorEditar/{id}") { e ->
                        val sector = datos.sectores.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (usuario.esDocente) EditorSector(sector, estado) { ejecutar(it) { nav.popBackStack() } }
                    }
                    composable("Cultivos") {
                        Pagina {
                            Encabezado("Cultivos")
                            if (usuario.esDocente) Guardar("Registrar cultivo", !estado.ocupado) { navegar("cultivoEditar/0") }
                            if (datos.cultivos.isEmpty()) Text("Todavía no hay cultivos registrados.")
                            datos.cultivos.forEach { planta ->
                                Tarjeta(planta.nombre, "${planta.sectorNombre} · ${etiqueta(planta.estado)}") {
                                    navegar("cultivo/${planta.id}")
                                }
                            }
                        }
                    }
                    composable("cultivo/{id}") { e ->
                        val cultivo = datos.cultivos.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (cultivo == null) Pagina { Text("Cultivo no disponible.") }
                        else DetalleCultivo(cultivo, usuario.esDocente, estado.ocupado,
                            editar = { navegar("cultivoEditar/${cultivo.id}") },
                            borrar = { ejecutar(Accion.Eliminar(Recurso.CULTIVO, cultivo.id)) { nav.popBackStack() } })
                    }
                    composable("cultivoEditar/{id}") { e ->
                        val cultivo = datos.cultivos.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (usuario.esDocente) EditorCultivo(cultivo, estado) { ejecutar(it) { nav.popBackStack() } }
                    }
                    composable("Tareas") {
                        ListaTareas(estado, usuario.esDocente, hoy, ::navegar)
                    }
                    composable("tarea/{id}") { e ->
                        val tarea = datos.tareas.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (tarea == null) Pagina { Text("Tarea no disponible.") }
                        else DetalleTarea(tarea, estado, store, hoy, ::navegar) { accion ->
                            if (accion is Accion.Eliminar) ejecutar(accion) { nav.popBackStack() }
                            else ejecutar(accion)
                        }
                    }
                    composable("tareaEditar/{id}") { e ->
                        val tarea = datos.tareas.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (usuario.esDocente) EditorTarea(tarea, estado) { ejecutar(it) { nav.popBackStack() } }
                    }
                    composable("revisar/{tarea}/{id}") { e ->
                        val tarea = e.arguments?.getString("tarea")?.toLongOrNull()
                        val id = e.arguments?.getString("id")?.toLongOrNull()
                        val evidencia = tarea?.let { estado.evidencia[it] }?.find { it.id == id }
                        if (usuario.esDocente && evidencia != null) EditorRevision(evidencia, estado, store) {
                            ejecutar(it) { nav.popBackStack() }
                        }
                    }
                    composable("Reportes") {
                        Pagina {
                            Encabezado("Reportes de problemas")
                            Guardar("Nuevo reporte", !estado.ocupado) { navegar("reportar") }
                            if (datos.reportes.isEmpty()) Text("Todavía no hay problemas reportados.")
                            datos.reportes.forEach { reporte ->
                                Tarjeta(reporte.descripcion, "${reporte.sectorNombre} · ${etiqueta(reporte.estado)}") {
                                    navegar("reporte/${reporte.id}")
                                }
                            }
                        }
                    }
                    composable("reportar") { EditorReporte(estado) { ejecutar(it) { nav.popBackStack() } } }
                    composable("reporte/{id}") { e ->
                        val reporte = datos.reportes.find { it.id == e.arguments?.getString("id")?.toLongOrNull() }
                        if (reporte != null) DetalleReporte(reporte, estado, store) { ejecutar(it) }
                    }
                    composable("Alertas") {
                        Pagina {
                            Encabezado("Alertas")
                            val alertas = Reglas.alertas(datos, hoy)
                            if (alertas.isEmpty()) Text("No hay alertas disponibles.")
                            alertas.forEach { Tarjeta(it.titulo, "${it.descripcion} · ${it.fecha}") }
                        }
                    }
                    composable("Historial") {
                        LaunchedEffect(usuario.id) { store.historial() }
                        Pagina {
                            Encabezado("Historial")
                            val eventos = Reglas.historial(datos, estado.evidencia.values.flatten())
                            if (eventos.isEmpty()) Text("Todavía no hay actividad registrada.")
                            eventos.forEach { Tarjeta(it.titulo, "${it.descripcion}\n${it.fecha.replace('T', ' ').take(16)}") }
                        }
                    }
                    composable("Lecturas") { Lecturas(estado) { ejecutar(it) } }
                    composable("Clima") {
                        LaunchedEffect(Unit) { store.clima() }
                        ClimaPantalla(estado) { scope.launch { store.clima() } }
                    }
                    composable("Más") {
                        Pagina {
                            Encabezado("Más")
                            listOf("Cultivos", "Reportes", "Alertas", "Historial", "Lecturas", "Clima").forEach {
                                Tarjeta(it, alTocar = { navegar(it) })
                            }
                            OutlinedButton(onClick = { scope.launch { store.salir() } }, enabled = !estado.ocupado,
                                modifier = Modifier.fillMaxWidth()) { Text("Cerrar sesión") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Ingreso(estado: HuertoUiState, entrar: (Acceso) -> Unit) {
    var registro by rememberSaveable { mutableStateOf(false) }
    var nombre by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    Pagina {
        Encabezado(if (registro) "Crear cuenta" else "Bienvenido al huerto",
            if (registro) "El registro crea una cuenta de estudiante." else "Ingresa con tu cuenta de docente o estudiante.")
        AnimatedVisibility(registro) { Campo("Nombre", nombre, { nombre = it }, estado.campos["nombre"]) }
        Campo("Correo", correo, { correo = it }, estado.campos["correo"], teclado = KeyboardType.Email)
        Campo("Contraseña", clave, { clave = it }, estado.campos["contraseña"], teclado = KeyboardType.Password, secreto = true)
        Guardar(if (registro) "Crear cuenta" else "Iniciar sesión", !estado.ocupado) {
            entrar(Acceso(correo, clave, nombre.takeIf { registro }))
        }
        TextButton(onClick = { registro = !registro }, enabled = !estado.ocupado) {
            Text(if (registro) "Volver al ingreso" else "Registrarme como estudiante")
        }
    }
}

@Composable
private fun Inicio(usuario: Usuario, datos: Datos, hoy: LocalDate, navegar: (String) -> Unit) {
    Pagina {
        Encabezado("Hola, ${usuario.nombre}", if (usuario.esDocente) "Perfil docente" else "Perfil estudiante")
        val pendientes = datos.tareas.count { it.estado in setOf("PENDIENTE", "EN_DESARROLLO", "RECHAZADA") }
        val porRevisar = datos.tareas.count { it.estado == "EN_REVISION" }
        Tarjeta(if (usuario.esDocente) "Tareas del huerto" else "Mis tareas",
            if (usuario.esDocente) "$pendientes activas · $porRevisar por revisar" else "$pendientes activas · $porRevisar enviadas") {
            navegar("Tareas")
        }
        val alertas = Reglas.alertas(datos, hoy)
        Tarjeta("Alertas del huerto", alertas.firstOrNull()?.let { "${it.titulo} · ${it.descripcion}" } ?: "Sin alertas") {
            navegar("Alertas")
        }
        Tarjeta("Reportes", "${datos.reportes.count { it.estado != "RESUELTO" }} en seguimiento") { navegar("Reportes") }
        Tarjeta("Clima en San Bernardo", "Temperatura, humedad y precipitación") { navegar("Clima") }
    }
}
