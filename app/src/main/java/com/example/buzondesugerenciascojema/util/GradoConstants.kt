package com.example.buzondesugerenciascojema.util

object GradoConstants {
    const val SEXTO = "sexto"
    const val SEPTIMO = "septimo"
    const val OCTAVO = "octavo"
    const val NOVENO = "noveno"
    const val DECIMO = "decimo"
    const val ONCE = "once"

    val GRADOS = listOf(
        SEXTO,
        SEPTIMO,
        OCTAVO,
        NOVENO,
        DECIMO,
        ONCE
    )

    val GRUPOS = listOf("1", "2", "3")
    
    // Códigos alfanuméricos únicos por grado y grupo
    val CODIGOS_GRADO_GRUPO = mapOf(
        SEXTO to mapOf(
            "1" to "6X7K9P2M5N",
            "2" to "6Y8L0Q3R4S",
            "3" to "6Z9M1T4U5V"
        ),
        SEPTIMO to mapOf(
            "1" to "7A0N2U5V6W",
            "2" to "7B1O3W6X7Y",
            "3" to "7C2P4X7Y8Z"
        ),
        OCTAVO to mapOf(
            "1" to "8D3Q5Y8Z9A",
            "2" to "8E4R6Z9A0B",
            "3" to "8F5S7A0B1C"
        ),
        NOVENO to mapOf(
            "1" to "9G6T8B1C2D",
            "2" to "9H7U9C2D3E",
            "3" to "9I8V0D3E4F"
        ),
        DECIMO to mapOf(
            "1" to "10J9W1E4F5G",
            "2" to "10K0X2F5G6H",
            "3" to "10L1Y3G6H7I"
        ),
        ONCE to mapOf(
            "1" to "11M2Z4H7I8J",
            "2" to "11N3A5I8J9K",
            "3" to "11O4B6J9K0L"
        )
    )

    // Códigos de administrador únicos por grado y grupo
    private val CODIGOS_ADMIN = mapOf(
        SEXTO to mapOf(
            "1" to "ADM6X7K9P2M5N",
            "2" to "ADM6Y8L0Q3R4S",
            "3" to "ADM6Z9M1T4U5V"
        ),
        SEPTIMO to mapOf(
            "1" to "ADM7A0N2U5V6W",
            "2" to "ADM7B1O3W6X7Y",
            "3" to "ADM7C2P4X7Y8Z"
        ),
        OCTAVO to mapOf(
            "1" to "ADM8D3Q5Y8Z9A",
            "2" to "ADM8E4R6Z9A0B",
            "3" to "ADM8F5S7A0B1C"
        ),
        NOVENO to mapOf(
            "1" to "ADM9G6T8B1C2D",
            "2" to "ADM9H7U9C2D3E",
            "3" to "ADM9I8V0D3E4F"
        ),
        DECIMO to mapOf(
            "1" to "ADM10J9W1E4F5G",
            "2" to "ADM10K0X2F5G6H",
            "3" to "ADM10L1Y3G6H7I"
        ),
        ONCE to mapOf(
            "1" to "ADM11M2Z4H7I8J",
            "2" to "ADM11N3A5I8J9K",
            "3" to "ADM11O4B6J9K0L"
        )
    )

    fun getCodigoGrado(grado: String): String {
        return when (grado.lowercase()) {
            SEXTO -> "6X7K9"
            SEPTIMO -> "7P2M5"
            OCTAVO -> "8N4L6"
            NOVENO -> "9Q8R3"
            DECIMO -> "10T5V7"
            ONCE -> "11W9Y2"
            else -> ""
        }
    }

    fun getGradoFromCodigo(codigo: String): String {
        return when (codigo) {
            "6X7K9" -> SEXTO
            "7P2M5" -> SEPTIMO
            "8N4L6" -> OCTAVO
            "9Q8R3" -> NOVENO
            "10T5V7" -> DECIMO
            "11W9Y2" -> ONCE
            else -> ""
        }
    }

    fun getGradoCompleto(grado: String, grupo: String): String {
        return "$grado $grupo"
    }

    fun getCodigoCompleto(grado: String, grupo: String): String {
        return CODIGOS_GRADO_GRUPO[grado.lowercase()]?.get(grupo) ?: ""
    }

    fun getCodigoAdmin(grado: String, grupo: String): String {
        return CODIGOS_ADMIN[grado.lowercase()]?.get(grupo) ?: ""
    }

    fun validarCodigoAdmin(grado: String, grupo: String, codigo: String): Boolean {
        return getCodigoAdmin(grado, grupo) == codigo
    }

    fun validarCodigoGradoGrupo(grado: String, grupo: String, codigo: String): Boolean {
        return CODIGOS_GRADO_GRUPO[grado.lowercase()]?.get(grupo) == codigo
    }
} 