package com.example.buzondesugerenciascojema

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

fun main() = runBlocking {
    val db = FirebaseFirestore.getInstance()
    val sugerenciasCollection = db.collection("sugerencias")
    
    try {
        val batch = db.batch()
        val sugerencias = sugerenciasCollection.get().await()
        
        sugerencias.documents.forEach { document ->
            batch.delete(document.reference)
        }
        
        batch.commit().await()
        println("Todas las sugerencias han sido borradas exitosamente")
    } catch (e: Exception) {
        println("Error al borrar las sugerencias: ${e.message}")
    }
} 