package com.example.buzondesugerenciascojema.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzondesugerenciascojema.data.PushNotificationService
import com.example.buzondesugerenciascojema.data.TipoNotificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PushNotificationState(
    val isLoading: Boolean = false,
    val mensaje: String? = null,
    val esExitoso: Boolean = false,
    val estadisticas: Map<String, Any> = emptyMap(),
    val usuariosEnviados: Int = 0
)

class PushNotificationViewModel : ViewModel() {
    private val pushNotificationService = PushNotificationService()
    
    private val _state = MutableStateFlow(PushNotificationState())
    val state: StateFlow<PushNotificationState> = _state.asStateFlow()

    /**
     * Envía notificación push a un usuario específico
     */
    fun enviarNotificacionPush(
        emailDestinatario: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, mensaje = null, esExitoso = false)
            
            try {
                val resultado = pushNotificationService.enviarNotificacionPush(
                    emailDestinatario = emailDestinatario,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipo,
                    datosAdicionales = datosAdicionales
                )
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = resultado,
                    mensaje = if (resultado) "Notificación enviada exitosamente" else "Error al enviar notificación"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = false,
                    mensaje = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Envía notificación push a un grupo específico
     */
    fun enviarNotificacionPushAGrupo(
        grado: String,
        grupo: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, mensaje = null, esExitoso = false)
            
            try {
                val usuariosEnviados = pushNotificationService.enviarNotificacionPushAGrupo(
                    grado = grado,
                    grupo = grupo,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipo,
                    datosAdicionales = datosAdicionales
                )
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = usuariosEnviados > 0,
                    usuariosEnviados = usuariosEnviados,
                    mensaje = "Notificación enviada a $usuariosEnviados usuarios del grupo $grado-$grupo"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = false,
                    mensaje = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Envía notificación push a todos los usuarios
     */
    fun enviarNotificacionPushATodos(
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, mensaje = null, esExitoso = false)
            
            try {
                val usuariosEnviados = pushNotificationService.enviarNotificacionPushATodos(
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipo,
                    datosAdicionales = datosAdicionales
                )
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = usuariosEnviados > 0,
                    usuariosEnviados = usuariosEnviados,
                    mensaje = "Notificación enviada a $usuariosEnviados usuarios"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = false,
                    mensaje = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Envía notificación push a usuarios por rol
     */
    fun enviarNotificacionPushPorRol(
        rol: String,
        titulo: String,
        mensaje: String,
        tipo: TipoNotificacion = TipoNotificacion.PUSH,
        datosAdicionales: Map<String, String> = emptyMap()
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, mensaje = null, esExitoso = false)
            
            try {
                val usuariosEnviados = pushNotificationService.enviarNotificacionPushPorRol(
                    rol = rol,
                    titulo = titulo,
                    mensaje = mensaje,
                    tipo = tipo,
                    datosAdicionales = datosAdicionales
                )
                
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = usuariosEnviados > 0,
                    usuariosEnviados = usuariosEnviados,
                    mensaje = "Notificación enviada a $usuariosEnviados usuarios con rol $rol"
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    esExitoso = false,
                    mensaje = "Error: ${e.message}"
                )
            }
        }
    }

    /**
     * Registra el token FCM de un usuario
     */
    fun registrarTokenFCM(email: String, token: String) {
        viewModelScope.launch {
            try {
                val resultado = pushNotificationService.registrarTokenFCM(email, token)
                if (resultado) {
                    println("Token FCM registrado exitosamente para: $email")
                } else {
                    println("Error al registrar token FCM para: $email")
                }
            } catch (e: Exception) {
                println("Error al registrar token FCM: ${e.message}")
            }
        }
    }

    /**
     * Obtiene estadísticas de tokens FCM
     */
    fun obtenerEstadisticasTokens() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            try {
                val estadisticas = pushNotificationService.obtenerEstadisticasTokens()
                _state.value = _state.value.copy(
                    isLoading = false,
                    estadisticas = estadisticas
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    mensaje = "Error al obtener estadísticas: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia el estado de mensajes
     */
    fun limpiarMensaje() {
        _state.value = _state.value.copy(mensaje = null, esExitoso = false)
    }

    /**
     * Resetea el estado completo
     */
    fun resetearEstado() {
        _state.value = PushNotificationState()
    }
} 