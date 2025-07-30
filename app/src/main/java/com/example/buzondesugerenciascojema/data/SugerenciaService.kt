package com.example.buzondesugerenciascojema.data

import com.example.buzondesugerenciascojema.model.Sugerencia
import com.example.buzondesugerenciascojema.model.Comentario
import com.example.buzondesugerenciascojema.model.SugerenciaValidationResult
import com.example.buzondesugerenciascojema.model.SentimientoResult
import com.example.buzondesugerenciascojema.model.SugerenciaEstadisticas
import com.example.buzondesugerenciascojema.model.SugerenciaRechazada
import com.example.buzondesugerenciascojema.model.ReporteContenidoProblematico
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.example.buzondesugerenciascojema.data.SimpleNotificationService

class SugerenciaService {
    private val db = FirebaseFirestore.getInstance()
    private val sugerenciasCollection = db.collection("sugerencias")
    private val usuariosCollection = db.collection("usuarios")
    private val notificacionService = NotificacionService()
    private var simpleNotificationService: SimpleNotificationService? = null
    
    fun setContext(context: android.content.Context) {
        simpleNotificationService = SimpleNotificationService(context)
    }

    // Sistema de filtrado automático
    private val palabrasInapropiadas = setOf(
        // Groserías comunes
        "puta", "hijo de puta", "hdp", "pendejo", "pendeja", "pendejos", "pendejas",
        "cabrón", "cabrona", "cabrones", "cabronas", "chinga", "chingada", "chingado",
        "verga", "vergas", "pito", "pitos", "coño", "coños", "carajo", "carajos",
        "mierda", "mierdas", "cagada", "cagadas", "cagar", "cagando", "cagó",
        "joder", "jodido", "jodida", "jodidos", "jodidas", "jodete", "jódete",
        "malparido", "malparida", "malparidos", "malparidas", "hijueputa", "hijueputas",
        "gonorrea", "gonorreas", "marica", "maricas", "maricón", "maricones",
        "güevón", "güevona", "güevones", "güevonas", "guevón", "guevona", "guevones", "guevonas",
        "hpta", "hptas", "pt", "pts", "pvt", "pvts", "pvtas", "pvtos",
        
        // Palabras ofensivas
        "idiota", "idiotas", "estúpido", "estúpida", "estúpidos", "estúpidas",
        "imbécil", "imbéciles", "tonto", "tonta", "tontos", "tontas",
        "bobo", "boba", "bobos", "bobas", "gilipollas", "gilipollas",
        "mamón", "mamona", "mamones", "mamonas", "mamada", "mamadas",
        
        // Expresiones vulgares
        "que te jodan", "que te joda", "que se joda", "que se jodan",
        "vete a la mierda", "vete al carajo", "vete al coño", "vete al diablo",
        "me cago en", "me cago en la", "me cago en el", "me cago en tu",
        "que te den", "que te den por", "que te den por el", "que te den por la",
        
        // Palabras relacionadas con contenido sexual inapropiado
        "pornografía", "porno", "xxx", "sexo", "sexual", "coito", "penetración",
        "masturbación", "masturbar", "follar", "follando", "follada", "folladas",
        
        // Palabras relacionadas con violencia
        "matar", "matando", "matado", "matada", "asesinar", "asesinando",
        "golpear", "golpeando", "pegar", "pegando", "pelearse", "peleando",
        "violencia", "violento", "violenta", "agresión", "agresivo", "agresiva",
        
        // INSULTOS COSTEÑOS Y COLOMBIANOS
        // Insultos costeños específicos
        "pichurria", "pirobo", "zunga", "sapo hijueputa", "huevon", "huevona", "huevones", "huevonas",
        "lampara", "ñero", "ñera", "ñeros", "ñeras", "gurrupleto", "gurrupleta", "gurrupletos", "gurrupletas",
        "garbimba", "care monda", "care chimba", "tontarron", "tontarrona", "tontarrones", "tontarronas",
        "lambon", "lambona", "lambones", "lambonas", "cacorro", "cacorra", "cacorros", "cacorras",
        "jueputa", "jueputas", "malparido", "malparida", "malparidos", "malparidas",
        
        // Palabras con "monda" (como solicitaste)
        "monda", "mondas", "mondón", "mondona", "mondones", "mondonas",
        "mondero", "mondera", "monderos", "monderas", "mondear", "mondeando",
        "mondazo", "mondazos", "mondadera", "mondaderas", "mondadero", "mondaderos",
        
        // Variaciones y combinaciones comunes
        "huevon hijueputa", "pirobo hijueputa", "sapo hijueputa", "care monda hijueputa",
        "mucho huevon", "muy huevon", "es un huevon", "esa huevona",
        "mucho pirobo", "muy pirobo", "es un pirobo", "esa piroba",
        "mucho sapo", "muy sapo", "es un sapo", "esa sapa",
        "mucho lambon", "muy lambon", "es un lambon", "esa lambona",
        "mucho cacorro", "muy cacorro", "es un cacorro", "esa cacorra",
        "mucho ñero", "muy ñero", "es un ñero", "esa ñera",
        "mucho gurrupleto", "muy gurrupleto", "es un gurrupleto", "esa gurrupleta",
        "mucho garbimba", "muy garbimba", "es un garbimba", "esa garbimba",
        "mucho tontarron", "muy tontarron", "es un tontarron", "esa tontarrona",
        "mucho lampara", "muy lampara", "es una lampara", "esa lampara"
    )

    private val frasesInapropiadas = setOf(
        "por joder", "para joder", "para molestar", "para fastidiar",
        "no tiene sentido", "sin sentido", "tontería", "tonterías",
        "broma", "bromas", "chiste", "chistes", "gracia", "gracias",
        "lol", "lmao", "rofl", "haha", "jaja", "jeje", "jiji",
        "test", "prueba", "experimento", "experimental",
        "hola", "buenos días", "buenas tardes", "buenas noches",
        "como estás", "qué tal", "saludos", "saludo",
        "nada", "ninguna", "ningún", "no sé", "no se", "no tengo",
        "no hay", "no existe", "no hay nada", "todo bien", "todo está bien",
        "está bien", "está todo bien", "no hay problema", "sin problema",
        "no hay nada que mejorar", "todo perfecto", "está perfecto",
        "no tengo sugerencias", "no tengo ideas", "no tengo propuestas",
        "no tengo nada que decir", "no tengo comentarios", "sin comentarios"
    )

    // Palabras que indican contenido constructivo y educativo
    private val palabrasConstructivas = setOf(
        "mejorar", "mejora", "mejoras", "sugerencia", "sugerencias", "propuesta", "propuestas",
        "recomendación", "recomendaciones", "idea", "ideas", "solución", "soluciones",
        "implementar", "implementación", "cambiar", "cambio", "cambios", "agregar", "agregue",
        "necesitamos", "necesita", "necesito", "faltan", "falta", "debería", "deberían",
        "sería", "serían", "podría", "podrían", "sugiero", "sugerimos", "proponemos",
        "considerar", "consideración", "evaluar", "evaluación", "revisar", "revisión",
        "actualizar", "actualización", "modernizar", "modernización", "optimizar", "optimización",
        "facilitar", "facilitación", "simplificar", "simplificación", "organizar", "organización",
        "estructurar", "estructuración", "planificar", "planificación", "desarrollar", "desarrollo",
        "crear", "creación", "establecer", "establecimiento", "implementar", "implementación",
        "instalar", "instalación", "renovar", "renovación", "ampliar", "ampliación",
        "expandir", "expansión", "mejorar", "mejora", "optimizar", "optimización",
        "proponer", "propongo", "sugerir", "recomendar", "considerar", "evaluar",
        "analizar", "estudiar", "investigar", "examinar", "revisar", "corregir",
        "arreglar", "solucionar", "resolver", "mejorar", "perfeccionar", "optimizar",
        "eficientizar", "eficientar", "agilizar", "acelerar", "simplificar", "facilitar",
        "ayudar", "apoyar", "beneficiar", "favorecer", "promover", "fomentar",
        "desarrollar", "crear", "establecer", "implementar", "aplicar", "ejecutar",
        "realizar", "llevar", "hacer", "construir", "edificar", "formar", "organizar",
        "estructurar", "sistematizar", "planificar", "programar", "diseñar", "elaborar",
        "preparar", "configurar", "ajustar", "adaptar", "modificar", "transformar",
        "renovar", "actualizar", "modernizar", "innovar", "inventar", "descubrir",
        "encontrar", "identificar", "detectar", "localizar", "ubicar", "situar",
        "colocar", "posicionar", "instalar", "montar", "ensamblar", "conectar",
        "integrar", "unificar", "combinar", "fusionar", "mezclar", "juntar",
        "agrupar", "clasificar", "categorizar", "ordenar", "organizar", "sistematizar",
        "estandarizar", "normalizar", "regularizar", "formalizar", "institucionalizar",
        "establecer", "fundar", "crear", "constituir", "formar", "conformar",
        "componer", "integrar", "incluir", "incorporar", "agregar", "añadir",
        "sumar", "adicionar", "complementar", "completar", "terminar", "finalizar",
        "concluir", "acabar", "culminar", "lograr", "alcanzar", "conseguir",
        "obtener", "adquirir", "ganar", "lograr", "triunfar", "éxito", "éxitos"
    )

    // Palabras que indican contenido específico y concreto
    private val palabrasEspecificas = setOf(
        "biblioteca", "laboratorio", "computador", "computadora", "tablet", "tableta",
        "proyector", "pizarra", "silla", "mesa", "escritorio", "ventana", "puerta",
        "aire", "acondicionado", "ventilador", "luz", "iluminación", "electricidad",
        "internet", "wifi", "red", "conexión", "software", "programa", "aplicación",
        "app", "página", "sitio", "web", "plataforma", "sistema", "base", "datos",
        "información", "documento", "archivo", "carpeta", "directorio", "libro",
        "texto", "material", "recurso", "herramienta", "equipo", "instrumento",
        "dispositivo", "aparato", "máquina", "tecnología", "tecnológico", "digital",
        "virtual", "online", "presencial", "híbrido", "mixto", "flexible",
        "horario", "cronograma", "calendario", "agenda", "programa", "plan",
        "estudio", "clase", "lección", "tema", "materia", "asignatura", "curso",
        "grado", "nivel", "año", "semestre", "trimestre", "bimestre", "mes",
        "semana", "día", "hora", "minuto", "tiempo", "duración", "período",
        "profesor", "maestro", "docente", "estudiante", "alumno", "alumna",
        "director", "coordinador", "administrador", "personal", "staff",
        "edificio", "salón", "aula", "clase", "sala", "oficina", "cubículo",
        "pasillo", "corredor", "escalera", "ascensor", "elevador", "rampa",
        "entrada", "salida", "acceso", "puerta", "ventana", "techo", "piso",
        "pared", "muro", "suelo", "césped", "jardín", "patio", "cancha",
        "deporte", "fútbol", "baloncesto", "voleibol", "tenis", "natación",
        "gimnasio", "gimnasia", "ejercicio", "actividad", "física", "deportiva",
        "música", "arte", "dibujo", "pintura", "escultura", "teatro", "danza",
        "baile", "canto", "instrumento", "banda", "orquesta", "coro",
        "ciencia", "matemáticas", "física", "química", "biología", "historia",
        "geografía", "literatura", "lenguaje", "español", "inglés", "francés",
        "alemán", "portugués", "italiano", "latín", "griego", "idioma",
        "comunicación", "expresión", "escritura", "lectura", "habla", "diálogo",
        "conversación", "debate", "discusión", "argumento", "opinión", "punto",
        "vista", "perspectiva", "enfoque", "método", "técnica", "estrategia",
        "táctica", "procedimiento", "proceso", "paso", "etapa", "fase",
        "nivel", "grado", "categoría", "tipo", "clase", "especie", "variedad",
        "diferencia", "distinción", "separación", "división", "parte",
        "sección", "área", "zona", "región", "sector", "segmento", "porción",
        "cantidad", "número", "total", "suma", "resta", "multiplicación",
        "división", "porcentaje", "proporción", "ratio", "tasa", "frecuencia",
        "velocidad", "rapidez", "lentitud", "tardanza", "prontitud", "urgencia",
        "importancia", "prioridad", "relevancia", "significado", "sentido",
        "propósito", "objetivo", "meta", "fin", "finalidad", "intención",
        "motivo", "razón", "causa", "origen", "fuente", "base", "fundamento",
        "justificación", "explicación", "descripción", "definición", "concepto",
        "término", "palabra", "vocablo", "expresión", "frase", "oración",
        "párrafo", "texto", "documento", "escrito", "manuscrito", "impreso",
        "digital", "electrónico", "virtual", "online", "web", "internet",
        "computadora", "ordenador", "pc", "laptop", "portátil", "tablet",
        "celular", "teléfono", "móvil", "smartphone", "dispositivo", "aparato",
        "equipo", "herramienta", "instrumento", "máquina", "tecnología",
        "sistema", "programa", "software", "aplicación", "app", "plataforma",
        "servicio", "función", "característica", "propiedad", "atributo",
        "cualidad", "virtud", "ventaja", "beneficio", "provecho", "utilidad",
        "valor", "importancia", "relevancia", "significado", "sentido",
        "propósito", "objetivo", "meta", "fin", "finalidad", "intención"
    )

    // Palabras que indican contenido vago o sin importancia
    private val palabrasVagas = setOf(
        "cosa", "cosas", "algo", "algún", "alguna", "algunos", "algunas",
        "todo", "toda", "todos", "todas", "nada", "nadie", "ninguno", "ninguna",
        "cualquier", "cualquiera", "cualesquiera", "cualesquier", "cualesquiera",
        "otro", "otra", "otros", "otras", "mismo", "misma", "mismos", "mismas",
        "igual", "iguales", "diferente", "diferentes", "parecido", "parecida",
        "parecidos", "parecidas", "similar", "similares", "distinto", "distinta",
        "distintos", "distintas", "vario", "varia", "varios", "varias",
        "mucho", "mucha", "muchos", "muchas", "poco", "poca", "pocos", "pocas",
        "bastante", "bastantes", "demasiado", "demasiada", "demasiados", "demasiadas",
        "suficiente", "suficientes", "insuficiente", "insuficientes",
        "bueno", "buena", "buenos", "buenas", "malo", "mala", "malos", "malas",
        "grande", "grandes", "pequeño", "pequeña", "pequeños", "pequeñas",
        "alto", "alta", "altos", "altas", "bajo", "baja", "bajos", "bajas",
        "largo", "larga", "largos", "largas", "corto", "corta", "cortos", "cortas",
        "ancho", "ancha", "anchos", "anchas", "estrecho", "estrecha", "estrechos", "estrechas",
        "gordo", "gorda", "gordos", "gordas", "flaco", "flaca", "flacos", "flacas",
        "güeno", "güena", "güenos", "güenas", "malo", "mala", "malos", "malas",
        "feo", "fea", "feos", "feas", "bonito", "bonita", "bonitos", "bonitas",
        "lindo", "linda", "lindos", "lindas", "hermoso", "hermosa", "hermosos", "hermosas",
        "guapo", "guapa", "guapos", "guapas", "atractivo", "atractiva", "atractivos", "atractivas",
        "interesante", "interesantes", "aburrido", "aburrida", "aburridos", "aburridas",
        "divertido", "divertida", "divertidos", "divertidas", "entretenido", "entretenida",
        "entretenidos", "entretenidas", "emocionante", "emocionantes", "emocionado", "emocionada",
        "emocionados", "emocionadas", "nervioso", "nerviosa", "nerviosos", "nerviosas",
        "tranquilo", "tranquila", "tranquilos", "tranquilas", "calmado", "calmada", "calmados", "calmadas",
        "enojado", "enojada", "enojados", "enojadas", "contento", "contenta", "contentos", "contentas",
        "feliz", "felices", "triste", "tristes", "alegre", "alegres", "deprimido", "deprimida",
        "deprimidos", "deprimidas", "optimista", "optimistas", "pesimista", "pesimistas",
        "positivo", "positiva", "positivos", "positivas", "negativo", "negativa", "negativos", "negativas",
        "importante", "importantes", "trascendental", "trascendentales", "esencial", "esenciales",
        "fundamental", "fundamentales", "básico", "básica", "básicos", "básicas",
        "principal", "principales", "secundario", "secundaria", "secundarios", "secundarias",
        "terciario", "terciaria", "terciarios", "terciarias", "primario", "primaria", "primarios", "primarias",
        "último", "última", "últimos", "últimas", "primero", "primera", "primeros", "primeras",
        "segundo", "segunda", "segundos", "segundas", "tercero", "tercera", "terceros", "terceras",
        "cuarto", "cuarta", "cuartos", "cuartas", "quinto", "quinta", "quintos", "quintas",
        "sexto", "sexta", "sextos", "sextas", "séptimo", "séptima", "séptimos", "séptimas",
        "octavo", "octava", "octavos", "octavas", "noveno", "novena", "novenos", "novenas",
        "décimo", "décima", "décimos", "décimas", "centésimo", "centésima", "centésimos", "centésimas",
        "milésimo", "milésima", "milésimos", "milésimas", "millonésimo", "millonésima", "millonésimos", "millonésimas"
    )

    // Palabras que indican contenido negativo o problemático
    private val palabrasNegativas = setOf(
        "odio", "odiar", "detesto", "detestan", "molesta", "molestan", "fastidia", "fastidian",
        "cansado", "cansada", "cansados", "cansadas", "aburrido", "aburrida", "aburridos", "aburridas",
        "terrible", "horrible", "pésimo", "pésima", "pésimos", "pésimas", "malo", "mala", "malos", "malas",
        "inútil", "inútiles", "estúpido", "estúpida", "estúpidos", "estúpidas", "tonto", "tonta", "tontos", "tontas",
        "ridículo", "ridícula", "ridículos", "ridículas", "absurdo", "absurda", "absurdos", "absurdas",
        "sin sentido", "sin propósito", "inútil", "inútiles", "desperdicio", "desperdicios",
        "tiempo perdido", "pérdida de tiempo", "no sirve", "no sirven", "no funciona", "no funcionan"
    )

    // Función para validar contenido de sugerencias
    fun validarSugerencia(titulo: String, contenido: String): SugerenciaValidationResult {
        return SugerenciaValidationResult(esValida = true, motivo = null)
    }
    
    // Función para analizar el sentimiento y constructividad del contenido
    private fun analizarSentimiento(texto: String): SentimientoResult {
        val palabras = texto.lowercase().split(Regex("\\s+"))
        
        val palabrasConstructivasEncontradas = palabrasConstructivas.count { palabra ->
            palabras.any { it.contains(palabra) }
        }
        
        val palabrasNegativasEncontradas = palabrasNegativas.count { palabra ->
            palabras.any { it.contains(palabra) }
        }
        
        val palabrasEspecificasEncontradas = palabrasEspecificas.count { palabra ->
            palabras.any { it.contains(palabra) }
        }
        
        val palabrasVagasEncontradas = palabrasVagas.count { palabra ->
            palabras.any { it == palabra }
        }
        
        val totalPalabras = palabras.size
        val ratioConstructivo = if (totalPalabras > 0) palabrasConstructivasEncontradas.toFloat() / totalPalabras else 0f
        val ratioNegativo = if (totalPalabras > 0) palabrasNegativasEncontradas.toFloat() / totalPalabras else 0f
        val ratioEspecifico = if (totalPalabras > 0) palabrasEspecificasEncontradas.toFloat() / totalPalabras else 0f
        val ratioVago = if (totalPalabras > 0) palabrasVagasEncontradas.toFloat() / totalPalabras else 0f
        
        return SentimientoResult(
            esMuyNegativo = ratioNegativo > 0.08, // Más del 8% de palabras negativas
            esPocoConstructivo = ratioConstructivo < 0.08 && totalPalabras > 8, // Menos del 8% de palabras constructivas
            esMuyVago = ratioVago > 0.15, // Más del 15% de palabras vagas
            esPocoEspecifico = ratioEspecifico < 0.05 && totalPalabras > 10, // Menos del 5% de palabras específicas
            esDemasiadoCorta = totalPalabras < 15 // Menos de 15 palabras totales
        )
    }
    
    private fun contieneRepeticionExcesiva(texto: String): Boolean {
        // Verificar repetición de caracteres (más de 5 veces seguidas)
        for (i in 0 until texto.length - 4) {
            val caracter = texto[i]
            if (texto.substring(i, i + 5).all { it == caracter }) {
                return true
            }
        }
        return false
    }
    
    private fun esSoloCaracteresEspeciales(texto: String): Boolean {
        val textoLimpio = texto.replace(Regex("[^a-zA-ZáéíóúÁÉÍÓÚñÑ]"), "")
        return textoLimpio.length < texto.length * 0.3 // Menos del 30% son letras
    }
    
    private fun esSpam(texto: String): Boolean {
        val palabras = texto.lowercase().split(Regex("\\s+"))
        val palabrasUnicas = palabras.toSet()
        val ratio = palabrasUnicas.size.toFloat() / palabras.size
        
        // Si hay muy poca variedad de palabras, es probable spam
        return ratio < 0.3 && palabras.size > 10
    }
    
    private fun esContenidoGenerico(titulo: String, contenido: String): Boolean {
        val textoCompleto = "${titulo.lowercase()} ${contenido.lowercase()}"
        val palabras = textoCompleto.split(Regex("\\s+"))
        
        // Frases muy genéricas que no aportan valor
        val frasesGenericas = setOf(
            "mejorar", "mejorar algo", "mejorar las cosas", "mejorar todo",
            "cambiar", "cambiar algo", "cambiar las cosas", "cambiar todo",
            "agregar", "agregar algo", "agregar cosas", "agregar todo",
            "hacer mejor", "hacer las cosas mejor", "hacer todo mejor",
            "poner", "poner algo", "poner cosas", "poner todo",
            "tener", "tener algo", "tener cosas", "tener todo",
            "necesitamos", "necesitamos algo", "necesitamos cosas", "necesitamos todo",
            "falta", "falta algo", "faltan cosas", "falta todo",
            "debería", "debería ser", "debería estar", "debería tener",
            "sería", "sería mejor", "sería bueno", "sería bueno tener",
            "podría", "podría ser", "podría estar", "podría tener",
            "sugiero", "sugiero algo", "sugiero cosas", "sugiero todo",
            "propongo", "propongo algo", "propongo cosas", "propongo todo",
            "idea", "ideas", "tener ideas", "más ideas",
            "sugerencia", "sugerencias", "hacer sugerencias", "más sugerencias",
            "propuesta", "propuestas", "hacer propuestas", "más propuestas"
        )
        
        // Verificar si el texto contiene principalmente frases genéricas
        val frasesGenericasEncontradas = frasesGenericas.count { frase ->
            textoCompleto.contains(frase)
        }
        
        // Si más del 30% del texto son frases genéricas, es contenido genérico
        val totalPalabras = palabras.size
        val ratioGenerico = if (totalPalabras > 0) frasesGenericasEncontradas.toFloat() / totalPalabras else 0f
        
        return ratioGenerico > 0.3 || (totalPalabras < 20 && frasesGenericasEncontradas > 2)
    }

    suspend fun crearSugerencia(sugerencia: Sugerencia): String {
        return try {
            // Validar la sugerencia antes de crearla
            val validacion = validarSugerencia(sugerencia.titulo, sugerencia.contenido)
            if (!validacion.esValida) {
                // Notificar a los admins sobre el intento de sugerencia inapropiada
                try {
                    val admins = obtenerEmailsAdminsPorGradoYGrupo(sugerencia.grado, sugerencia.grupo)
                    if (admins.isNotEmpty()) {
                        // Obtener el nombre del usuario que intentó hacer la sugerencia
                        val usuarioDoc = usuariosCollection.document(sugerencia.autorId).get().await()
                        val nombreUsuario = usuarioDoc.getString("nombreCompleto") ?: "Usuario desconocido"
                        
                        // Crear notificación para cada admin
                        admins.forEach { email ->
                            notificacionService.crearNotificacion(
                                titulo = "🚨 Intento de Sugerencia Inapropiada",
                                mensaje = "El estudiante $nombreUsuario (${sugerencia.grado}° ${sugerencia.grupo}) intentó crear una sugerencia con contenido inapropiado.\n\nMotivo: ${validacion.motivo}\n\nTítulo: ${sugerencia.titulo}\nContenido: ${sugerencia.contenido}",
                                tipo = TipoNotificacion.SUGERENCIA_INNAPROPIADA,
                                destinatarioEmail = email,
                                leida = false
                            )
                        }
                        println("DEBUG: Notificación de sugerencia inapropiada enviada a ${admins.size} admins")
                    }
                } catch (e: Exception) {
                    println("DEBUG: Error al enviar notificación de sugerencia inapropiada: ${e.message}")
                    // No fallar la operación principal por errores de notificación
                }
                
                throw IllegalArgumentException(validacion.motivo)
            }
            
            // Obtener el nombre real del usuario
            val usuarioDoc = usuariosCollection.document(sugerencia.autorId).get().await()
            val nombreCompleto = usuarioDoc.getString("nombreCompleto") ?: "Usuario"

            val id = UUID.randomUUID().toString()
            val sugerenciaConId = sugerencia.copy(
                id = id,
                autorNombre = nombreCompleto,
                likes = emptyList(),
                dislikes = emptyList()
            )
            sugerenciasCollection.document(id).set(sugerenciaConId).await()
            println("Sugerencia creada exitosamente con ID: $id")
            
            // Notificar a los admins sobre la nueva sugerencia pendiente
            try {
                println("DEBUG: Buscando admins para grado: ${sugerencia.grado}, grupo: ${sugerencia.grupo}")
                val admins = obtenerEmailsAdminsPorGradoYGrupo(sugerencia.grado, sugerencia.grupo)
                println("DEBUG: Admins encontrados: $admins")
                
                if (admins.isNotEmpty()) {
                    // Crear notificación en Firestore para cada admin
                    admins.forEach { email ->
                        println("DEBUG: Creando notificación para admin: $email")
                        notificacionService.crearNotificacion(
                            titulo = "💡 Nueva sugerencia pendiente",
                            mensaje = "Sugerencia de $nombreCompleto: ${sugerencia.titulo}",
                            tipo = TipoNotificacion.SUGERENCIA_PENDIENTE,
                            destinatarioEmail = email,
                            leida = false
                        )
                    }
                    println("✅ Notificaciones creadas en Firestore para ${admins.size} administradores")
                } else {
                    println("⚠️ No se encontraron administradores para el grado ${sugerencia.grado} grupo ${sugerencia.grupo}")
                    println("DEBUG: Verificando todos los usuarios del grado y grupo...")
                    
                    // Debug: mostrar todos los usuarios del grado y grupo
                    val todosUsuarios = usuariosCollection
                        .whereEqualTo("grado", sugerencia.grado)
                        .whereEqualTo("grupo", sugerencia.grupo)
                        .get()
                        .await()
                    
                    println("DEBUG: Total usuarios en grado ${sugerencia.grado} grupo ${sugerencia.grupo}: ${todosUsuarios.size()}")
                    for (doc in todosUsuarios.documents) {
                        val email = doc.getString("email")
                        val esAdmin = doc.getBoolean("esAdmin")
                        val rol = doc.getString("rol")
                        println("DEBUG: Usuario: $email, esAdmin: $esAdmin, rol: $rol")
                    }
                }
            } catch (e: Exception) {
                println("❌ Error al enviar notificación automática: ${e.message}")
                e.printStackTrace()
                // No fallar la operación principal por errores de notificación
            }
            
            id
        } catch (e: Exception) {
            println("Error al crear sugerencia: ${e.message}")
            throw e
        }
    }

    suspend fun obtenerSugerencias(
        usuarioId: String,
        grado: String,
        grupo: String
    ): List<Sugerencia> {
        return try {
            println("Obteniendo sugerencias para usuario: $usuarioId, grado: $grado, grupo: $grupo")
            
            // Primero verificamos si el usuario es admin
            val esAdmin = esAdmin(usuarioId)
            println("Usuario es admin: $esAdmin")
            
            val querySnapshot = if (esAdmin) {
                // Si es admin, obtiene todas las sugerencias de su grado y grupo asignado
                sugerenciasCollection
                    .whereEqualTo("grado", grado)
                    .whereEqualTo("grupo", grupo)
                    .get()
                    .await()
            } else {
                // Si es estudiante, obtiene solo sus sugerencias
                sugerenciasCollection
                    .whereEqualTo("grado", grado)
                    .whereEqualTo("grupo", grupo)
                    .whereEqualTo("autorId", usuarioId)
                    .get()
                    .await()
            }
            
            val sugerencias = querySnapshot.documents.mapNotNull { document ->
                try {
                    val sugerencia = document.toObject(Sugerencia::class.java)?.copy(id = document.id)
                    if (sugerencia != null) {
                        // Obtener el nombre completo del autor de manera segura
                        try {
                            val autorDoc = usuariosCollection.document(sugerencia.autorId).get().await()
                            val nombreCompleto = autorDoc.getString("nombreCompleto") ?: "Usuario"
                            sugerencia.copy(autorNombre = nombreCompleto)
                        } catch (e: Exception) {
                            println("Error al obtener nombre del autor: ${e.message}")
                            sugerencia.copy(autorNombre = "Usuario")
                        }
                    } else null
                } catch (e: Exception) {
                    println("Error al procesar sugerencia: ${e.message}")
                    null
                }
            }
            
            println("Sugerencias encontradas: ${sugerencias.size}")
            sugerencias.forEach { sugerencia ->
                println("Sugerencia: ${sugerencia.titulo}, Autor: ${sugerencia.autorNombre}, Grado: ${sugerencia.grado}, Grupo: ${sugerencia.grupo}")
            }
            
            sugerencias
        } catch (e: Exception) {
            println("Error al obtener sugerencias: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun obtenerSugerenciasPorGradoYGrupo(grado: String, grupo: String): List<Sugerencia> {
        return try {
            val query = sugerenciasCollection
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()
            
            query.documents.mapNotNull { doc ->
                doc.toObject(Sugerencia::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            println("Error al obtener sugerencias por grado y grupo: ${e.message}")
            emptyList()
        }
    }

    suspend fun esAdmin(usuarioId: String): Boolean {
        return try {
            val usuarioDoc = usuariosCollection.document(usuarioId).get().await()
            usuarioDoc.getBoolean("esAdmin") ?: false
        } catch (e: Exception) {
            println("Error al verificar si es admin: ${e.message}")
            false
        }
    }

    suspend fun votarSugerencia(sugerenciaId: String, usuarioId: String, esLike: Boolean): Boolean {
        val sugerenciaRef = sugerenciasCollection.document(sugerenciaId)
        
        return db.runTransaction { transaction ->
            val snapshot = transaction.get(sugerenciaRef)
            val sugerencia = snapshot.toObject(Sugerencia::class.java) ?: return@runTransaction false
            
            val likes = sugerencia.likes
            val dislikes = sugerencia.dislikes
            
            // Si el usuario ya votó, remover su voto anterior
            val nuevosLikes = if (usuarioId in likes) likes - usuarioId else likes
            val nuevosDislikes = if (usuarioId in dislikes) dislikes - usuarioId else dislikes
            
            // Agregar el nuevo voto si es diferente
            if (esLike) {
                transaction.update(sugerenciaRef, "likes", nuevosLikes + usuarioId)
                transaction.update(sugerenciaRef, "dislikes", nuevosDislikes)
            } else {
                transaction.update(sugerenciaRef, "likes", nuevosLikes)
                transaction.update(sugerenciaRef, "dislikes", nuevosDislikes + usuarioId)
            }
            
            true
        }.await()
    }

    suspend fun obtenerVotoUsuario(sugerenciaId: String, usuarioId: String): String? {
        val document = sugerenciasCollection.document(sugerenciaId).get().await()
        val sugerencia = document.toObject(Sugerencia::class.java) ?: return null
        
        return when {
            usuarioId in sugerencia.likes -> "like"
            usuarioId in sugerencia.dislikes -> "dislike"
            else -> null
        }
    }

    suspend fun editarSugerencia(sugerenciaId: String, nuevoTitulo: String, nuevoContenido: String, usuarioId: String): Boolean {
        return try {
            val sugerenciaRef = sugerenciasCollection.document(sugerenciaId)
            val sugerencia = sugerenciaRef.get().await().toObject(Sugerencia::class.java)
            
            if (sugerencia?.autorId == usuarioId) {
                sugerenciaRef.update(
                    "titulo", nuevoTitulo,
                    "contenido", nuevoContenido
                ).await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun borrarSugerencia(sugerenciaId: String, usuarioId: String): Boolean {
        return try {
            val sugerenciaRef = sugerenciasCollection.document(sugerenciaId)
            val sugerencia = sugerenciaRef.get().await().toObject(Sugerencia::class.java)
            
            if (sugerencia?.autorId == usuarioId) {
                sugerenciaRef.delete().await()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun agregarComentario(sugerenciaId: String, comentario: Comentario) {
        val comentarioId = UUID.randomUUID().toString()
        val comentarioConId = comentario.copy(id = comentarioId)
        
        sugerenciasCollection.document(sugerenciaId)
            .update("comentarios", com.google.firebase.firestore.FieldValue.arrayUnion(comentarioConId))
            .await()
    }

    suspend fun actualizarEstadoSugerencia(sugerenciaId: String, nuevoEstado: String, comentario: String = "") {
        sugerenciasCollection.document(sugerenciaId)
            .update("estado", nuevoEstado)
            .await()
        
        // Enviar notificación automática al autor
        try {
            val sugerenciaRef = sugerenciasCollection.document(sugerenciaId)
            val sugerencia = sugerenciaRef.get().await()
            val autorId = sugerencia.getString("autorId")
            if (autorId != null) {
                val notificacionService = NotificacionService()
                val titulo = when (nuevoEstado.lowercase()) {
                    "aprobada" -> "✅ Sugerencia Aprobada"
                    "rechazada" -> "❌ Sugerencia Rechazada"
                    else -> "Sugerencia Procesada"
                }
                val mensaje = when (nuevoEstado.lowercase()) {
                    "aprobada" -> "Tu sugerencia ha sido aprobada por el administrador."
                    "rechazada" -> "Tu sugerencia ha sido rechazada por el administrador."
                    else -> "Tu sugerencia ha sido procesada por el administrador."
                }
                
                val mensajeFinal = if (comentario.isNotBlank()) {
                    "$mensaje\n\nComentario: $comentario"
                } else {
                    mensaje
                }
                
                // Enviar notificación push al autor
                notificacionService.enviarNotificacionPush(autorId, titulo, mensajeFinal)
                
                // También crear notificación en Firestore
                notificacionService.crearNotificacion(
                    titulo = titulo,
                    mensaje = mensajeFinal,
                    tipo = TipoNotificacion.SUGERENCIA,
                    destinatarioEmail = autorId,
                    leida = false
                )
            }
        } catch (e: Exception) {
            println("Error al enviar notificación automática: ${e.message}")
        }
    }

    suspend fun obtenerSugerenciaPorId(sugerenciaId: String): Sugerencia? {
        val document = sugerenciasCollection.document(sugerenciaId).get().await()
        return document.toObject(Sugerencia::class.java)
    }

    suspend fun obtenerUsuarioPorId(usuarioId: String): com.example.buzondesugerenciascojema.data.Usuario? {
        return try {
            val document = usuariosCollection.document(usuarioId).get().await()
            document.toObject(com.example.buzondesugerenciascojema.data.Usuario::class.java)
        } catch (e: Exception) {
            println("Error al obtener usuario por ID: ${e.message}")
            null
        }
    }

    suspend fun marcarComentariosComoLeidos(sugerenciaId: String, usuarioId: String) {
        val sugerenciaRef = sugerenciasCollection.document(sugerenciaId)
        val snapshot = sugerenciaRef.get().await()
        val sugerencia = snapshot.toObject(Sugerencia::class.java)
        if (sugerencia != null) {
            val nuevosLeidos = (sugerencia.comentariosLeidosPor + usuarioId).distinct()
            sugerenciaRef.update("comentariosLeidosPor", nuevosLeidos).await()
        }
    }

    // Función auxiliar para obtener emails de admins por grado y grupo
    private suspend fun obtenerEmailsAdminsPorGradoYGrupo(grado: String, grupo: String): List<String> {
        return try {
            // Intentar diferentes campos para identificar administradores
            val snapshot = usuariosCollection
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()
            
            val admins = mutableListOf<String>()
            
            for (doc in snapshot.documents) {
                val email = doc.getString("email")
                val esAdmin = doc.getBoolean("esAdmin") ?: false
                val rol = doc.getString("rol") ?: ""
                
                // Verificar si es admin por diferentes campos
                if (email != null && (esAdmin || rol.equals("admin", ignoreCase = true) || rol.equals("administrador", ignoreCase = true))) {
                    admins.add(email)
                    println("DEBUG: Admin encontrado: $email (esAdmin: $esAdmin, rol: $rol)")
                }
            }
            
            println("DEBUG: Total de admins encontrados para grado $grado grupo $grupo: ${admins.size}")
            admins
        } catch (e: Exception) {
            println("DEBUG: Error al obtener emails de admins: ${e.message}")
            emptyList()
        }
    }

    // Función auxiliar para obtener email de un usuario por ID
    private suspend fun obtenerEmailUsuario(usuarioId: String): String? {
        return try {
            val document = usuariosCollection.document(usuarioId).get().await()
            document.getString("email")
        } catch (e: Exception) {
            println("DEBUG: Error al obtener email del usuario: ${e.message}")
            null
        }
    }

    // Función para obtener estadísticas de sugerencias
    suspend fun obtenerEstadisticasSugerencias(grado: String, grupo: String): SugerenciaEstadisticas {
        return try {
            val query = sugerenciasCollection
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .get()
                .await()
            
            val sugerencias = query.toObjects(Sugerencia::class.java)
            
            val total = sugerencias.size
            val aprobadas = sugerencias.count { it.estado == "Aprobada" }
            val rechazadas = sugerencias.count { it.estado == "Rechazada" }
            val pendientes = sugerencias.count { it.estado == "Pendiente" }
            
            SugerenciaEstadisticas(
                total = total,
                aprobadas = aprobadas,
                rechazadas = rechazadas,
                pendientes = pendientes,
                porcentajeAprobacion = if (total > 0) (aprobadas.toFloat() / total) * 100 else 0f
            )
        } catch (e: Exception) {
            println("Error al obtener estadísticas: ${e.message}")
            SugerenciaEstadisticas()
        }
    }

    // Función para obtener sugerencias rechazadas con motivos
    suspend fun obtenerSugerenciasRechazadas(grado: String, grupo: String): List<SugerenciaRechazada> {
        return try {
            val query = sugerenciasCollection
                .whereEqualTo("grado", grado)
                .whereEqualTo("grupo", grupo)
                .whereEqualTo("estado", "Rechazada")
                .get()
                .await()
            
            query.toObjects(Sugerencia::class.java).map { sugerencia: Sugerencia ->
                SugerenciaRechazada(
                    id = sugerencia.id,
                    titulo = sugerencia.titulo,
                    contenido = sugerencia.contenido,
                    autorNombre = sugerencia.autorNombre,
                    fecha = sugerencia.fecha,
                    motivoRechazo = "Rechazada por administrador" // Por defecto
                )
            }
        } catch (e: Exception) {
            println("Error al obtener sugerencias rechazadas: ${e.message}")
            emptyList()
        }
    }

    // Función para generar reporte de contenido problemático
    suspend fun generarReporteContenidoProblematico(grado: String, grupo: String): ReporteContenidoProblematico {
        return try {
            val sugerenciasRechazadas = obtenerSugerenciasRechazadas(grado, grupo)
            
            val motivosComunes = mutableMapOf<String, Int>()
            val autoresProblematicos = mutableMapOf<String, Int>()
            
            sugerenciasRechazadas.forEach { sugerencia ->
                // Contar autores problemáticos
                autoresProblematicos[sugerencia.autorNombre] = 
                    autoresProblematicos.getOrDefault(sugerencia.autorNombre, 0) + 1
                
                // Analizar contenido para detectar motivos
                val validacion = validarSugerencia(sugerencia.titulo, sugerencia.contenido)
                if (!validacion.esValida) {
                    val motivo = validacion.motivo ?: "Motivo desconocido"
                    motivosComunes[motivo] = motivosComunes.getOrDefault(motivo, 0) + 1
                }
            }
            
            val topAutoresProblematicos = autoresProblematicos.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { "${it.key} (${it.value} sugerencias)" }
            
            val topMotivos = motivosComunes.entries
                .sortedByDescending { it.value }
                .take(5)
                .map { "${it.key} (${it.value} veces)" }
            
            ReporteContenidoProblematico(
                totalSugerenciasRechazadas = sugerenciasRechazadas.size,
                topAutoresProblematicos = topAutoresProblematicos,
                topMotivosRechazo = topMotivos,
                fechaGeneracion = java.util.Date()
            )
        } catch (e: Exception) {
            println("Error al generar reporte: ${e.message}")
            ReporteContenidoProblematico()
        }
    }
} 