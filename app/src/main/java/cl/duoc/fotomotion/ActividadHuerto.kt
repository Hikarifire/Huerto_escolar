package cl.duoc.fotomotion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import cl.duoc.fotomotion.ui.RecorridoHuerto

class ActividadHuerto : ComponentActivity() {
    // onCreate mantiene su nombre porque Android lo exige.
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun onCreate(estadoGuardado: Bundle?) {
        super.onCreate(estadoGuardado)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF397B72),
                secondary = Color(0xFFC28B57), background = Color(0xFFF7FAF8))) {
                RecorridoHuerto()
            }
        }
    }
}
