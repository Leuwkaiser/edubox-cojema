package com.example.buzondesugerenciascojema.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzondesugerenciascojema.data.Notificacion
import com.example.buzondesugerenciascojema.data.NotificacionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificacionViewModel : ViewModel() {
    private val notificacionService = NotificacionService()
    
    private val _notificaciones = MutableStateFlow<List<Notificacion>>(emptyList())
    val notificaciones: StateFlow<List<Notificacion>> = _notificaciones.asStateFlow()
    
    private val _cantidadNoLeidas = MutableStateFlow(0)
    val cantidadNoLeidas: StateFlow<Int> = _cantidadNoLeidas.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun cargarNotificaciones(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                Log.d("NotificacionVM", "[DEBUG] Consultando notificaciones para email: '$email'")
                val notificaciones = notificacionService.obtenerNotificaciones(email)
                Log.d("NotificacionVM", "[DEBUG] Notificaciones recibidas: ${notificaciones.size}")
                _notificaciones.value = notificaciones
                
                val noLeidas = notificacionService.obtenerCantidadNoLeidas(email)
                Log.d("NotificacionVM", "[DEBUG] Cantidad de no leídas: $noLeidas")
                _cantidadNoLeidas.value = noLeidas
            } catch (e: Exception) {
                _error.value = "Error al cargar notificaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun marcarComoLeida(notificacionId: String, email: String) {
        viewModelScope.launch {
            try {
                notificacionService.marcarComoLeida(notificacionId)
                
                // Recargar todas las notificaciones para asegurar sincronización
                val notificacionesActualizadas = notificacionService.obtenerNotificaciones(email)
                _notificaciones.value = notificacionesActualizadas
                
                // Actualizar contador de no leídas
                val noLeidas = notificacionService.obtenerCantidadNoLeidas(email)
                _cantidadNoLeidas.value = noLeidas
            } catch (e: Exception) {
                _error.value = "Error al marcar como leída: ${e.message}"
            }
        }
    }

    fun marcarTodasComoLeidas(email: String) {
        viewModelScope.launch {
            try {
                // Marcar todas las notificaciones como leídas una por una
                _notificaciones.value.forEach { notificacion ->
                    if (!notificacion.leida) {
                        notificacionService.marcarComoLeida(notificacion.id)
                    }
                }
                
                // Actualizar la lista local
                val notificacionesActualizadas = _notificaciones.value.map { it.copy(leida = true) }
                _notificaciones.value = notificacionesActualizadas
                
                // Actualizar contador
                _cantidadNoLeidas.value = 0
            } catch (e: Exception) {
                _error.value = "Error al marcar todas como leídas: ${e.message}"
            }
        }
    }

    fun eliminarNotificacion(notificacionId: String, email: String) {
        viewModelScope.launch {
            try {
                notificacionService.eliminarNotificacion(notificacionId)
                
                // Actualizar la lista local
                val notificacionesActualizadas = _notificaciones.value.filter { it.id != notificacionId }
                _notificaciones.value = notificacionesActualizadas
                
                // Actualizar contador
                val noLeidas = notificacionService.obtenerCantidadNoLeidas(email)
                _cantidadNoLeidas.value = noLeidas
            } catch (e: Exception) {
                _error.value = "Error al eliminar notificación: ${e.message}"
            }
        }
    }

    fun actualizarContadorNoLeidas(email: String) {
        viewModelScope.launch {
            try {
                val noLeidas = notificacionService.obtenerCantidadNoLeidas(email)
                _cantidadNoLeidas.value = noLeidas
            } catch (e: Exception) {
                _error.value = "Error al actualizar contador: ${e.message}"
            }
        }
    }

    fun limpiarError() {
        _error.value = null
    }
    
    fun limpiarMensajeExito() {
        _successMessage.value = null
    }

    fun borrarTodasLasNotificaciones() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                // Obtener el email del usuario actual para borrar sus notificaciones
                val email = _notificaciones.value.firstOrNull()?.destinatarioEmail ?: ""
                if (email.isNotEmpty()) {
                    notificacionService.eliminarTodasLasNotificaciones(email)
                }
                
                // Limpiar la lista local después de borrar
                _notificaciones.value = emptyList()
                _cantidadNoLeidas.value = 0
                
                _successMessage.value = "Todas las notificaciones han sido eliminadas"
                println("DEBUG: Notificaciones borradas exitosamente")
            } catch (e: Exception) {
                _error.value = "Error al borrar notificaciones: ${e.message}"
                println("Error al borrar notificaciones: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}