package com.example.buzondesugerenciascojema.model

import com.google.firebase.firestore.IgnoreExtraProperties
import java.util.Date

@IgnoreExtraProperties
data class Evento(
    var id: String = "",
    var titulo: String = "",
    var descripcion: String = "",
    var fecha: Date = Date(),
    var creadoPor: String = "",
    var notificados: List<String> = emptyList()
) {
    constructor() : this("", "", "", Date(), "", emptyList())
} 