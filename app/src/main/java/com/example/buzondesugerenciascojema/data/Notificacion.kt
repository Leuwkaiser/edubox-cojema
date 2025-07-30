package com.example.buzondesugerenciascojema.data

import java.util.Date

enum class TipoNotificacion {
    NUEVO_LIBRO,
    SUGERENCIA_APROBADA,
    SUGERENCIA_DESAPROBADA,
    NUEVO_EVENTO,
    SUGERENCIA_PENDIENTE,
    SUGERENCIA_INNAPROPIADA,
    SUGERENCIA,
    BIBLIOTECA,
    EVENTO,
    PUSH,
    DOCUMENTO,
    LOGRO
}

data class Notificacion(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "NUEVO_LIBRO",
    val fecha: Date = Date(),
    val leida: Boolean = false,
    val destinatarioEmail: String = "",
    val esAdmin: Boolean = false
) 