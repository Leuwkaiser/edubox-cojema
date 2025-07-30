package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Usuario(
    var nombreCompleto: String = "",
    var email: String = "",
    var password: String = "",
    var grado: String = "",
    var grupo: String = "",
    var codigoGrado: String = "",
    var fotoPerfil: String = "",
    var esAdmin: Boolean = false
) {
    // Constructor sin argumentos requerido por Firebase
    constructor() : this("", "", "", "", "", "", "", false)
} 