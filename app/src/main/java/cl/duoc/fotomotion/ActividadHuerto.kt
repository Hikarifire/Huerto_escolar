package cl.duoc.fotomotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import cl.duoc.fotomotion.ui.navigation.RecorridoHuerto

class ActividadHuerto : ComponentActivity() {
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onCreate(estadoGuardado: Bundle?) {
        super.onCreate(estadoGuardado)
        enableEdgeToEdge()
        setContent {
            val paletaHuerto = lightColorScheme(
                primary = Color(0xFF2E6F40),
                onPrimary = Color(0xFFFFFFFF),
                primaryContainer = Color(0xFFDCECC9),
                onPrimaryContainer = Color(0xFF112211),
                secondary = Color(0xFF8D6E63),
                onSecondary = Color(0xFFFFFFFF),
                secondaryContainer = Color(0xFFF5EBE0),
                onSecondaryContainer = Color(0xFF3E2723),
                tertiary = Color(0xFF388E3C),
                background = Color(0xFFF8F9FA),
                surface = Color(0xFFFFFFFF),
                surfaceVariant = Color(0xFFEEF2ED),
                onSurface = Color(0xFF1C1D1B),
                onSurfaceVariant = Color(0xFF444843),
                outline = Color(0xFF757973),
            )
            MaterialTheme(colorScheme = paletaHuerto) {
                RecorridoHuerto()
            }
        }
    }
}
