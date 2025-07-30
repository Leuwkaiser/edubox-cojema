package com.example.buzondesugerenciascojema.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await

data class RankingEntry(
    val nombreUsuario: String?,
    val puntuacion: Int,
    val emailUsuario: String?
) {
    constructor() : this(null, 0, null)
}

class RankingService {
    private val db = FirebaseFirestore.getInstance()
    private val collection = "rankings"

    suspend fun guardarPuntuacion(
        juego: String,
        puntuacion: Int,
        nombreUsuario: String,
        emailUsuario: String
    ): Boolean {
        return try {
            println("DEBUG: Guardando puntuación - Juego: $juego, Puntuación: $puntuacion, Usuario: $nombreUsuario")
            
            val rankingEntry = RankingEntry(
                nombreUsuario = nombreUsuario,
                puntuacion = puntuacion,
                emailUsuario = emailUsuario
            )
            
            // Crear el documento con timestamp correcto para Firestore
            val documentData = mapOf(
                "juego" to juego,
                "puntuacion" to puntuacion,
                "nombreUsuario" to nombreUsuario,
                "emailUsuario" to emailUsuario,
                "fecha" to Timestamp.now()
            )
            
            val result = db.collection(collection)
                .add(documentData)
                .await()
            
            println("DEBUG: Puntuación guardada exitosamente con ID: ${result.id}")
            true
        } catch (e: Exception) {
            println("DEBUG: Error al guardar puntuación: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerTopRanking(juego: String, limite: Int = 3): List<RankingEntry> {
        return try {
            println("DEBUG: Obteniendo top ranking para juego: $juego, límite: $limite")
            println("DEBUG: Conectando a Firestore...")
            
            val snapshot = db.collection(collection)
                .whereEqualTo("juego", juego)
                .orderBy("puntuacion", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()

            println("DEBUG: Snapshot obtenido, documentos: ${snapshot.documents.size}")
            
            val rankings = snapshot.documents.mapNotNull { doc ->
                val ranking = doc.toObject(RankingEntry::class.java)
                println("DEBUG: Documento ${doc.id}: ${ranking?.nombreUsuario} - ${ranking?.puntuacion}")
                ranking
            }
            
            println("DEBUG: Top ranking obtenido: ${rankings.size} entradas")
            rankings.forEach { ranking ->
                println("DEBUG: - ${ranking.nombreUsuario}: ${ranking.puntuacion}")
            }
            
            rankings
        } catch (e: Exception) {
            println("DEBUG: Error al obtener ranking: ${e.message}")
            println("DEBUG: Tipo de error: ${e.javaClass.simpleName}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerPuntuacionUsuario(juego: String, emailUsuario: String): Int {
        return try {
            val snapshot = db.collection(collection)
                .whereEqualTo("juego", juego)
                .whereEqualTo("emailUsuario", emailUsuario)
                .orderBy("puntuacion", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .await()

            snapshot.documents.firstOrNull()?.toObject(RankingEntry::class.java)?.puntuacion ?: 0
        } catch (e: Exception) {
            println("Error al obtener puntuación del usuario: ${e.message}")
            0
        }
    }

    suspend fun esNuevoRecord(juego: String, puntuacion: Int): Boolean {
        return try {
            println("DEBUG: Verificando si $puntuacion es nuevo récord para $juego")
            val topRanking = obtenerTopRanking(juego, 1)
            
            val esNuevoRecord = topRanking.isEmpty() || puntuacion > topRanking.first().puntuacion
            println("DEBUG: ¿Es nuevo récord? $esNuevoRecord (Top actual: ${topRanking.firstOrNull()?.puntuacion ?: "ninguno"})")
            
            esNuevoRecord
        } catch (e: Exception) {
            println("DEBUG: Error al verificar si es nuevo récord: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerPosicionUsuario(juego: String, emailUsuario: String): Int {
        return try {
            val snapshot = db.collection(collection)
                .whereEqualTo("juego", juego)
                .orderBy("puntuacion", Query.Direction.DESCENDING)
                .get()
                .await()

            val rankings = snapshot.documents.mapNotNull { doc ->
                doc.toObject(RankingEntry::class.java)
            }

            val posicion = rankings.indexOfFirst { it.emailUsuario == emailUsuario }
            if (posicion >= 0) posicion + 1 else -1
        } catch (e: Exception) {
            println("Error al obtener posición del usuario: ${e.message}")
            -1
        }
    }
    
    // Función de prueba para verificar la conexión
    suspend fun probarConexion(): Boolean {
        return try {
            println("DEBUG: Probando conexión con Firestore...")
            val snapshot = db.collection(collection).limit(1).get().await()
            println("DEBUG: Conexión exitosa. Colección accesible.")
            true
        } catch (e: Exception) {
            println("DEBUG: Error de conexión: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    // Función para agregar datos de prueba
    suspend fun agregarDatosPrueba(): Boolean {
        return try {
            println("DEBUG: Agregando datos de prueba...")
            
            val juegos = listOf("snake", "bubble_shooter", "space_invaders")
            val nombres = listOf("Jugador1", "Jugador2", "Jugador3", "Jugador4", "Jugador5")
            val puntuaciones = listOf(100, 85, 72, 65, 50)
            
            for (juego in juegos) {
                for (i in 0 until 3) {
                    val documentData = mapOf(
                        "juego" to juego,
                        "puntuacion" to puntuaciones[i],
                        "nombreUsuario" to nombres[i],
                        "emailUsuario" to "${nombres[i].lowercase()}@test.com",
                        "fecha" to Timestamp.now()
                    )
                    
                    db.collection(collection).add(documentData).await()
                    println("DEBUG: Dato de prueba agregado para $juego: ${nombres[i]} - ${puntuaciones[i]}")
                }
            }
            
            println("DEBUG: Datos de prueba agregados exitosamente")
            true
        } catch (e: Exception) {
            println("DEBUG: Error al agregar datos de prueba: ${e.message}")
            e.printStackTrace()
            false
        }
    }
} 