package com.example.buzondesugerenciascojema.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzondesugerenciascojema.data.SugerenciaService
import com.example.buzondesugerenciascojema.model.Sugerencia
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {
    private val sugerenciaService = SugerenciaService()
    
    private val _sugerencias = MutableStateFlow<List<Sugerencia>>(emptyList())
    val sugerencias: StateFlow<List<Sugerencia>> = _sugerencias
    
    fun cargarSugerencias(grado: String, grupo: String) {
        viewModelScope.launch {
            val sugerencias = sugerenciaService.obtenerSugerenciasPorGradoYGrupo(grado, grupo)
            _sugerencias.value = sugerencias
        }
    }
    
    fun editarSugerencia(id: String, titulo: String, contenido: String, usuarioId: String) {
        viewModelScope.launch {
            sugerenciaService.editarSugerencia(id, titulo, contenido, usuarioId)
            // Recargar sugerencias después de editar
            cargarSugerencias("", "")
        }
    }
    
    fun borrarSugerencia(id: String, usuarioId: String) {
        viewModelScope.launch {
            sugerenciaService.borrarSugerencia(id, usuarioId)
            // Recargar sugerencias después de borrar
            cargarSugerencias("", "")
        }
    }
    
    fun votarSugerencia(sugerenciaId: String, usuarioId: String, esLike: Boolean) {
        viewModelScope.launch {
            sugerenciaService.votarSugerencia(sugerenciaId, usuarioId, esLike)
            // Recargar sugerencias después de votar
            cargarSugerencias("", "")
        }
    }
} 