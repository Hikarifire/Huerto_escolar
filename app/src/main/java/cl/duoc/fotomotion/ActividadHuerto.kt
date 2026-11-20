package cl.duoc.fotomotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import cl.duoc.fotomotion.ui.Huerto

class ActividadHuerto : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = HuertoColors) { Huerto() }
        }
    }
}

private val HuertoColors = lightColorScheme(
    primary = Color(0xFF295943), onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE9D6), onPrimaryContainer = Color(0xFF173328),
    secondary = Color(0xFF6A521B), secondaryContainer = Color(0xFFF1E6BF),
    background = Color(0xFFF8F8F0), surface = Color(0xFFFEFDF6),
    surfaceVariant = Color(0xFFE7EBE0), onSurface = Color(0xFF192820),
    onSurfaceVariant = Color(0xFF45544A),
)
