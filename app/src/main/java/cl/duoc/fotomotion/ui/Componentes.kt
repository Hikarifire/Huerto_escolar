package cl.duoc.fotomotion.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import cl.duoc.fotomotion.domain.HuertoStore
import coil3.compose.AsyncImage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun Pagina(contenido: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp), content = contenido)
}

@Composable
fun Encabezado(titulo: String, texto: String? = null) {
    Text(titulo, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    texto?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

@Composable
fun Tarjeta(titulo: String, texto: String? = null, alTocar: (() -> Unit)? = null) {
    if (alTocar == null) Card(Modifier.fillMaxWidth()) { CuerpoTarjeta(titulo, texto) }
    else Card(onClick = alTocar, modifier = Modifier.fillMaxWidth()) { CuerpoTarjeta(titulo, texto) }
}

@Composable
private fun CuerpoTarjeta(titulo: String, texto: String?) {
    Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        texto?.takeIf(String::isNotBlank)?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun Campo(etiqueta: String, valor: String, cambiar: (String) -> Unit, error: String? = null,
          multilinea: Boolean = false, teclado: KeyboardType = KeyboardType.Text, secreto: Boolean = false) {
    OutlinedTextField(value = valor, onValueChange = cambiar, label = { Text(etiqueta) },
        modifier = Modifier.fillMaxWidth(), isError = error != null,
        supportingText = error?.let { { Text(it) } }, singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (secreto) PasswordVisualTransformation() else VisualTransformation.None)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Selector(titulo: String, opciones: List<Pair<T, String>>, actual: T?, error: String? = null,
                 seleccionar: (T) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = abierto, onExpandedChange = { abierto = it }) {
        OutlinedTextField(
            value = opciones.firstOrNull { it.first == actual }?.second.orEmpty(),
            onValueChange = {}, readOnly = true, label = { Text(titulo) }, singleLine = true,
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(abierto) }, isError = error != null,
            supportingText = error?.let { { Text(it) } },
        )
        ExposedDropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
            opciones.forEach { (valor, texto) ->
                DropdownMenuItem(text = { Text(texto) }, onClick = { seleccionar(valor); abierto = false })
            }
        }
    }
}

@Composable
fun Guardar(texto: String, habilitado: Boolean, ejecutar: () -> Unit) =
    Button(onClick = ejecutar, enabled = habilitado, modifier = Modifier.fillMaxWidth()) { Text(texto) }

@Composable
fun Borrar(texto: String, habilitado: Boolean, ejecutar: () -> Unit) {
    var confirmar by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { confirmar = true }, enabled = habilitado, modifier = Modifier.fillMaxWidth()) { Text(texto) }
    if (confirmar) AlertDialog(
        onDismissRequest = { confirmar = false }, title = { Text(texto) },
        text = { Text("¿Confirmas la eliminación de este registro?") },
        confirmButton = { TextButton(onClick = { confirmar = false; ejecutar() }) { Text("Eliminar") } },
        dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } },
    )
}

@Composable
fun FotoLocal(ruta: String?, error: String? = null, cambiar: (String?) -> Unit) {
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var destino by rememberSaveable { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val carpeta = remember(contexto) { File(contexto.filesDir, "fotos").apply { mkdirs() } }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { tomada ->
        val archivo = destino?.let(::File)
        if (tomada && archivo != null && archivo.length() in 1..5_242_880) cambiar(archivo.path)
        else {
            archivo?.delete()
            aviso = if (tomada) "La fotografía debe pesar hasta 5 MB." else "Captura cancelada."
        }
        destino = null
    }
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val archivo = withContext(Dispatchers.IO) {
                    val salida = File.createTempFile("huerto_", ".img", carpeta)
                    try {
                        val contenido = contexto.contentResolver.openInputStream(uri)
                            ?: error("No se pudo abrir la fotografía.")
                        contenido.use { entrada ->
                            salida.outputStream().use { archivo ->
                                val buffer = ByteArray(8192)
                                var total = 0
                                while (true) {
                                    val leidos = entrada.read(buffer)
                                    if (leidos < 0) break
                                    total += leidos
                                    require(total <= 5_242_880) { "La fotografía debe pesar hasta 5 MB." }
                                    archivo.write(buffer, 0, leidos)
                                }
                                require(total > 0) { "La fotografía está vacía." }
                            }
                        }
                        salida
                    } catch (ex: Exception) {
                        salida.delete()
                        throw ex
                    }
                }
                cambiar(archivo.path)
            } catch (ex: CancellationException) {
                throw ex
            } catch (ex: Exception) {
                aviso = ex.message ?: "No se pudo copiar la fotografía."
            }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = {
            aviso = null
            try {
                val archivo = File.createTempFile("huerto_", ".jpg", carpeta)
                destino = archivo.path
                camara.launch(FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo))
            } catch (_: Exception) {
                destino?.let { File(it).delete() }
                destino = null
                aviso = "No se pudo abrir la cámara. Usa la galería."
            }
        }, modifier = Modifier.weight(1f)) { Text("Cámara") }
        OutlinedButton(onClick = {
            aviso = null
            galeria.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }, modifier = Modifier.weight(1f)) { Text("Galería") }
    }
    AnimatedVisibility(ruta != null) {
        Column {
            AsyncImage(ruta?.let(::File), "Vista previa de la fotografía", Modifier.fillMaxWidth().height(180.dp),
                contentScale = ContentScale.Fit)
            TextButton(onClick = { cambiar(null) }) { Text("Quitar foto") }
        }
    }
    (aviso ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
}

@Composable
fun FotoRemota(ruta: String, store: HuertoStore) {
    var error by remember(ruta) { mutableStateOf(false) }
    val imagen by produceState<ImageBitmap?>(null, ruta, store) {
        try {
            val bytes = store.foto(ruta)
            value = bytes?.let { withContext(Dispatchers.Default) { BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap() } }
            error = value == null
        } catch (ex: CancellationException) {
            throw ex
        } catch (_: Exception) {
            error = true
        }
    }
    imagen?.let { Image(it, "Fotografía adjunta", Modifier.fillMaxWidth().height(185.dp), contentScale = ContentScale.Fit) }
    if (error) Text("La fotografía no está disponible.", style = MaterialTheme.typography.bodySmall)
}

fun etiqueta(valor: String) = valor.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
