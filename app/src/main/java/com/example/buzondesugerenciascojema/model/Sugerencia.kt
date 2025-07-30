package com.example.buzondesugerenciascojema.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Sugerencia(
    var id: String = "",
    var titulo: String = "",
    var contenido: String = "",
    var grado: String = "",
    var grupo: String = "",
    var fecha: Date = Date(),
    var autorId: String = "",
    var autorNombre: String = "",
    var votosPositivos: Int = 0,
    var votosNegativos: Int = 0,
    var estado: String = "Pendiente", // Pendiente, Aprobada, Rechazada
    var likes: List<String> = emptyList(),
    var dislikes: List<String> = emptyList(),
    var comentarios: List<Comentario> = emptyList(),
    var comentariosLeidosPor: List<String> = emptyList()
) {
    constructor() : this("", "", "", "", "", Date(), "", "", 0, 0, "Pendiente", emptyList(), emptyList(), emptyList(), emptyList())

    @Exclude
    fun getTotalLikes(): Int = likes.size

    @Exclude
    fun getTotalDislikes(): Int = dislikes.size
}

@IgnoreExtraProperties
data class Comentario(
    var id: String = "",
    var contenido: String = "",
    var autorId: String = "",
    var autorNombre: String = "",
    var fecha: Date = Date()
) {
    constructor() : this("", "", "", "", Date())
}

// Clase para el resultado de validación de sugerencias
data class SugerenciaValidationResult(
    val esValida: Boolean,
    val motivo: String?
)

// Clase para el resultado del análisis de sentimiento
data class SentimientoResult(
    val esMuyNegativo: Boolean,
    val esPocoConstructivo: Boolean,
    val esMuyVago: Boolean = false,
    val esPocoEspecifico: Boolean = false,
    val esDemasiadoCorta: Boolean = false
)

// Clase para estadísticas de sugerencias
data class SugerenciaEstadisticas(
    val total: Int = 0,
    val aprobadas: Int = 0,
    val rechazadas: Int = 0,
    val pendientes: Int = 0,
    val porcentajeAprobacion: Float = 0f
)

// Clase para sugerencias rechazadas con información adicional
data class SugerenciaRechazada(
    val id: String,
    val titulo: String,
    val contenido: String,
    val autorNombre: String,
    val fecha: Date,
    val motivoRechazo: String
)

// Clase para reporte de contenido problemático
data class ReporteContenidoProblematico(
    val totalSugerenciasRechazadas: Int = 0,
    val topAutoresProblematicos: List<String> = emptyList(),
    val topMotivosRechazo: List<String> = emptyList(),
    val fechaGeneracion: Date = Date()
) 