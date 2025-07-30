package com.example.buzondesugerenciascojema.model

data class Documento(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val asignatura: String = "",
    val grado: Int = 0,
    val url: String = "", // Enlace externo (Google Drive, Dropbox, etc.)
    val nombreArchivo: String = "",
    val fechaSubida: Long = 0,
    val subidoPor: String = "",
    val tipoArchivo: String = "",
    val esEnlaceExterno: Boolean = true, // Indica si es un enlace externo
    val portadaUrl: String = "", // Enlace público de la portada (Drive)
    val portadaDrawable: String = "" // Nombre del recurso drawable local (ej: "libro_matematicas")
)

enum class Asignatura(val nombre: String) {
    CIENCIAS_SOCIALES("Ciencias Sociales"),
    CIENCIAS_NATURALES("Ciencias Naturales"),
    MATEMATICAS("Matemáticas"),
    CASTELLANO("Castellano"),
    INGLES("Inglés"),
    OBRAS_LITERARIAS("Obras Literarias")
}

object GradoConstants {
    val GRADOS = listOf(6, 7, 8, 9, 10, 11)
}

// Portadas disponibles en la aplicación
object PortadasDisponibles {
    val PORTADAS = mapOf(
        "libro" to "Libro por defecto",
        "logo_cojema" to "Logo COJEMA",
        "logoia" to "Logo IA",
        "gamepad" to "Icono de juego",
        "calendario" to "Icono de calendario",
        "buzon" to "Icono de buzón",
        "snake" to "Icono de serpiente",
        "logro" to "Icono de logro"
    )
    
    // Obtener nombre amigable de la portada
    fun getNombreAmigable(drawableName: String): String {
        return PORTADAS[drawableName] ?: "Portada personalizada"
    }
    
    // Obtener lista de nombres amigables
    fun getNombresAmigables(): List<String> {
        return PORTADAS.values.toList()
    }
    
    // Obtener nombre del drawable por nombre amigable
    fun getDrawableName(nombreAmigable: String): String? {
        return PORTADAS.entries.find { it.value == nombreAmigable }?.key
    }
} 