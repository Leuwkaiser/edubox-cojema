package com.example.buzondesugerenciascojema.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.buzondesugerenciascojema.data.DocumentoService
import com.example.buzondesugerenciascojema.data.NotificacionService
import com.example.buzondesugerenciascojema.data.TipoNotificacion
import com.example.buzondesugerenciascojema.data.SimpleNotificationService
import com.example.buzondesugerenciascojema.model.Documento
import com.example.buzondesugerenciascojema.model.Asignatura
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BibliotecaViewModel : ViewModel() {
    private val documentoService = DocumentoService()
    private val notificacionService = NotificacionService()
    private val db = FirebaseFirestore.getInstance()
    private var simpleNotificationService: SimpleNotificationService? = null
    
    fun setContext(context: android.content.Context) {
        simpleNotificationService = SimpleNotificationService(context)
    }
    
    private val _documentos = MutableStateFlow<List<Documento>>(emptyList())
    val documentos: StateFlow<List<Documento>> = _documentos.asStateFlow()
    
    private val _documentoActual = MutableStateFlow<Documento?>(null)
    val documentoActual: StateFlow<Documento?> = _documentoActual.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _asignaturaSeleccionada = MutableStateFlow<Asignatura?>(null)
    val asignaturaSeleccionada: StateFlow<Asignatura?> = _asignaturaSeleccionada.asStateFlow()
    
    private val _gradoSeleccionado = MutableStateFlow<Int?>(null)
    val gradoSeleccionado: StateFlow<Int?> = _gradoSeleccionado.asStateFlow()

    fun cargarDocumentos() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val docs = documentoService.obtenerDocumentos()
                _documentos.value = docs
            } catch (e: Exception) {
                _error.value = "Error al cargar documentos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarDocumentosPorAsignatura(asignatura: Asignatura) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _asignaturaSeleccionada.value = asignatura
                val docs = documentoService.obtenerDocumentosPorAsignatura(asignatura.nombre)
                _documentos.value = docs
            } catch (e: Exception) {
                _error.value = "Error al cargar documentos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarDocumentosPorGrado(grado: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _gradoSeleccionado.value = grado
                val docs = documentoService.obtenerDocumentosPorGrado(grado)
                _documentos.value = docs
            } catch (e: Exception) {
                _error.value = "Error al cargar documentos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun cargarDocumentosPorAsignaturaYGrado(asignatura: Asignatura, grado: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                _asignaturaSeleccionada.value = asignatura
                _gradoSeleccionado.value = grado
                val docs = documentoService.obtenerDocumentosPorAsignaturaYGrado(asignatura.nombre, grado)
                _documentos.value = docs
            } catch (e: Exception) {
                _error.value = "Error al cargar documentos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun agregarDocumento(
        titulo: String,
        descripcion: String,
        asignatura: String,
        grado: Int,
        url: String,
        nombreArchivo: String,
        subidoPor: String,
        tipoArchivo: String = "application/octet-stream",
        portadaUrl: String = "",
        portadaDrawable: String = ""
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                println("DEBUG: ViewModel - Iniciando agregación de documento...")
                println("DEBUG: ViewModel - Título: $titulo")
                println("DEBUG: ViewModel - Asignatura: $asignatura")
                println("DEBUG: ViewModel - Grado: $grado")
                println("DEBUG: ViewModel - Portada URL: $portadaUrl")
                println("DEBUG: ViewModel - Portada Drawable: $portadaDrawable")
                
                val result = documentoService.agregarDocumento(
                    titulo, descripcion, asignatura, grado, url, nombreArchivo, subidoPor, tipoArchivo, portadaUrl, portadaDrawable
                )
                result.fold(
                    onSuccess = { documentId ->
                        println("DEBUG: ViewModel - Documento agregado exitosamente: $documentId")
                        
                        // Enviar notificación automática sobre nuevo documento
                        try {
                            // Obtener todos los usuarios para enviar notificación
                            val todosUsuarios = db.collection("usuarios").get().await()
                            
                            // Crear notificación en Firestore para cada usuario
                            for (doc in todosUsuarios.documents) {
                                val email = doc.getString("email")
                                if (!email.isNullOrEmpty()) {
                                    notificacionService.crearNotificacion(
                                        titulo = "📚 Nuevo documento disponible",
                                        mensaje = "$titulo ha sido agregado a la biblioteca por $subidoPor",
                                        tipo = TipoNotificacion.BIBLIOTECA,
                                        destinatarioEmail = email,
                                        leida = false
                                    )
                                }
                            }
                            
                            println("✅ Notificaciones creadas en Firestore para ${todosUsuarios.size()} usuarios")
                        } catch (e: Exception) {
                            println("❌ Error al enviar notificación automática: ${e.message}")
                        }
                        
                        cargarDocumentos()
                    },
                    onFailure = { exception ->
                        println("DEBUG: ViewModel - Error al agregar documento: ${exception.message}")
                        _error.value = exception.message ?: "Error desconocido al agregar documento"
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: ViewModel - Excepción inesperada: ${e.message}")
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun eliminarDocumento(documentoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = documentoService.eliminarDocumento(documentoId)
                result.fold(
                    onSuccess = {
                        cargarDocumentos()
                    },
                    onFailure = { exception ->
                        _error.value = "Error al eliminar documento: ${exception.message}"
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun limpiarFiltros() {
        _asignaturaSeleccionada.value = null
        _gradoSeleccionado.value = null
        cargarDocumentos()
    }

    fun limpiarError() {
        _error.value = null
    }

    fun cargarDocumentoPorId(documentoId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val result = documentoService.obtenerDocumentoPorId(documentoId)
                result.fold(
                    onSuccess = { documento ->
                        _documentoActual.value = documento
                    },
                    onFailure = { exception ->
                        _error.value = exception.message ?: "Error al cargar el documento"
                    }
                )
            } catch (e: Exception) {
                _error.value = "Error inesperado: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}