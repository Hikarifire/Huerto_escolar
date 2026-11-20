package cl.duoc.fotomotion.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.fotomotion.data.RepositorioHuerto
import cl.duoc.fotomotion.domain.HuertoStore
import kotlinx.coroutines.launch

class HuertoViewModel(app: Application) : AndroidViewModel(app) {
    val store = HuertoStore(RepositorioHuerto(app))

    init {
        viewModelScope.launch { store.restaurar() }
    }
}
