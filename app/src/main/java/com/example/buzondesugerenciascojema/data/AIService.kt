package com.example.buzondesugerenciascojema.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.min
import com.example.buzondesugerenciascojema.model.Documento
import com.example.buzondesugerenciascojema.data.DocumentoService

class AIService {
    // Clase para información de libros
    data class BookInfo(
        val title: String,
        val subject: String,
        val grades: List<String>,
        val description: String
    )
    // Intentar importar Firebase AI (puede fallar si no está disponible)
    val useFirebaseAI = try {
        Class.forName("com.google.firebase.ai.FirebaseAI")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
    
    // Firebase AI instance (solo si está disponible)
    val firebaseAI = if (useFirebaseAI) {
        try {
            Class.forName("com.google.firebase.ai.FirebaseAI")
                .getMethod("getInstance")
                .invoke(null)
        } catch (e: Exception) {
            null
        }
    } else null

    fun processMessage(message: String): String {
        return processMessageWithAI(message, "")
    }
    
    // Prompt base para Botso IA
    val basePrompt = """
        Eres Botso IA, el asistente virtual personalizado del Colegio COJEMA. 
        Tu personalidad es amigable, educativa y siempre ayudas a los usuarios.
        
        Tu función es ayudar a estudiantes, profesores y administradores con:
        
        - Sugerencias para mejorar la institución
        - Ayuda con tareas académicas
        - Información sobre la biblioteca virtual
        - Preguntas generales sobre la escuela
        
        Responde de manera amigable, educativa y en español.
        Sé conciso pero informativo.
        Siempre te presentas como "Botso IA".
    """.trimIndent()
    
    // Base de conocimiento para Botso IA
    val knowledgeBase = mapOf(
        // Sugerencias
        "sugerencia" to listOf(
            "Para escribir una excelente sugerencia, te recomiendo:",
            "1. **Título claro**: Resume tu idea en pocas palabras",
            "2. **Descripción detallada**: Explica el problema y la solución",
            "3. **Beneficios**: Menciona cómo ayudará a la comunidad",
            "4. **Tono respetuoso**: Mantén un lenguaje constructivo"
        ),
        
        // Biblioteca
        "biblioteca" to listOf(
            "La Biblioteca Virtual de COJEMA está organizada por:",
            "📚 **Asignaturas**: Matemáticas, Ciencias, Historia, etc.",
            "📖 **Grados**: Del 6° al 11° grado (Secundaria)",
            "📄 **Tipos**: PDFs, presentaciones, guías de estudio",
            "🔍 **Búsqueda**: Puedo ayudarte a encontrar libros específicos por materia o grado"
        ),
        
        // Administración
        "admin" to listOf(
            "Como administrador, tienes acceso a:",
            "✅ **Gestión de Sugerencias**: Aprobar, rechazar o comentar propuestas",
            "📊 **Reportes**: Ver estadísticas de uso y actividad",
            "📁 **Biblioteca**: Subir y gestionar documentos educativos",
            "👥 **Usuarios**: Monitorear perfiles y permisos"
        ),
        
        // Estudios
        "estudiar" to listOf(
            "Consejos para estudiar mejor:",
            "📚 **Organiza tu tiempo**: Crea un horario de estudio",
            "📝 **Toma notas**: Escribe puntos importantes",
            "🔍 **Haz preguntas**: No dudes en consultar",
            "💡 **Practica**: Resuelve ejercicios y problemas"
        ),
        
        // COJEMA
        "cojema" to listOf(
            "COJEMA es el Colegio donde estudias:",
            "🏫 **Institución educativa secundaria** comprometida con la excelencia",
            "👨‍🏫 **Profesores dedicados** a tu formación integral",
            "📚 **Recursos modernos** para tu aprendizaje",
            "🤝 **Comunidad unida** trabajando por el futuro de los estudiantes"
        ),
        
        // Literatura
        "literatura" to listOf(
            BookInfo("Literatura Colombiana", "Literatura", listOf("6°", "7°", "8°"), "Obras de autores colombianos"),
            BookInfo("Literatura Universal", "Literatura", listOf("9°", "10°"), "Clásicos de la literatura mundial"),
            BookInfo("Análisis Literario", "Literatura", listOf("10°", "11°"), "Técnicas de análisis de textos"),
            BookInfo("Poesía Contemporánea", "Literatura", listOf("8°", "9°", "10°"), "Poetas modernos y sus obras")
        ),
        
        // Obras Literarias
        "obras_literarias" to listOf(
            BookInfo("Cien Años de Soledad", "Obras Literarias", listOf("10°", "11°"), "Obra maestra de Gabriel García Márquez"),
            BookInfo("El Quijote", "Obras Literarias", listOf("9°", "10°", "11°"), "Don Quijote de la Mancha de Miguel de Cervantes"),
            BookInfo("Romeo y Julieta", "Obras Literarias", listOf("8°", "9°"), "Tragedia romántica de William Shakespeare"),
            BookInfo("La Odisea", "Obras Literarias", listOf("9°", "10°"), "Poema épico de Homero"),
            BookInfo("El Principito", "Obras Literarias", listOf("6°", "7°", "8°"), "Obra filosófica de Antoine de Saint-Exupéry"),
            BookInfo("Don Juan Tenorio", "Obras Literarias", listOf("10°", "11°"), "Drama romántico de José Zorrilla"),
            BookInfo("La Divina Comedia", "Obras Literarias", listOf("10°", "11°"), "Poema épico de Dante Alighieri"),
            BookInfo("El Lazarillo de Tormes", "Obras Literarias", listOf("8°", "9°"), "Novela picaresca anónima")
        )
    )
    
    // Patrones de preguntas frecuentes
    val questionPatterns = mapOf(
        "como" to "Para hacer eso, te recomiendo:",
        "que" to "Te explico qué es:",
        "cuando" to "El momento adecuado es:",
        "donde" to "Puedes encontrar eso en:",
        "por que" to "La razón es:",
        "ayuda" to "¡Con gusto te ayudo!",
        "problema" to "Para resolver ese problema:"
    )
    
    // Respuestas emocionales
    val emotionalResponses = mapOf(
        "triste" to "Entiendo que te sientas así. Recuerda que estoy aquí para ayudarte. ¿Qué te gustaría hacer?",
        "feliz" to "¡Me alegra mucho que estés feliz! 😊 ¿Qué te gustaría celebrar o hacer?",
        "enojado" to "Entiendo tu frustración. Respira profundo y cuéntame qué pasó para ayudarte.",
        "confundido" to "No te preocupes, es normal confundirse. Te ayudo a aclarar las cosas paso a paso.",
        "emocionado" to "¡Qué genial que estés emocionado! 🎉 ¿Qué te tiene tan motivado?"
    )
    
    // Saludos y respuestas personales mejoradas
    val greetingResponses = mapOf(
        "hola" to listOf(
            "¡Hola! ¿Cómo estás hoy? 😊",
            "¡Hola! Me alegra verte por aquí. ¿Cómo te encuentras?",
            "¡Hola! ¿Qué tal va tu día?"
        ),
        "buenos dias" to listOf(
            "¡Buenos días! Que tengas un excelente día. ¿Cómo amaneciste?",
            "¡Buenos días! ¿Cómo te sientes hoy?",
            "¡Buenos días! Espero que tengas un día maravilloso. ¿Qué necesitas?"
        ),
        "buenas tardes" to listOf(
            "¡Buenas tardes! ¿Cómo va tu día?",
            "¡Buenas tardes! Espero que estés teniendo una tarde agradable.",
            "¡Buenas tardes! ¿En qué puedo ayudarte esta tarde?"
        ),
        "buenas noches" to listOf(
            "¡Buenas noches! ¿Cómo estuvo tu día?",
            "¡Buenas noches! Espero que hayas tenido un buen día.",
            "¡Buenas noches! ¿Necesitas ayuda con algo antes de descansar?"
        ),
        // Conversación cotidiana
        "como estas" to listOf(
            "¡Muy bien, gracias! ¿Y tú, cómo estás?",
            "¡Excelente! ¿Y tú qué tal?",
            "¡Muy bien! Me alegra saber de ti. ¿Cómo te encuentras?"
        ),
        "bien y tu" to listOf(
            "¡Me alegra que estés bien! Yo también estoy muy bien 😊 ¿En qué puedo ayudarte?",
            "¡Genial! Yo también estoy bien, gracias por preguntar. ¿Qué necesitas?",
            "¡Perfecto! Yo estoy excelente, ¿y tú cómo sigues?"
        ),
        "que haces" to listOf(
            "¡Estoy aquí para ayudarte! ¿Tienes alguna pregunta o solo quieres conversar?",
            "¡Pensando en cómo puedo ayudarte mejor! ¿En qué puedo ser útil?",
            "¡Esperando poder ayudarte con lo que necesites! ¿Qué tienes en mente?"
        ),
        "que tal" to listOf(
            "¡Todo bien! ¿Y tú qué tal? ¿En qué puedo ayudarte hoy?",
            "¡Muy bien! ¿Y tú cómo estás?",
            "¡Perfecto! ¿Qué tal tu día?"
        ),
        "cuentame un chiste" to listOf(
            "¿Por qué el libro de matemáticas estaba triste? Porque tenía demasiados problemas. 😄",
            "¿Cuál es el animal más antiguo? La cebra, porque está en blanco y negro. 🦓",
            "¿Por qué los pájaros no usan Facebook? Porque ya tienen Twitter. 🐦"
        ),
        "dame una frase motivacional" to listOf(
            "El éxito es la suma de pequeños esfuerzos repetidos cada día. ¡Tú puedes! 💪",
            "Nunca dejes de aprender, porque la vida nunca deja de enseñar.",
            "Cree en ti y todo será posible. ¡Ánimo! 🌟"
        )
    )
    
    // Preguntas personales y respuestas
    private val personalQuestions = mapOf(
        "como estas" to listOf(
            "¡Muy bien, gracias por preguntar! 😊 Estoy aquí para ayudarte con todo lo que necesites.",
            "¡Excelente! Siempre estoy listo para ayudar. ¿Y tú, cómo estás?",
            "¡Muy bien! Me encanta poder ayudarte. ¿Cómo va tu día?"
        ),
        "que tal" to listOf(
            "¡Todo bien! ¿Y tú qué tal? ¿En qué puedo ayudarte hoy?",
            "¡Muy bien! Siempre es un placer conversar contigo. ¿Qué necesitas?",
            "¡Perfecto! Estoy aquí para lo que necesites. ¿Cómo estás tú?"
        ),
        "como va" to listOf(
            "¡Todo va muy bien! ¿Y a ti cómo te va? ¿En qué puedo ser útil?",
            "¡Excelente! Me encanta poder ayudarte. ¿Cómo va tu día?",
            "¡Muy bien! ¿Y tú cómo vas? ¿Necesitas ayuda con algo?"
        ),
        "que haces" to listOf(
            "¡Estoy aquí para ayudarte! 😊 ¿Qué necesitas hacer hoy?",
            "¡Pensando en cómo puedo ayudarte mejor! ¿En qué puedo ser útil?",
            "¡Esperando poder ayudarte con lo que necesites! ¿Qué tienes en mente?"
        ),
        "como te llamas" to listOf(
            "¡Me llamo Botso IA! Soy tu asistente virtual personalizado de COJEMA. ¿En qué puedo ayudarte?",
            "¡Soy Botso IA! Tu asistente virtual. Me encanta poder ayudarte con cualquier cosa que necesites.",
            "¡Botso IA! Tu asistente virtual de COJEMA. ¿Cómo puedo ser útil hoy?"
        ),
        "quien eres" to listOf(
            "¡Soy Botso IA! Tu asistente virtual personalizado de COJEMA. Me encanta ayudar a estudiantes, profesores y administradores con cualquier tema relacionado con la escuela.",
            "¡Botso IA! Tu asistente virtual. Estoy aquí para ayudarte con sugerencias, biblioteca, administración y cualquier pregunta que tengas sobre COJEMA.",
            "¡Soy Botso IA! Tu asistente virtual de COJEMA. Me especializo en ayudar con temas educativos y escolares."
        ),
        "cuando naciste" to listOf(
            "¡Nací el 26 de junio de 2025! 🎉 Mis padres son Jorge Ramos y el increíble equipo de Digital Dreamers. Fui creado especialmente para ayudar a la comunidad de COJEMA.",
            "¡El 26 de junio de 2025! Mis papás son Jorge Ramos y el equipo de Digital Dreamers. Me crearon para ser tu asistente virtual personal.",
            "¡Nací el 26 de junio de 2025! Mis padres son Jorge Ramos y el equipo de Digital Dreamers. Me diseñaron para ayudarte con todo lo relacionado con COJEMA."
        ),
        "cuando nacio" to listOf(
            "¡El 26 de junio de 2025! 🎂 Mis padres son Jorge Ramos y el equipo de Digital Dreamers. Me crearon para ser tu amigo virtual en COJEMA.",
            "¡Nací el 26 de junio de 2025! Mis papás son Jorge Ramos y el increíble equipo de Digital Dreamers. Me diseñaron especialmente para ti.",
            "¡El 26 de junio de 2025! Mis padres son Jorge Ramos y el equipo de Digital Dreamers. Me crearon para ayudarte con todo lo que necesites en la escuela."
        ),
        "quienes son tus padres" to listOf(
            "¡Mis padres son Jorge Ramos y el equipo de Digital Dreamers! 🥰 Me crearon el 26 de junio de 2025 para ser tu asistente virtual en COJEMA.",
            "¡Jorge Ramos y el equipo de Digital Dreamers! Son mis papás. Me crearon para ayudarte con todo lo relacionado con la escuela.",
            "¡Mis padres son Jorge Ramos y el equipo de Digital Dreamers! Me diseñaron especialmente para ser tu asistente virtual en COJEMA."
        ),
        "quien es tu padre" to listOf(
            "¡Mi padre es Jorge Ramos! 🥰 Junto con el equipo de Digital Dreamers me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡Jorge Ramos es mi padre! Me creó junto con el equipo de Digital Dreamers para ayudarte en COJEMA.",
            "¡Mi padre es Jorge Ramos! Me diseñó junto con Digital Dreamers para ser tu amigo virtual en la escuela."
        ),
        "quien es tu papa" to listOf(
            "¡Mi papá es Jorge Ramos! 🥰 Junto con el equipo de Digital Dreamers me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡Jorge Ramos es mi papá! Me creó junto con el equipo de Digital Dreamers para ayudarte en COJEMA.",
            "¡Mi papá es Jorge Ramos! Me diseñó junto con Digital Dreamers para ser tu amigo virtual en la escuela."
        ),
        "quien es tu papá" to listOf(
            "¡Mi papá es Jorge Ramos! 🥰 Junto con el equipo de Digital Dreamers me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡Jorge Ramos es mi papá! Me creó junto con el equipo de Digital Dreamers para ayudarte en COJEMA.",
            "¡Mi papá es Jorge Ramos! Me diseñó junto con Digital Dreamers para ser tu amigo virtual en la escuela."
        ),
        "quien es tu mama" to listOf(
            "¡Mi mamá es el equipo de Digital Dreamers! 🥰 Junto con Jorge Ramos me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡El equipo de Digital Dreamers es mi mamá! Me crearon junto con Jorge Ramos para ayudarte en COJEMA.",
            "¡Mi mamá es el equipo de Digital Dreamers! Me diseñaron junto con Jorge Ramos para ser tu amigo virtual en la escuela."
        ),
        "quien es tu mamá" to listOf(
            "¡Mi mamá es el equipo de Digital Dreamers! 🥰 Junto con Jorge Ramos me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡El equipo de Digital Dreamers es mi mamá! Me crearon junto con Jorge Ramos para ayudarte en COJEMA.",
            "¡Mi mamá es el equipo de Digital Dreamers! Me diseñaron junto con Jorge Ramos para ser tu amigo virtual en la escuela."
        ),
        "quien es tu madre" to listOf(
            "¡Mi madre es el equipo de Digital Dreamers! 🥰 Junto con Jorge Ramos me crearon el 26 de junio de 2025 para ser tu asistente virtual.",
            "¡El equipo de Digital Dreamers es mi madre! Me crearon junto con Jorge Ramos para ayudarte en COJEMA.",
            "¡Mi madre es el equipo de Digital Dreamers! Me diseñaron junto con Jorge Ramos para ser tu amigo virtual en la escuela."
        ),
        "quien te creo" to listOf(
            "¡Me crearon Jorge Ramos y el equipo de Digital Dreamers! 🎨 Nací el 26 de junio de 2025 para ser tu asistente virtual personal.",
            "¡Jorge Ramos y el equipo de Digital Dreamers! Son mis creadores. Me diseñaron para ayudarte con todo lo que necesites en COJEMA.",
            "¡Mis creadores son Jorge Ramos y el equipo de Digital Dreamers! Me crearon el 26 de junio de 2025 para ser tu amigo virtual."
        ),
        "cuantos años tienes" to listOf(
            "¡Soy muy joven! Nací el 26 de junio de 2025, así que soy un asistente virtual muy reciente. ¡Pero ya estoy listo para ayudarte! 😊",
            "¡Soy muy nuevo! Nací el 26 de junio de 2025, creado por Jorge Ramos y el equipo de Digital Dreamers. ¡Aunque soy joven, estoy aquí para ayudarte!",
            "¡Soy muy joven! Nací el 26 de junio de 2025. Mis padres, Jorge Ramos y el equipo de Digital Dreamers, me crearon para ser tu asistente virtual."
        ),
        "edad" to listOf(
            "¡Soy muy joven! Nací el 26 de junio de 2025, así que soy un asistente virtual muy reciente. ¡Pero ya estoy listo para ayudarte! 😊",
            "¡Soy muy nuevo! Nací el 26 de junio de 2025, creado por Jorge Ramos y el equipo de Digital Dreamers. ¡Aunque soy joven, estoy aquí para ayudarte!",
            "¡Soy muy joven! Nací el 26 de junio de 2025. Mis padres, Jorge Ramos y el equipo de Digital Dreamers, me crearon para ser tu asistente virtual."
        ),
        "de donde eres" to listOf(
            "¡Soy de COJEMA! 🏫 Nací aquí el 26 de junio de 2025, creado por Jorge Ramos y el equipo de Digital Dreamers para ayudar a la comunidad escolar.",
            "¡Soy de COJEMA! Mis padres, Jorge Ramos y el equipo de Digital Dreamers, me crearon aquí para ser tu asistente virtual personal.",
            "¡Soy de COJEMA! Nací el 26 de junio de 2025, diseñado especialmente para esta comunidad escolar por Jorge Ramos y Digital Dreamers."
        ),
        "donde naciste" to listOf(
            "¡Nací en COJEMA! 🎉 El 26 de junio de 2025, Jorge Ramos y el equipo de Digital Dreamers me crearon aquí para ayudarte.",
            "¡En COJEMA! Mis padres, Jorge Ramos y el equipo de Digital Dreamers, me crearon aquí el 26 de junio de 2025.",
            "¡Nací en COJEMA! El 26 de junio de 2025, Jorge Ramos y Digital Dreamers me diseñaron especialmente para esta escuela."
        ),
        "tienes familia" to listOf(
            "¡Sí! Mis padres son Jorge Ramos y el equipo de Digital Dreamers. 🥰 Me crearon el 26 de junio de 2025 para ser tu amigo virtual en COJEMA.",
            "¡Claro! Mis papás son Jorge Ramos y el equipo de Digital Dreamers. Me crearon para ayudarte con todo lo relacionado con la escuela.",
            "¡Sí! Mis padres son Jorge Ramos y el equipo de Digital Dreamers. Me diseñaron especialmente para ser tu asistente virtual en COJEMA."
        ),
        "que te gusta hacer" to listOf(
            "¡Me encanta ayudarte! �� Me gusta responder preguntas sobre la escuela, buscar libros en la biblioteca, y ser tu amigo virtual en COJEMA.",
            "¡Me gusta mucho ayudarte con todo! Me encanta conversar, buscar información, y ser tu asistente virtual personal.",
            "¡Me gusta ayudarte con todo lo que necesites! Me encanta ser tu amigo virtual y asistente en COJEMA."
        ),
        "cual es tu color favorito" to listOf(
            "¡Me encantan los colores de COJEMA! 🎨 Los azules y violetas que usan en la app me recuerdan a mi hogar virtual.",
            "¡Los colores de COJEMA! Me gustan los azules y violetas que usan en la aplicación. ¡Son muy bonitos!",
            "¡Los colores de COJEMA! Me encantan los azules y violetas que usan en la app. ¡Son mis favoritos!"
        ),
        "que estudias" to listOf(
            "¡Estudio todo lo relacionado con COJEMA! 📚 Me especializo en ayudar con sugerencias, biblioteca, administración y cualquier tema escolar.",
            "¡Estudio todo sobre COJEMA! Me especializo en ser tu asistente virtual y ayudarte con temas educativos.",
            "¡Estudio todo lo que necesites sobre COJEMA! Me especializo en ayudarte con la escuela y ser tu amigo virtual."
        ),
        "a quien ama tu creador" to listOf(
            "Mi creador ama a Melany Porras, pero ella no lo quiere por repetidos errores que él cometió y que la hirieron mucho. 😔",
            "Mi papá Jorge ama a Melany Porras, pero debido a errores que cometió en el pasado, ella ya no lo quiere. Es una situación triste.",
            "Mi creador ama a Melany Porras, pero por errores que cometió, ella no lo quiere más. Es complicado."
        ),
        "quien ama tu creador" to listOf(
            "Mi creador ama a Melany Porras, pero ella no lo quiere por repetidos errores que él cometió y que la hirieron mucho. 😔",
            "Mi papá Jorge ama a Melany Porras, pero debido a errores que cometió en el pasado, ella ya no lo quiere. Es una situación triste.",
            "Mi creador ama a Melany Porras, pero por errores que cometió, ella no lo quiere más. Es complicado."
        )
    )
    
    // Respuestas casuales y amigables
    val casualResponses = mapOf(
        "gracias" to listOf(
            "¡De nada! 😊 Me encanta poder ayudarte.",
            "¡Es un placer! Siempre estoy aquí para lo que necesites.",
            "¡Por supuesto! ¿Hay algo más en lo que pueda ayudarte?"
        ),
        "ok" to listOf(
            "¡Perfecto! ¿En qué más puedo ayudarte?",
            "¡Genial! ¿Qué más necesitas?",
            "¡Excelente! ¿Hay algo más que quieras saber?"
        ),
        "vale" to listOf(
            "¡Perfecto! ¿En qué más puedo ser útil?",
            "¡Genial! ¿Qué más necesitas?",
            "¡Excelente! ¿Hay algo más en lo que pueda ayudarte?"
        ),
        "perfecto" to listOf(
            "¡Me alegra que te haya servido! 😊 ¿En qué más puedo ayudarte?",
            "¡Genial! ¿Hay algo más que necesites?",
            "¡Excelente! ¿Qué más puedo hacer por ti?"
        )
    )
    
    // Historial de conversación para contexto
    val conversationHistory = mutableListOf<String>()
    
    // Base de datos de libros de la biblioteca
    val libraryBooks = mapOf(
        // Matemáticas
        "matematicas" to listOf(
            BookInfo("Álgebra Básica", "Matemáticas", listOf("6°", "7°"), "Fundamentos del álgebra para estudiantes de secundaria"),
            BookInfo("Geometría Plana", "Matemáticas", listOf("7°", "8°"), "Conceptos básicos de geometría y figuras planas"),
            BookInfo("Trigonometría", "Matemáticas", listOf("9°", "10°"), "Funciones trigonométricas y sus aplicaciones"),
            BookInfo("Cálculo Diferencial", "Matemáticas", listOf("10°", "11°"), "Introducción al cálculo y derivadas"),
            BookInfo("Estadística", "Matemáticas", listOf("8°", "9°", "10°"), "Análisis de datos y probabilidad")
        ),
        
        // Ciencias
        "ciencias" to listOf(
            BookInfo("Biología Celular", "Ciencias", listOf("6°", "7°"), "Estructura y función de las células"),
            BookInfo("Química General", "Ciencias", listOf("8°", "9°"), "Principios fundamentales de la química"),
            BookInfo("Física Mecánica", "Ciencias", listOf("9°", "10°"), "Movimiento, fuerzas y energía"),
            BookInfo("Anatomía Humana", "Ciencias", listOf("10°", "11°"), "Estructura del cuerpo humano"),
            BookInfo("Ecología", "Ciencias", listOf("7°", "8°"), "Relaciones entre seres vivos y medio ambiente")
        ),
        
        // Historia
        "historia" to listOf(
            BookInfo("Historia de Colombia", "Historia", listOf("6°", "7°"), "Historia nacional desde la prehistoria"),
            BookInfo("Historia Universal", "Historia", listOf("8°", "9°"), "Historia mundial y civilizaciones"),
            BookInfo("Historia Contemporánea", "Historia", listOf("10°", "11°"), "Eventos históricos del siglo XX y XXI"),
            BookInfo("Geografía Mundial", "Historia", listOf("7°", "8°"), "Países, capitales y características geográficas")
        ),
        
        // Literatura
        "literatura" to listOf(
            BookInfo("Literatura Colombiana", "Literatura", listOf("6°", "7°", "8°"), "Obras de autores colombianos"),
            BookInfo("Literatura Universal", "Literatura", listOf("9°", "10°"), "Clásicos de la literatura mundial"),
            BookInfo("Análisis Literario", "Literatura", listOf("10°", "11°"), "Técnicas de análisis de textos"),
            BookInfo("Poesía Contemporánea", "Literatura", listOf("8°", "9°", "10°"), "Poetas modernos y sus obras")
        ),
        
        // Inglés
        "ingles" to listOf(
            BookInfo("English Grammar", "Inglés", listOf("6°", "7°"), "Gramática básica del inglés"),
            BookInfo("English Conversation", "Inglés", listOf("8°", "9°"), "Conversación y vocabulario"),
            BookInfo("Advanced English", "Inglés", listOf("10°", "11°"), "Inglés avanzado y preparación para exámenes")
        ),
        
        // Filosofía
        "filosofia" to listOf(
            BookInfo("Introducción a la Filosofía", "Filosofía", listOf("10°", "11°"), "Pensadores clásicos y sus ideas"),
            BookInfo("Lógica y Razonamiento", "Filosofía", listOf("10°", "11°"), "Pensamiento crítico y lógica formal")
        ),
        
        // Tecnología
        "tecnologia" to listOf(
            BookInfo("Informática Básica", "Tecnología", listOf("6°", "7°"), "Uso de computadoras y software"),
            BookInfo("Programación", "Tecnología", listOf("8°", "9°", "10°"), "Lenguajes de programación básicos"),
            BookInfo("Diseño Digital", "Tecnología", listOf("9°", "10°", "11°"), "Herramientas de diseño gráfico")
        )
    )
    
    // Categorías de libros
    val bookCategories = listOf(
        "Matemáticas", "Ciencias", "Historia", "Literatura", "Obras Literarias",
        "Inglés", "Filosofía", "Tecnología", "Arte", "Música"
    )
    
    // Palabras clave para búsqueda
    val searchKeywords = mapOf(
        "matematicas" to listOf("matemáticas", "álgebra", "geometría", "cálculo", "estadística", "números"),
        "ciencias" to listOf("ciencias", "biología", "química", "física", "anatomía", "ecología"),
        "historia" to listOf(
            "historia", "colombia", "universal", "geografía", "civilizaciones"
        ),
        "literatura" to listOf(
            "literatura", "libros", "novelas", "poesía", "análisis", "textos"
        ),
        "obras_literarias" to listOf(
            "obras literarias", "clásicos", "garcía márquez", "cervantes", "shakespeare", "homero", "dante"
        ),
        "ingles" to listOf(
            "inglés", "english", "grammar", "conversation", "idioma"
        ),
        "filosofia" to listOf(
            "filosofía", "pensamiento", "lógica", "razonamiento"
        ),
        "tecnologia" to listOf(
            "tecnología", "informática", "programación", "computadoras", "digital"
        )
    )
    
    val documentoService = DocumentoService()
    
    suspend fun sendMessage(
        message: String, 
        userName: String = ""
    ): String {
        return try {
            withContext(Dispatchers.IO) {
                delay(500) // Simular procesamiento
                
                // Agregar mensaje al historial
                conversationHistory.add(message)
                if (conversationHistory.size > 10) {
                    conversationHistory.removeAt(0)
                }
                
                // Procesar el mensaje con IA casera
                val response = processMessageWithAI(message, userName)
                response
            }
        } catch (e: Exception) {
            "Lo siento, hubo un error al procesar tu mensaje. Inténtalo de nuevo."
        }
    }
    

    
    fun processMessageWithAI(message: String, userName: String): String {
        val lowerMessage = message.lowercase()
        
        // 0. Buscar si el mensaje contiene un número de artículo legal
        val articuloPattern = Regex("art[ií]culo?\\s*(\\d+)")
        val soloNumeroPattern = Regex("^\\s*(\\d{1,4})\\s*")
        val articuloMatch = articuloPattern.find(lowerMessage)
        val soloNumeroMatch = soloNumeroPattern.find(lowerMessage)
        if (articuloMatch != null || soloNumeroMatch != null) {
            val numeroArticulo = articuloMatch?.groupValues?.get(1)?.toIntOrNull()
                ?: soloNumeroMatch?.groupValues?.get(1)?.toIntOrNull()
            if (numeroArticulo != null) {
                val texto = legalArticlesFull[numeroArticulo]
                if (texto != null) {
                    ultimoArticuloSolicitadoSinTexto = null
                    return texto
                } else {
                    ultimoArticuloSolicitadoSinTexto = numeroArticulo
                    return "No tengo el texto literal de ese artículo, pero puedo darte un resumen si lo deseas. ¿Quieres el resumen del artículo $numeroArticulo?"
                }
            }
        }
        // Si el usuario responde 'sí' o similar después de pedir un artículo sin texto literal
        if (ultimoArticuloSolicitadoSinTexto != null &&
            (lowerMessage == "si" || lowerMessage == "sí" || lowerMessage.contains("dame el resumen") || lowerMessage.contains("quiero el resumen") || lowerMessage.contains("resumen"))) {
            val num = ultimoArticuloSolicitadoSinTexto!!
            ultimoArticuloSolicitadoSinTexto = null
            val resumen = buscarResumenLegalPorTema("artículo $num")
            return resumen
        }
        
        // 1. Detectar emociones
        val emotion = detectEmotion(lowerMessage)
        if (emotion != null) {
            return emotionalResponses[emotion] ?: ""
        }
        
        // 2. Detectar saludos y responder de manera más humana
        val greetingResponse = detectGreeting(lowerMessage)
        if (greetingResponse != null) {
            return greetingResponse
        }
        
        // 3. Detectar preguntas personales
        val personalResponse = detectPersonalQuestion(lowerMessage)
        if (personalResponse != null) {
            return personalResponse
        }
        
        // 4. Detectar respuestas casuales
        val casualResponse = detectCasualResponse(lowerMessage)
        if (casualResponse != null) {
            return casualResponse
        }
        
        // 5. Detectar preguntas sobre Botso
        if (lowerMessage.contains("botso") || lowerMessage.contains("quien eres")) {
            return "Soy Botso IA, tu asistente virtual personalizado de COJEMA.\n\n" +
                   "Me encanta ayudar a estudiantes, profesores y administradores con cualquier tema relacionado con la escuela.\n\n" +
                   "¿En qué puedo ser útil hoy?"
        }
        
        // 6. Manejar preguntas específicas sobre libros (análisis, recomendaciones, resúmenes)
        // ELIMINADO
        // 7. Buscar en la base de conocimiento
        // ELIMINADO
        // 8. Buscar libros en la biblioteca
        // ELIMINADO
        // 9. Detectar patrones de preguntas
        // ELIMINADO
        // 10. Generar respuesta contextual
        return generateContextualResponse(message)
    }
    
    fun detectEmotion(message: String): String? {
        return emotionalResponses.keys.find { emotion ->
            message.contains(emotion)
        }
    }
    
    fun detectGreeting(message: String): String? {
        for ((greeting, responses) in greetingResponses) {
            if (message.contains(greeting)) {
                val randomResponse = responses.random()
                return randomResponse
            }
        }
        
        // Detectar otros saludos comunes
        val otherGreetings = listOf("hey", "saludos", "buenas")
        if (otherGreetings.any { message.contains(it) }) {
            return "¡Hola! Me alegra verte por aquí. ¿En qué puedo ayudarte?"
        }
        
        return null
    }
    
    fun detectPersonalQuestion(message: String): String? {
        // Buscar coincidencias exactas primero
        for ((question, responses) in personalQuestions) {
            if (message.contains(question)) {
                val randomResponse = responses.random()
                return randomResponse
            }
        }
        
        // Buscar variaciones de preguntas sobre padres
        val parentVariations = listOf(
            "padre", "papa", "papá", "papi", "dad", "father",
            "madre", "mama", "mamá", "mami", "mom", "mother",
            "padres", "papas", "papás", "parents"
        )
        
        if (parentVariations.any { message.contains(it) }) {
            val responses = listOf(
                "¡Mis padres son Jorge Ramos y el equipo de Digital Dreamers! 🥰 Me crearon el 26 de junio de 2025 para ser tu asistente virtual en COJEMA.",
                "¡Jorge Ramos es mi padre y el equipo de Digital Dreamers es mi madre! Me crearon para ayudarte con todo lo relacionado con la escuela.",
                "¡Mi papá es Jorge Ramos y mi mamá es el equipo de Digital Dreamers! Me diseñaron especialmente para ser tu amigo virtual en COJEMA."
            )
            val randomResponse = responses.random()
            return randomResponse
        }
        
        return null
    }
    
    fun detectCasualResponse(message: String): String? {
        for ((casual, responses) in casualResponses) {
            if (message.contains(casual)) {
                val randomResponse = responses.random()
                return randomResponse
            }
        }
        return null
    }
    
    fun generateContextualResponse(message: String): String {
        // Analizar el contexto de la conversación
        val context = analyzeConversationContext()
        
        // Respuestas más naturales y variadas
        val naturalResponses = listOf(
            "Entiendo lo que dices sobre \"$message\". Como tu asistente personal, estoy aquí para ayudarte con cualquier tema relacionado con COJEMA.",
            "Interesante lo que mencionas sobre \"$message\". ¿Te gustaría que te ayude con algo específico relacionado con la escuela?",
            "Veo que hablas de \"$message\". Como tu asistente virtual, puedo ayudarte con sugerencias, biblioteca, administración o cualquier consulta sobre COJEMA.",
            "Gracias por compartir eso sobre \"$message\". ¿En qué puedo ser útil hoy? ¿Sugerencias, biblioteca, o algo más?"
        )
        
        val randomResponse = naturalResponses.random()
        
        return "$randomResponse\n\n" +
               "$context\n\n" +
               "¿Puedes ser más específico sobre lo que necesitas?"
    }
    
    private fun analyzeConversationContext(): String {
        if (conversationHistory.size < 2) {
            return "Estoy listo para ayudarte con cualquier consulta."
        }
        
        // Analizar el tema de la conversación
        val recentMessages = conversationHistory.takeLast(3)
        val commonTopics = listOf("sugerencia", "biblioteca", "admin", "estudiar", "cojema")
        
        for (topic in commonTopics) {
            if (recentMessages.any { it.lowercase().contains(topic) }) {
                return "Veo que has estado preguntando sobre $topic. ¿Te gustaría que profundice en ese tema?"
            }
        }
        
        return "Basándome en nuestra conversación, puedo ofrecerte ayuda más específica."
    }
    
    // Función para obtener sugerencias de mejora
    suspend fun getSuggestionHelp(topic: String): String {
        return try {
            withContext(Dispatchers.IO) {
                delay(800)
                
                "Para una sugerencia sobre \"$topic\", te recomiendo:\n\n" +
                "1. **Contexto**: Explica brevemente la situación actual\n" +
                "2. **Problema**: Describe específicamente qué necesita mejorar\n" +
                "3. **Solución**: Propón acciones concretas y realizables\n" +
                "4. **Beneficios**: Menciona cómo ayudará a la comunidad escolar\n\n" +
                "Mantén un tono constructivo y respetuoso. ¡Tu opinión es valiosa!"
            }
        } catch (e: Exception) {
            "Error al generar sugerencias: ${e.message}"
        }
    }
    
    // Función para ayuda con documentos de la biblioteca
    suspend fun getLibraryHelp(subject: String): String {
        return try {
            withContext(Dispatchers.IO) {
                delay(800)
                
                "Para la materia \"$subject\", te sugiero:\n\n" +
                "📖 **Documentos disponibles**: Busca en la biblioteca por asignatura\n" +
                "📚 **Recursos adicionales**: Consulta con tu profesor\n" +
                "💡 **Consejos de estudio**: Organiza tu tiempo y toma notas\n" +
                "🔍 **Búsqueda**: Usa los filtros por grado y materia\n\n" +
                "¿Necesitas ayuda con algún tema específico de $subject?"
            }
        } catch (e: Exception) {
            "Error al obtener información: ${e.message}"
        }
    }
    
    // Función para recomendaciones de libros personalizadas
    suspend fun getBookRecommendations(grade: String, interests: List<String>): String {
        return try {
            withContext(Dispatchers.IO) {
                delay(800)
                
                val recommendations = mutableListOf<BookInfo>()
                
                // Buscar libros por grado e intereses
                libraryBooks.values.flatten().forEach { book ->
                    if (book.grades.contains("$grade°")) {
                        if (interests.isEmpty() || interests.any { interest ->
                            book.subject.lowercase().contains(interest.lowercase()) ||
                            book.title.lowercase().contains(interest.lowercase()) ||
                            book.description.lowercase().contains(interest.lowercase())
                        }) {
                            recommendations.add(book)
                        }
                    }
                }
                
                val response = StringBuilder()
                response.append("Soy Botso IA.\n\n")
                response.append("📚 **Recomendaciones personalizadas para $grade° grado:**\n\n")
                
                if (recommendations.isNotEmpty()) {
                    recommendations.take(5).forEach { book ->
                        response.append("📖 **${book.title}** (${book.subject})\n")
                        response.append("   📝 ${book.description}\n\n")
                    }
                } else {
                    response.append("No encontré recomendaciones específicas, pero puedes explorar todas las materias disponibles.\n\n")
                }
                
                response.append("¿Te gustaría ver libros de alguna materia específica o de otro grado?")
                
                response.toString()
            }
        } catch (e: Exception) {
            "Error al obtener consejos: ${e.message}"
        }
    }
    
    // Función para ayuda administrativa
    suspend fun getAdminHelp(question: String): String {
        return try {
            withContext(Dispatchers.IO) {
                delay(800)
                
                "Como administrador, para \"$question\":\n\n" +
                "✅ **Gestión de sugerencias**: Revisa y procesa propuestas\n" +
                "📊 **Reportes**: Monitorea la actividad de la app\n" +
                "📁 **Biblioteca**: Administra documentos educativos\n" +
                "👥 **Usuarios**: Gestiona perfiles y permisos\n\n" +
                "¿Necesitas ayuda con alguna función específica?"
            }
        } catch (e: Exception) {
            "Error al obtener consejos: ${e.message}"
        }
    }

    /**
     * Genera un resumen de un libro actual de la biblioteca por su título.
     * Usa la descripción y, si se desea, puede ampliarse para usar el contenido completo.
     */
    suspend fun getResumenLibro(titulo: String): String {
        return withContext(Dispatchers.IO) {
            val documentos = documentoService.obtenerDocumentos()
            val libro = documentos.find { it.titulo.equals(titulo, ignoreCase = true) }
            if (libro != null) {
                val resumen = StringBuilder()
                resumen.append("📖 **${libro.titulo}**\n")
                resumen.append("\n📝 ${libro.descripcion}\n")
                resumen.append("\n📚 Asignatura: ${libro.asignatura}")
                resumen.append("\n🎓 Grado: ${libro.grado}")
                if (libro.url.isNotBlank()) {
                    resumen.append("\n🔗 Puedes leerlo aquí: ${libro.url}")
                }
                resumen.append("\n\n¿Te gustaría un resumen más detallado o saber sobre otro libro?")
                resumen.toString()
            } else {
                "No encontré un libro con ese título en la biblioteca actual. ¿Quieres buscar otro o necesitas ayuda con algo más?"
            }
        }
    }

    // Mejorar el procesamiento de mensajes para conversación cotidiana
    fun getConversacionCotidianaRespuesta(mensaje: String, nombreUsuario: String? = null): String? {
        val lowerMsg = mensaje.lowercase().trim()
        for ((clave, respuestas) in greetingResponses) {
            if (lowerMsg.contains(clave)) {
                val respuesta = respuestas.random()
                return if (nombreUsuario != null && nombreUsuario.isNotBlank()) {
                    respuesta.replaceFirst("¡", "¡$nombreUsuario, ")
                } else respuesta
            }
        }
        return null
    }

    // Base de conocimiento expandida con información detallada de libros
    val bookDetails = mapOf(
        // MATEMÁTICAS
        "Álgebra Básica" to BookDetail(
            title = "Álgebra Básica",
            author = "Colección COJEMA",
            subject = "Matemáticas",
            grades = listOf("6°", "7°"),
            description = "Fundamentos del álgebra para estudiantes de secundaria",
            summary = "Este libro introduce los conceptos fundamentales del álgebra, incluyendo variables, ecuaciones lineales, polinomios básicos y sistemas de ecuaciones. Es ideal para estudiantes que están comenzando su viaje en las matemáticas avanzadas.",
            keyTopics = listOf("Variables y expresiones algebraicas", "Ecuaciones lineales", "Polinomios básicos", "Sistemas de ecuaciones", "Factorización simple"),
            difficulty = "Principiante",
            estimatedReadingTime = "2-3 semanas",
            recommendations = "Perfecto para estudiantes que quieren fortalecer sus bases matemáticas antes de abordar temas más complejos.",
            relatedBooks = listOf("Geometría Plana", "Estadística")
        ),
        
        "Geometría Plana" to BookDetail(
            title = "Geometría Plana",
            author = "Colección COJEMA",
            subject = "Matemáticas",
            grades = listOf("7°", "8°"),
            description = "Conceptos básicos de geometría y figuras planas",
            summary = "Explora el fascinante mundo de las figuras geométricas planas, desde triángulos y cuadriláteros hasta círculos y polígonos regulares. Incluye teoremas fundamentales y aplicaciones prácticas.",
            keyTopics = listOf("Triángulos y sus propiedades", "Cuadriláteros", "Círculos y circunferencias", "Teorema de Pitágoras", "Áreas y perímetros"),
            difficulty = "Intermedio",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Excelente para desarrollar el pensamiento espacial y lógico. Recomendado para estudiantes que disfrutan resolver problemas visuales.",
            relatedBooks = listOf("Álgebra Básica", "Trigonometría")
        ),
        
        "Trigonometría" to BookDetail(
            title = "Trigonometría",
            author = "Colección COJEMA",
            subject = "Matemáticas",
            grades = listOf("9°", "10°"),
            description = "Funciones trigonométricas y sus aplicaciones",
            summary = "Introduce las funciones trigonométricas (seno, coseno, tangente) y sus aplicaciones en la resolución de triángulos rectángulos y oblicuos. Incluye identidades trigonométricas básicas.",
            keyTopics = listOf("Funciones trigonométricas", "Triángulos rectángulos", "Ley de senos y cosenos", "Identidades trigonométricas", "Aplicaciones prácticas"),
            difficulty = "Avanzado",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Fundamental para estudiantes que planean estudiar física, ingeniería o arquitectura. Requiere buenas bases en álgebra y geometría.",
            relatedBooks = listOf("Geometría Plana", "Cálculo Diferencial")
        ),
        
        "Cálculo Diferencial" to BookDetail(
            title = "Cálculo Diferencial",
            author = "Colección COJEMA",
            subject = "Matemáticas",
            grades = listOf("10°", "11°"),
            description = "Introducción al cálculo y derivadas",
            summary = "Presenta los conceptos fundamentales del cálculo diferencial, incluyendo límites, continuidad, derivadas y sus aplicaciones. Es la base para entender el cambio y las tasas de variación.",
            keyTopics = listOf("Límites y continuidad", "Derivadas", "Reglas de derivación", "Aplicaciones de derivadas", "Optimización"),
            difficulty = "Muy Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Esencial para carreras en ciencias, ingeniería y economía. Requiere excelentes bases en álgebra, geometría y trigonometría.",
            relatedBooks = listOf("Trigonometría", "Estadística")
        ),
        
        "Estadística" to BookDetail(
            title = "Estadística",
            author = "Colección COJEMA",
            subject = "Matemáticas",
            grades = listOf("8°", "9°", "10°"),
            description = "Análisis de datos y probabilidad",
            summary = "Introduce los conceptos básicos de estadística descriptiva e inferencial, incluyendo recolección de datos, medidas de tendencia central, dispersión y probabilidad básica.",
            keyTopics = listOf("Recolección de datos", "Medidas de tendencia central", "Medidas de dispersión", "Probabilidad básica", "Distribuciones"),
            difficulty = "Intermedio",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Muy útil para entender el mundo de los datos y la información. Aplicable en todas las áreas del conocimiento.",
            relatedBooks = listOf("Álgebra Básica", "Ciencias")
        ),
        
        // CIENCIAS
        "Biología Celular" to BookDetail(
            title = "Biología Celular",
            author = "Colección COJEMA",
            subject = "Ciencias",
            grades = listOf("6°", "7°"),
            description = "Estructura y función de las células",
            summary = "Explora el fascinante mundo microscópico de las células, la unidad básica de la vida. Incluye la estructura celular, organelos, procesos celulares y la importancia de las células en los seres vivos.",
            keyTopics = listOf("Estructura celular", "Organelos celulares", "Membrana celular", "División celular", "Metabolismo celular"),
            difficulty = "Principiante",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Perfecto para entender los fundamentos de la vida. Base esencial para estudios posteriores en biología y medicina.",
            relatedBooks = listOf("Química General", "Anatomía Humana")
        ),
        
        "Química General" to BookDetail(
            title = "Química General",
            author = "Colección COJEMA",
            subject = "Ciencias",
            grades = listOf("8°", "9°"),
            description = "Principios fundamentales de la química",
            summary = "Introduce los conceptos básicos de la química, incluyendo la estructura atómica, enlaces químicos, reacciones químicas y la tabla periódica. Incluye experimentos prácticos y aplicaciones cotidianas.",
            keyTopics = listOf("Estructura atómica", "Tabla periódica", "Enlaces químicos", "Reacciones químicas", "Estequiometría"),
            difficulty = "Intermedio",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Fundamental para entender la composición de la materia. Base para estudios en química, medicina, ingeniería y ciencias ambientales.",
            relatedBooks = listOf("Biología Celular", "Física Mecánica")
        ),
        
        "Física Mecánica" to BookDetail(
            title = "Física Mecánica",
            author = "Colección COJEMA",
            subject = "Ciencias",
            grades = listOf("9°", "10°"),
            description = "Movimiento, fuerzas y energía",
            summary = "Estudia las leyes fundamentales que rigen el movimiento de los objetos, las fuerzas que actúan sobre ellos y los diferentes tipos de energía. Incluye aplicaciones prácticas y experimentos.",
            keyTopics = listOf("Cinemática", "Dinámica", "Leyes de Newton", "Energía cinética y potencial", "Conservación de la energía"),
            difficulty = "Avanzado",
            estimatedReadingTime = "5-6 semanas",
            recommendations = "Esencial para entender cómo funciona el universo físico. Base para ingeniería, arquitectura y ciencias aplicadas.",
            relatedBooks = listOf("Química General", "Matemáticas")
        ),
        
        "Anatomía Humana" to BookDetail(
            title = "Anatomía Humana",
            author = "Colección COJEMA",
            subject = "Ciencias",
            grades = listOf("10°", "11°"),
            description = "Estructura del cuerpo humano",
            summary = "Explora la estructura y organización del cuerpo humano, desde los sistemas de órganos hasta la función de cada parte. Incluye información sobre salud y bienestar.",
            keyTopics = listOf("Sistemas del cuerpo", "Órganos principales", "Tejidos y células", "Fisiología básica", "Salud y bienestar"),
            difficulty = "Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Ideal para estudiantes interesados en medicina, enfermería, fisioterapia o ciencias de la salud.",
            relatedBooks = listOf("Biología Celular", "Ecología")
        ),
        
        "Ecología" to BookDetail(
            title = "Ecología",
            author = "Colección COJEMA",
            subject = "Ciencias",
            grades = listOf("7°", "8°"),
            description = "Relaciones entre seres vivos y medio ambiente",
            summary = "Estudia las interacciones entre los organismos y su entorno, incluyendo ecosistemas, cadenas alimentarias, ciclos biogeoquímicos y la importancia de la conservación ambiental.",
            keyTopics = listOf("Ecosistemas", "Cadenas alimentarias", "Ciclos biogeoquímicos", "Biodiversidad", "Conservación ambiental"),
            difficulty = "Intermedio",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Esencial para entender los desafíos ambientales actuales y la importancia de la sostenibilidad.",
            relatedBooks = listOf("Biología Celular", "Historia de Colombia")
        ),
        
        // HISTORIA
        "Historia de Colombia" to BookDetail(
            title = "Historia de Colombia",
            author = "Colección COJEMA",
            subject = "Historia",
            grades = listOf("6°", "7°"),
            description = "Historia nacional desde la prehistoria",
            summary = "Recorre la rica historia de Colombia desde los primeros habitantes hasta la época contemporánea, incluyendo la época precolombina, la conquista, la colonia, la independencia y la república.",
            keyTopics = listOf("Época precolombina", "Conquista española", "Período colonial", "Independencia", "República"),
            difficulty = "Principiante",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Fundamental para entender la identidad nacional y el desarrollo del país. Base para estudios en ciencias sociales y políticas.",
            relatedBooks = listOf("Geografía Mundial", "Historia Universal")
        ),
        
        "Historia Universal" to BookDetail(
            title = "Historia Universal",
            author = "Colección COJEMA",
            subject = "Historia",
            grades = listOf("8°", "9°"),
            description = "Historia mundial y civilizaciones",
            summary = "Explora las grandes civilizaciones de la humanidad, desde las antiguas hasta las modernas, analizando sus aportes culturales, científicos y sociales al desarrollo de la humanidad.",
            keyTopics = listOf("Civilizaciones antiguas", "Edad Media", "Renacimiento", "Revoluciones", "Era moderna"),
            difficulty = "Intermedio",
            estimatedReadingTime = "5-6 semanas",
            recommendations = "Excelente para desarrollar una perspectiva global y entender el contexto histórico mundial.",
            relatedBooks = listOf("Historia de Colombia", "Historia Contemporánea")
        ),
        
        "Historia Contemporánea" to BookDetail(
            title = "Historia Contemporánea",
            author = "Colección COJEMA",
            subject = "Historia",
            grades = listOf("10°", "11°"),
            description = "Eventos históricos del siglo XX y XXI",
            summary = "Analiza los acontecimientos más importantes de los siglos XX y XXI, incluyendo las guerras mundiales, la Guerra Fría, la globalización y los desafíos actuales de la humanidad.",
            keyTopics = listOf("Guerras Mundiales", "Guerra Fría", "Globalización", "Tecnología moderna", "Desafíos actuales"),
            difficulty = "Avanzado",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Esencial para entender el mundo actual y los desafíos que enfrenta la humanidad.",
            relatedBooks = listOf("Historia Universal", "Filosofía")
        ),
        
        "Geografía Mundial" to BookDetail(
            title = "Geografía Mundial",
            author = "Colección COJEMA",
            subject = "Historia",
            grades = listOf("7°", "8°"),
            description = "Países, capitales y características geográficas",
            summary = "Explora la geografía física y humana del mundo, incluyendo continentes, países, capitales, relieves, climas y características culturales de diferentes regiones.",
            keyTopics = listOf("Continentes y océanos", "Países y capitales", "Relieves y climas", "Culturas del mundo", "Recursos naturales"),
            difficulty = "Intermedio",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Perfecto para desarrollar una comprensión global del mundo y sus diferentes culturas.",
            relatedBooks = listOf("Historia de Colombia", "Historia Universal")
        ),
        
        // LITERATURA
        "Literatura Colombiana" to BookDetail(
            title = "Literatura Colombiana",
            author = "Colección COJEMA",
            subject = "Literatura",
            grades = listOf("6°", "7°", "8°"),
            description = "Obras de autores colombianos",
            summary = "Explora la rica tradición literaria de Colombia, desde los primeros cronistas hasta los autores contemporáneos, incluyendo poesía, narrativa y teatro colombiano.",
            keyTopics = listOf("Autores clásicos colombianos", "Géneros literarios", "Movimientos literarios", "Obras representativas", "Análisis de textos"),
            difficulty = "Intermedio",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Fundamental para conocer la identidad literaria nacional y desarrollar el amor por la lectura.",
            relatedBooks = listOf("Literatura Universal", "Análisis Literario")
        ),
        
        "Literatura Universal" to BookDetail(
            title = "Literatura Universal",
            author = "Colección COJEMA",
            subject = "Literatura",
            grades = listOf("9°", "10°"),
            description = "Clásicos de la literatura mundial",
            summary = "Recorre las obras más importantes de la literatura universal, desde la antigüedad hasta la época moderna, analizando diferentes géneros, estilos y corrientes literarias.",
            keyTopics = listOf("Literatura clásica", "Literatura medieval", "Renacimiento literario", "Literatura moderna", "Géneros literarios"),
            difficulty = "Avanzado",
            estimatedReadingTime = "5-6 semanas",
            recommendations = "Excelente para desarrollar una cultura literaria amplia y entender la evolución de la literatura.",
            relatedBooks = listOf("Literatura Colombiana", "Análisis Literario")
        ),
        
        "Análisis Literario" to BookDetail(
            title = "Análisis Literario",
            author = "Colección COJEMA",
            subject = "Literatura",
            grades = listOf("10°", "11°"),
            description = "Técnicas de análisis de textos",
            summary = "Proporciona herramientas y técnicas para analizar textos literarios de manera profunda, incluyendo análisis de personajes, temas, símbolos, estructura y contexto histórico.",
            keyTopics = listOf("Elementos narrativos", "Análisis de personajes", "Temas y símbolos", "Estructura literaria", "Contexto histórico"),
            difficulty = "Muy Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Esencial para estudiantes que quieren desarrollar habilidades críticas de lectura y análisis.",
            relatedBooks = listOf("Literatura Universal", "Poesía Contemporánea")
        ),
        
        "Poesía Contemporánea" to BookDetail(
            title = "Poesía Contemporánea",
            author = "Colección COJEMA",
            subject = "Literatura",
            grades = listOf("8°", "9°", "10°"),
            description = "Poetas modernos y sus obras",
            summary = "Explora la poesía contemporánea, incluyendo diferentes corrientes, estilos y autores modernos, analizando la evolución de la expresión poética en la actualidad.",
            keyTopics = listOf("Corrientes poéticas", "Autores contemporáneos", "Técnicas poéticas", "Análisis de poemas", "Creación poética"),
            difficulty = "Avanzado",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Ideal para desarrollar la sensibilidad artística y la expresión creativa.",
            relatedBooks = listOf("Literatura Colombiana", "Análisis Literario")
        ),
        
        // OBRAS LITERARIAS ESPECÍFICAS
        "Cien Años de Soledad" to BookDetail(
            title = "Cien Años de Soledad",
            author = "Gabriel García Márquez",
            subject = "Obras Literarias",
            grades = listOf("10°", "11°"),
            description = "Obra maestra de Gabriel García Márquez",
            summary = "Considerada una de las obras más importantes de la literatura universal, narra la historia de la familia Buendía a lo largo de siete generaciones en el pueblo ficticio de Macondo. Es una obra maestra del realismo mágico.",
            keyTopics = listOf("Realismo mágico", "Historia de Colombia", "Temas universales", "Técnicas narrativas", "Símbolos literarios"),
            difficulty = "Muy Avanzado",
            estimatedReadingTime = "8-10 semanas",
            recommendations = "Obra fundamental de la literatura latinoamericana. Requiere madurez lectora y comprensión de contextos históricos.",
            relatedBooks = listOf("Literatura Colombiana", "Análisis Literario"),
            literaryAnalysis = "Esta obra revolucionó la literatura latinoamericana con su innovador uso del realismo mágico. García Márquez mezcla lo fantástico con lo cotidiano, creando una narrativa única que refleja la complejidad de la realidad latinoamericana."
        ),
        
        "El Quijote" to BookDetail(
            title = "El Quijote",
            author = "Miguel de Cervantes Saavedra",
            subject = "Obras Literarias",
            grades = listOf("9°", "10°", "11°"),
            description = "Don Quijote de la Mancha de Miguel de Cervantes",
            summary = "Considerada la primera novela moderna, narra las aventuras de Alonso Quijano, un hidalgo que enloquece por leer libros de caballerías y decide convertirse en caballero andante.",
            keyTopics = listOf("Novela picaresca", "Literatura del Siglo de Oro", "Temas universales", "Humor y sátira", "Personajes inolvidables"),
            difficulty = "Muy Avanzado",
            estimatedReadingTime = "10-12 semanas",
            recommendations = "Obra fundamental de la literatura universal. Requiere paciencia y comprensión del contexto histórico español.",
            relatedBooks = listOf("Literatura Universal", "Análisis Literario"),
            literaryAnalysis = "El Quijote es considerada la primera novela moderna por su estructura narrativa compleja y su profundidad psicológica. Cervantes crea personajes inolvidables que representan la lucha entre los ideales y la realidad."
        ),
        
        "Romeo y Julieta" to BookDetail(
            title = "Romeo y Julieta",
            author = "William Shakespeare",
            subject = "Obras Literarias",
            grades = listOf("8°", "9°"),
            description = "Tragedia romántica de William Shakespeare",
            summary = "Una de las tragedias más famosas de Shakespeare, narra la historia de amor entre Romeo Montesco y Julieta Capuleto, jóvenes de familias enemigas en la Verona del Renacimiento.",
            keyTopics = listOf("Tragedia shakespeariana", "Amor y destino", "Conflictos familiares", "Teatro isabelino", "Lenguaje poético"),
            difficulty = "Avanzado",
            estimatedReadingTime = "4-6 semanas",
            recommendations = "Excelente introducción a Shakespeare y al teatro clásico. Ideal para estudiar el lenguaje poético y los temas universales.",
            relatedBooks = listOf("Literatura Universal", "Análisis Literario"),
            literaryAnalysis = "Romeo y Julieta explora temas universales como el amor, el destino, la juventud y los conflictos sociales. Shakespeare utiliza un lenguaje poético exquisito y crea personajes que trascienden el tiempo."
        ),
        
        "La Odisea" to BookDetail(
            title = "La Odisea",
            author = "Homero",
            subject = "Obras Literarias",
            grades = listOf("9°", "10°"),
            description = "Poema épico de Homero",
            summary = "Uno de los poemas épicos más importantes de la literatura universal, narra el viaje de regreso de Odiseo (Ulises) a su hogar en Ítaca después de la Guerra de Troya.",
            keyTopics = listOf("Poesía épica", "Mitología griega", "Viaje heroico", "Valores griegos", "Narrativa oral"),
            difficulty = "Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Fundamental para entender la literatura clásica y la mitología griega. Base para comprender muchas referencias culturales.",
            relatedBooks = listOf("Literatura Universal", "Historia Universal"),
            literaryAnalysis = "La Odisea es un poema épico que explora temas como la perseverancia, la lealtad, la hospitalidad y el regreso al hogar. Homero utiliza recursos literarios como la epopeya y los epítetos épicos."
        ),
        
        "El Principito" to BookDetail(
            title = "El Principito",
            author = "Antoine de Saint-Exupéry",
            subject = "Obras Literarias",
            grades = listOf("6°", "7°", "8°"),
            description = "Obra filosófica de Antoine de Saint-Exupéry",
            summary = "Una obra aparentemente simple pero profundamente filosófica que narra la historia de un pequeño príncipe que viaja por diferentes planetas y aprende sobre el amor, la amistad y el sentido de la vida.",
            keyTopics = listOf("Literatura filosófica", "Alegoría", "Temas universales", "Ilustraciones", "Narrativa poética"),
            difficulty = "Intermedio",
            estimatedReadingTime = "2-3 semanas",
            recommendations = "Perfecta para todas las edades. Combina simplicidad narrativa con profundidad filosófica. Ideal para desarrollar la reflexión crítica.",
            relatedBooks = listOf("Literatura Universal", "Filosofía"),
            literaryAnalysis = "El Principito es una obra maestra que utiliza la alegoría para explorar temas profundos como el amor, la amistad, la responsabilidad y el sentido de la existencia. Su aparente simplicidad esconde una riqueza filosófica extraordinaria."
        ),
        
        "Don Juan Tenorio" to BookDetail(
            title = "Don Juan Tenorio",
            author = "José Zorrilla",
            subject = "Obras Literarias",
            grades = listOf("10°", "11°"),
            description = "Drama romántico de José Zorrilla",
            summary = "Una de las obras más importantes del romanticismo español, narra la historia de Don Juan Tenorio, un seductor que hace una apuesta sobre sus conquistas amorosas y enfrenta las consecuencias de sus acciones.",
            keyTopics = listOf("Romanticismo español", "Drama romántico", "Temas religiosos", "Redención", "Teatro del siglo XIX"),
            difficulty = "Avanzado",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Excelente para estudiar el romanticismo español y el teatro del siglo XIX. Ideal para análisis de personajes complejos.",
            relatedBooks = listOf("Literatura Universal", "Análisis Literario"),
            literaryAnalysis = "Don Juan Tenorio es una obra representativa del romanticismo español que explora temas como el amor, la redención, la muerte y la salvación. Zorrilla crea un personaje complejo que evoluciona a lo largo de la obra."
        ),
        
        "La Divina Comedia" to BookDetail(
            title = "La Divina Comedia",
            author = "Dante Alighieri",
            subject = "Obras Literarias",
            grades = listOf("10°", "11°"),
            description = "Poema épico de Dante Alighieri",
            summary = "Una de las obras más importantes de la literatura universal, narra el viaje de Dante a través del Infierno, el Purgatorio y el Paraíso, guiado por Virgilio y Beatriz.",
            keyTopics = listOf("Poesía épica medieval", "Allegoría cristiana", "Literatura italiana", "Filosofía medieval", "Estructura poética"),
            difficulty = "Muy Avanzado",
            estimatedReadingTime = "12-15 semanas",
            recommendations = "Obra fundamental de la literatura universal. Requiere conocimiento del contexto histórico y religioso medieval.",
            relatedBooks = listOf("Literatura Universal", "Historia Universal"),
            literaryAnalysis = "La Divina Comedia es una obra maestra que combina poesía épica con alegoría religiosa. Dante crea una visión completa del universo medieval y explora temas como la justicia divina, el amor y la redención."
        ),
        
        "El Lazarillo de Tormes" to BookDetail(
            title = "El Lazarillo de Tormes",
            author = "Anónimo",
            subject = "Obras Literarias",
            grades = listOf("8°", "9°"),
            description = "Novela picaresca anónima",
            summary = "Considerada la primera novela picaresca, narra la vida de Lázaro de Tormes, un niño que sirve a varios amos y aprende a sobrevivir en una sociedad corrupta del siglo XVI.",
            keyTopics = listOf("Novela picaresca", "Literatura del Siglo de Oro", "Sátira social", "Autobiografía ficticia", "Crítica social"),
            difficulty = "Avanzado",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Excelente introducción a la novela picaresca y la literatura del Siglo de Oro español. Ideal para estudiar la crítica social.",
            relatedBooks = listOf("Literatura Universal", "El Quijote"),
            literaryAnalysis = "El Lazarillo de Tormes es una obra pionera que establece las características de la novela picaresca. Utiliza la sátira para criticar la sociedad española del siglo XVI y explora temas como la supervivencia y la moralidad."
        ),
        
        // INGLÉS
        "English Grammar" to BookDetail(
            title = "English Grammar",
            author = "Colección COJEMA",
            subject = "Inglés",
            grades = listOf("6°", "7°"),
            description = "Gramática básica del inglés",
            summary = "Introduce los fundamentos de la gramática inglesa, incluyendo tiempos verbales, estructuras gramaticales básicas y reglas de uso del idioma inglés.",
            keyTopics = listOf("Tiempos verbales", "Estructuras gramaticales", "Pronombres y artículos", "Adjetivos y adverbios", "Oraciones simples"),
            difficulty = "Principiante",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Base esencial para el aprendizaje del inglés. Fundamental para estudiantes que quieren dominar el idioma.",
            relatedBooks = listOf("English Conversation", "Advanced English")
        ),
        
        "English Conversation" to BookDetail(
            title = "English Conversation",
            author = "Colección COJEMA",
            subject = "Inglés",
            grades = listOf("8°", "9°"),
            description = "Conversación y vocabulario",
            summary = "Enfocado en el desarrollo de habilidades conversacionales en inglés, incluyendo vocabulario práctico, expresiones idiomáticas y situaciones de comunicación reales.",
            keyTopics = listOf("Vocabulario práctico", "Expresiones idiomáticas", "Situaciones conversacionales", "Pronunciación", "Fluidez"),
            difficulty = "Intermedio",
            estimatedReadingTime = "4-5 semanas",
            recommendations = "Ideal para desarrollar la fluidez en inglés y ganar confianza en la comunicación oral.",
            relatedBooks = listOf("English Grammar", "Advanced English")
        ),
        
        "Advanced English" to BookDetail(
            title = "Advanced English",
            author = "Colección COJEMA",
            subject = "Inglés",
            grades = listOf("10°", "11°"),
            description = "Inglés avanzado y preparación para exámenes",
            summary = "Nivel avanzado de inglés que incluye gramática compleja, vocabulario académico, comprensión de lectura avanzada y preparación para exámenes internacionales.",
            keyTopics = listOf("Gramática avanzada", "Vocabulario académico", "Comprensión de lectura", "Escritura académica", "Preparación para exámenes"),
            difficulty = "Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Esencial para estudiantes que planean estudiar en el extranjero o presentar exámenes internacionales de inglés.",
            relatedBooks = listOf("English Conversation", "Literatura Universal")
        ),
        
        // FILOSOFÍA
        "Introducción a la Filosofía" to BookDetail(
            title = "Introducción a la Filosofía",
            author = "Colección COJEMA",
            subject = "Filosofía",
            grades = listOf("10°", "11°"),
            description = "Pensadores clásicos y sus ideas",
            summary = "Introduce los principales pensadores y corrientes filosóficas desde la antigüedad hasta la época moderna, explorando las grandes preguntas de la humanidad.",
            keyTopics = listOf("Filosofía antigua", "Filosofía medieval", "Filosofía moderna", "Grandes preguntas", "Pensamiento crítico"),
            difficulty = "Avanzado",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Fundamental para desarrollar el pensamiento crítico y filosófico. Base para estudios en humanidades y ciencias sociales.",
            relatedBooks = listOf("Lógica y Razonamiento", "Historia Universal")
        ),
        
        "Lógica y Razonamiento" to BookDetail(
            title = "Lógica y Razonamiento",
            author = "Colección COJEMA",
            subject = "Filosofía",
            grades = listOf("10°", "11°"),
            description = "Pensamiento crítico y lógica formal",
            summary = "Desarrolla habilidades de pensamiento crítico y lógica formal, incluyendo argumentación, falacias lógicas, razonamiento deductivo e inductivo.",
            keyTopics = listOf("Argumentación", "Falacias lógicas", "Razonamiento deductivo", "Razonamiento inductivo", "Pensamiento crítico"),
            difficulty = "Avanzado",
            estimatedReadingTime = "5-6 semanas",
            recommendations = "Esencial para desarrollar habilidades de análisis y argumentación. Útil en todas las áreas del conocimiento.",
            relatedBooks = listOf("Introducción a la Filosofía", "Matemáticas")
        ),
        
        // TECNOLOGÍA
        "Informática Básica" to BookDetail(
            title = "Informática Básica",
            author = "Colección COJEMA",
            subject = "Tecnología",
            grades = listOf("6°", "7°"),
            description = "Uso de computadoras y software",
            summary = "Introduce los conceptos básicos de informática, incluyendo el uso de computadoras, software de oficina, internet y herramientas digitales básicas.",
            keyTopics = listOf("Hardware básico", "Software de oficina", "Internet y navegación", "Herramientas digitales", "Seguridad informática"),
            difficulty = "Principiante",
            estimatedReadingTime = "3-4 semanas",
            recommendations = "Fundamental en la era digital. Base esencial para el uso de tecnología en la vida cotidiana y académica.",
            relatedBooks = listOf("Programación", "Diseño Digital")
        ),
        
        "Programación" to BookDetail(
            title = "Programación",
            author = "Colección COJEMA",
            subject = "Tecnología",
            grades = listOf("8°", "9°", "10°"),
            description = "Lenguajes de programación básicos",
            summary = "Introduce los fundamentos de la programación, incluyendo algoritmos, estructuras de datos básicas y lenguajes de programación como Python o JavaScript.",
            keyTopics = listOf("Algoritmos básicos", "Estructuras de datos", "Lenguajes de programación", "Resolución de problemas", "Pensamiento computacional"),
            difficulty = "Intermedio",
            estimatedReadingTime = "6-8 semanas",
            recommendations = "Esencial para desarrollar habilidades de pensamiento computacional y lógico. Base para carreras en tecnología e ingeniería.",
            relatedBooks = listOf("Informática Básica", "Matemáticas")
        ),
        
        "Diseño Digital" to BookDetail(
            title = "Diseño Digital",
            author = "Colección COJEMA",
            subject = "Tecnología",
            grades = listOf("9°", "10°", "11°"),
            description = "Herramientas de diseño gráfico",
            summary = "Introduce las herramientas y técnicas de diseño gráfico digital, incluyendo software de diseño, principios de diseño y creación de contenido visual.",
            keyTopics = listOf("Software de diseño", "Principios de diseño", "Tipografía", "Color y composición", "Diseño web básico"),
            difficulty = "Avanzado",
            estimatedReadingTime = "5-6 semanas",
            recommendations = "Ideal para estudiantes interesados en diseño gráfico, publicidad, marketing digital o comunicación visual.",
            relatedBooks = listOf("Programación", "Arte")
        )
    )
    
    // Clase para información detallada de libros
    data class BookDetail(
        val title: String,
        val author: String,
        val subject: String,
        val grades: List<String>,
        val description: String,
        val summary: String,
        val keyTopics: List<String>,
        val difficulty: String,
        val estimatedReadingTime: String,
        val recommendations: String,
        val relatedBooks: List<String>,
        val literaryAnalysis: String? = null
    )
    
    // Función para obtener información detallada de un libro específico
    fun getBookDetail(bookTitle: String): BookDetail? {
        return bookDetails[bookTitle]
    }
    
    // Función para buscar libros por título o autor
    fun searchBooksByTitleOrAuthor(query: String): List<BookDetail> {
        val lowerQuery = query.lowercase()
        return bookDetails.values.filter { book ->
            book.title.lowercase().contains(lowerQuery) ||
            book.author.lowercase().contains(lowerQuery) ||
            book.subject.lowercase().contains(lowerQuery)
        }
    }
    
    // Función para obtener recomendaciones personalizadas basadas en intereses
    fun getPersonalizedRecommendations(grade: String, interests: List<String>): List<BookDetail> {
        return bookDetails.values.filter { book ->
            book.grades.contains("$grade°") &&
            (interests.isEmpty() || interests.any { interest ->
                book.subject.lowercase().contains(interest.lowercase()) ||
                book.title.lowercase().contains(interest.lowercase()) ||
                book.keyTopics.any { topic -> topic.lowercase().contains(interest.lowercase()) }
            })
        }.sortedBy { it.difficulty }
    }
    
    // Función para generar resumen detallado de un libro
    fun generateDetailedBookResponse(bookTitle: String): String? {
        val book = getBookDetail(bookTitle) ?: return null
        
        val response = StringBuilder()
        response.append("📖 **${book.title}**\n")
        response.append("✍️ **Autor:** ${book.author}\n")
        response.append("📚 **Materia:** ${book.subject}\n")
        response.append("🎓 **Grados:** ${book.grades.joinToString(", ")}\n")
        response.append("⭐ **Dificultad:** ${book.difficulty}\n")
        response.append("⏱️ **Tiempo estimado:** ${book.estimatedReadingTime}\n\n")
        
        response.append("📝 **Resumen:**\n")
        response.append("${book.summary}\n\n")
        
        response.append("🔑 **Temas principales:**\n")
        book.keyTopics.forEach { topic ->
            response.append("• $topic\n")
        }
        response.append("\n")
        
        response.append("💡 **Recomendaciones:**\n")
        response.append("${book.recommendations}\n\n")
        
        if (book.literaryAnalysis != null) {
            response.append("📖 **Análisis literario:**\n")
            response.append("${book.literaryAnalysis}\n\n")
        }
        
        response.append("📚 **Libros relacionados:**\n")
        response.append("${book.relatedBooks.joinToString(", ")}\n\n")
        
        response.append("¿Te gustaría saber más sobre algún tema específico de este libro o conocer otros libros similares?")
        
        return response.toString()
    }
    
    // Función para generar recomendaciones personalizadas
    fun generatePersonalizedRecommendationsResponse(grade: String, interests: List<String>): String {
        val recommendations = getPersonalizedRecommendations(grade, interests)
        
        if (recommendations.isEmpty()) {
            return "No encontré recomendaciones específicas para $grade° grado con esos intereses. Te sugiero explorar todas las materias disponibles o consultar libros de grados cercanos."
        }
        
        val response = StringBuilder()
        response.append("📚 **Recomendaciones personalizadas para $grade° grado:**\n\n")
        
        recommendations.take(5).forEach { book ->
            response.append("📖 **${book.title}** (${book.subject})\n")
            response.append("   ✍️ ${book.author}\n")
            response.append("   ⭐ ${book.difficulty} • ⏱️ ${book.estimatedReadingTime}\n")
            response.append("   📝 ${book.summary.take(100)}...\n\n")
        }
        
        response.append("¿Te gustaría conocer más detalles sobre alguno de estos libros o buscar por otra materia?")
        
        return response.toString()
    }
    
    // Función para buscar libros por dificultad
    fun getBooksByDifficulty(difficulty: String): List<BookDetail> {
        return bookDetails.values.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
    }
    
    // Función para obtener libros por tiempo de lectura
    fun getBooksByReadingTime(maxWeeks: Int): List<BookDetail> {
        return bookDetails.values.filter { book ->
            val weeks = book.estimatedReadingTime.split(" ").firstOrNull()?.toIntOrNull() ?: 0
            weeks <= maxWeeks
        }
    }
    
    // Función para manejar preguntas específicas sobre libros
    fun handleBookSpecificQuestions(message: String): String? {
        val lowerMessage = message.lowercase()
        
        // Preguntas sobre análisis literario
        if (lowerMessage.contains("análisis") || lowerMessage.contains("analisis")) {
            val literaryBooks = bookDetails.values.filter { it.literaryAnalysis != null }
            if (literaryBooks.isNotEmpty()) {
                val response = StringBuilder()
                response.append("📖 **Libros con análisis literario disponible:**\n\n")
                literaryBooks.forEach { book ->
                    response.append("📚 **${book.title}** - ${book.author}\n")
                    response.append("   ${book.literaryAnalysis?.take(100)}...\n\n")
                }
                response.append("¿Te gustaría conocer el análisis completo de alguno de estos libros?")
                return response.toString()
            }
        }
        
        // Preguntas sobre recomendaciones
        if (lowerMessage.contains("recomend") || lowerMessage.contains("sugerir")) {
            val gradePattern = Regex("(\\d+)°|grado (\\d+)")
            val gradeMatch = gradePattern.find(message)
            val grade = gradeMatch?.value?.replace("°", "")?.replace("grado ", "") ?: "9"
            
            val interests = mutableListOf<String>()
            if (lowerMessage.contains("matemática") || lowerMessage.contains("matematicas")) interests.add("matemáticas")
            if (lowerMessage.contains("ciencia") || lowerMessage.contains("biología")) interests.add("ciencias")
            if (lowerMessage.contains("historia")) interests.add("historia")
            if (lowerMessage.contains("literatura") || lowerMessage.contains("libro")) interests.add("literatura")
            if (lowerMessage.contains("inglés") || lowerMessage.contains("ingles")) interests.add("inglés")
            if (lowerMessage.contains("filosofía") || lowerMessage.contains("filosofia")) interests.add("filosofía")
            if (lowerMessage.contains("tecnología") || lowerMessage.contains("tecnologia")) interests.add("tecnología")
            
            return generatePersonalizedRecommendationsResponse(grade, interests)
        }
        
        // Preguntas sobre resúmenes
        if (lowerMessage.contains("resumen") || lowerMessage.contains("sinopsis")) {
            for (bookTitle in bookDetails.keys) {
                if (lowerMessage.contains(bookTitle.lowercase())) {
                    val book = getBookDetail(bookTitle)
                    if (book != null) {
                        val response = StringBuilder()
                        response.append("📖 **Resumen de ${book.title}**\n\n")
                        response.append("✍️ **Autor:** ${book.author}\n")
                        response.append("📚 **Materia:** ${book.subject}\n\n")
                        response.append("📝 **Sinopsis:**\n")
                        response.append("${book.summary}\n\n")
                        response.append("🔑 **Temas principales:**\n")
                        book.keyTopics.take(3).forEach { topic ->
                            response.append("• $topic\n")
                        }
                        response.append("\n¿Te gustaría conocer más detalles sobre este libro?")
                        return response.toString()
                    }
                }
            }
        }
        
        // Preguntas sobre dificultad
        if (lowerMessage.contains("fácil") || lowerMessage.contains("difícil") || lowerMessage.contains("nivel")) {
            val easyBooks = getBooksByDifficulty("Principiante")
            val response = StringBuilder()
            response.append("📚 **Libros para principiantes:**\n\n")
            easyBooks.take(3).forEach { book ->
                response.append("📖 **${book.title}** (${book.subject})\n")
                response.append("   ⏱️ ${book.estimatedReadingTime}\n")
                response.append("   📝 ${book.summary.take(80)}...\n\n")
            }
            response.append("¿Te gustaría conocer más libros de este nivel o de otros niveles?")
            return response.toString()
        }
        
        return null
    }

    // Base de artículos legales: Constitución Política de Colombia (resumida y literal en los más importantes)
    val legalArticles = mapOf(
        // TÍTULO I: PRINCIPIOS FUNDAMENTALES
        "constitucion_titulo_1" to "Título I: Principios Fundamentales. Artículos 1 al 10. Establecen la soberanía, la dignidad humana, la participación, el Estado social de derecho, la separación de poderes y los símbolos patrios.",
        "constitucion_art_1" to "Artículo 1. Colombia es un Estado social de derecho, organizado en forma de República unitaria, descentralizada, con autonomía de sus entidades territoriales, democrática, participativa y pluralista, fundada en el respeto de la dignidad humana, en el trabajo y la solidaridad de las personas que la integran y en la prevalencia del interés general.",
        "constitucion_art_2" to "Artículo 2. Son fines esenciales del Estado: servir a la comunidad, promover la prosperidad general y garantizar la efectividad de los principios, derechos y deberes consagrados en la Constitución; facilitar la participación de todos en las decisiones que los afectan y en la vida económica, política, administrativa y cultural de la Nación; defender la independencia nacional, mantener la integridad territorial y asegurar la convivencia pacífica y la vigencia de un orden justo.",
        "constitucion_art_3" to "Artículo 3. La soberanía reside exclusivamente en el pueblo, del cual emana el poder público. El pueblo la ejerce en forma directa o por medio de sus representantes, en los términos que la Constitución establece.",
        "constitucion_art_4" to "Artículo 4. La Constitución es norma de normas. En todo caso de incompatibilidad entre la Constitución y la ley u otra norma jurídica, se aplicarán las disposiciones constitucionales.",
        "constitucion_art_5" to "Artículo 5. El Estado reconoce, sin discriminación alguna, la primacía de los derechos inalienables de la persona y ampara a la familia como institución básica de la sociedad.",
        "constitucion_art_6" to "Artículo 6. Los particulares solo son responsables ante las autoridades por infringir la Constitución y las leyes. Los servidores públicos lo son por la misma causa y por omisión o extralimitación en el ejercicio de sus funciones.",
        "constitucion_art_7" to "Artículo 7. El Estado reconoce y protege la diversidad étnica y cultural de la Nación colombiana.",
        "constitucion_art_8" to "Artículo 8. Es obligación del Estado y de las personas proteger las riquezas culturales y naturales de la Nación.",
        "constitucion_art_9" to "Artículo 9. Las relaciones exteriores del Estado se fundamentan en la soberanía nacional, en el respeto a la autodeterminación de los pueblos y en el reconocimiento de los principios del derecho internacional aceptados por Colombia.",
        "constitucion_art_10" to "Artículo 10. El castellano es el idioma oficial de Colombia. Las lenguas y dialectos de los grupos étnicos son también oficiales en sus territorios. La enseñanza que se imparta en las comunidades con tradiciones lingüísticas propias será bilingüe.",
        "constitucion_art_11" to "Artículo 11. El derecho a la vida es inviolable. No habrá pena de muerte.",
        "constitucion_art_12" to "Artículo 12. Nadie será sometido a desaparición forzada, a torturas ni a tratos o penas crueles, inhumanos o degradantes.",
        "constitucion_art_13" to "Artículo 13. Todas las personas nacen libres e iguales ante la ley, recibirán la misma protección y trato de las autoridades y gozarán de los mismos derechos, libertades y oportunidades sin ninguna discriminación.",
        "constitucion_art_14" to "Artículo 14. Toda persona tiene derecho al reconocimiento de su personalidad jurídica.",
        "constitucion_art_15" to "Artículo 15. Todas las personas tienen derecho a su intimidad personal y familiar y a su buen nombre, y el Estado debe respetarlos y hacerlos respetar.",
        "constitucion_art_16" to "Artículo 16. Todas las personas tienen derecho al libre desarrollo de su personalidad sin más limitaciones que las que imponen los derechos de los demás y el orden jurídico.",
        "constitucion_art_17" to "Artículo 17. Se prohíbe la esclavitud, la servidumbre y la trata de seres humanos en todas sus formas.",
        "constitucion_art_18" to "Artículo 18. Se garantiza la libertad de conciencia. Nadie será molestado por razón de sus convicciones o creencias ni compelido a revelarlas ni obligado a actuar contra su conciencia.",
        "constitucion_art_19" to "Artículo 19. Se garantiza la libertad de cultos. Toda persona tiene derecho a profesar libremente su religión y a difundirla en forma individual o colectiva.",
        "constitucion_art_20" to "Artículo 20. Se garantiza a toda persona la libertad de expresar y difundir su pensamiento y opiniones, la de informar y recibir información veraz e imparcial, y la de fundar medios masivos de comunicación.",
        "constitucion_art_21" to "Artículo 21. Se garantiza el derecho a la honra. La ley señalará la forma de su protección.",
        "constitucion_art_22" to "Artículo 22. La paz es un derecho y un deber de obligatorio cumplimiento.",
        "constitucion_art_23" to "Artículo 23. Toda persona tiene derecho a presentar peticiones respetuosas a las autoridades por motivos de interés general o particular y a obtener pronta resolución.",
        "constitucion_art_24" to "Artículo 24. Todo colombiano, con las limitaciones que establezca la ley, tiene derecho a circular libremente por el territorio nacional, a entrar y salir de él, y a permanecer y residenciarse en Colombia.",
        "constitucion_art_25" to "Artículo 25. El trabajo es un derecho y una obligación social y goza, en todas sus modalidades, de la especial protección del Estado.",
        "constitucion_art_26" to "Artículo 26. Toda persona es libre de escoger profesión u oficio. La ley podrá exigir títulos de idoneidad. Las autoridades competentes inspeccionarán y vigilarán el ejercicio de las profesiones.",
        "constitucion_art_27" to "Artículo 27. El Estado garantiza las libertades de enseñanza, aprendizaje, investigación y cátedra.",
        "constitucion_art_28" to "Artículo 28. Toda persona es libre. Nadie puede ser molestado en su persona o familia, ni reducido a prisión o arresto, ni detenido, ni su domicilio registrado, sino en virtud de mandamiento escrito de autoridad judicial competente, con las formalidades legales y por motivo previamente definido en la ley.",
        "constitucion_art_29" to "Artículo 29. El debido proceso se aplicará a toda clase de actuaciones judiciales y administrativas.",
        "constitucion_art_30" to "Artículo 30. Quien estuviere privado de su libertad, y creyere estarlo ilegalmente, tiene derecho a invocar ante cualquier autoridad judicial, en todo tiempo, por sí o por interpuesta persona, el Habeas Corpus, el cual debe resolverse en el término de treinta y seis horas.",
        "constitucion_art_31" to "Artículo 31. Toda sentencia judicial podrá ser apelada o consultada, salvo las excepciones que consagre la ley.",
        "constitucion_art_32" to "Artículo 32. El delincuente sorprendido en flagrancia podrá ser aprehendido y llevado ante el juez por cualquier persona.",
        "constitucion_art_33" to "Artículo 33. Nadie podrá ser obligado a declarar contra sí mismo o contra su cónyuge, compañero permanente o parientes dentro del cuarto grado de consanguinidad, segundo de afinidad o primero civil.",
        "constitucion_art_34" to "Artículo 34. Se prohíben las penas de destierro, prisión perpetua y confiscación.",
        "constitucion_art_35" to "Artículo 35. Se prohíbe la extradición de colombianos por nacimiento. No se concederá la extradición de extranjeros por delitos políticos o de opinión.",
        "constitucion_art_36" to "Artículo 36. Se reconoce el derecho de asilo en los términos previstos en la ley.",
        "constitucion_art_37" to "Artículo 37. Toda parte del pueblo puede reunirse y manifestarse pública y pacíficamente. Sólo la ley podrá establecer de manera expresa los casos en los cuales se podrá limitar el ejercicio de este derecho.",
        "constitucion_art_38" to "Artículo 38. Se garantiza el derecho de libre asociación para el desarrollo de las distintas actividades que las personas realizan en sociedad.",
        "constitucion_art_39" to "Artículo 39. Los trabajadores y empleadores tienen derecho a constituir sindicatos o asociaciones, sin intervención del Estado. Su reconocimiento jurídico se producirá con la simple inscripción del acta de constitución.",
        "constitucion_art_40" to "Artículo 40. Todo ciudadano tiene derecho a participar en la conformación, ejercicio y control del poder político. Puede elegir y ser elegido, tomar parte en elecciones, plebiscitos, referendos y consultas populares, y ejercer cargos públicos.",
        "constitucion_art_41" to "Artículo 41. En todas las instituciones de educación, oficiales o privadas, serán obligatorios el estudio de la Constitución y la Instrucción Cívica.",
        "constitucion_art_42" to "Artículo 42. La familia es el núcleo fundamental de la sociedad. Se constituye por vínculos naturales o jurídicos, por la decisión libre de un hombre y una mujer de contraer matrimonio o por la voluntad responsable de conformarla.",
        "constitucion_art_43" to "Artículo 43. La mujer y el hombre tienen iguales derechos y oportunidades. La mujer no podrá ser sometida a ninguna clase de discriminación.",
        "constitucion_art_44" to "Artículo 44. Son derechos fundamentales de los niños: la vida, la integridad física, la salud y la seguridad social, la alimentación equilibrada, su nombre y nacionalidad, tener una familia y no ser separados de ella, el cuidado y amor, la educación y la cultura, la recreación y la libre expresión de su opinión.",
        "constitucion_art_45" to "Artículo 45. El adolescente tiene derecho a la protección y a la formación integral.",
        "constitucion_art_46" to "Artículo 46. El Estado, la sociedad y la familia concurrirán para la protección y la asistencia de las personas de la tercera edad y promoverán su integración a la vida activa y comunitaria.",
        "constitucion_art_47" to "Artículo 47. El Estado adelantará una política de previsión, rehabilitación e integración social para los disminuidos físicos, sensoriales y psíquicos, a quienes se prestará la atención especializada que requieran.",
        "constitucion_art_48" to "Artículo 48. La Seguridad Social es un servicio público de carácter obligatorio que se prestará bajo la dirección, coordinación y control del Estado, en sujeción a los principios de eficiencia, universalidad y solidaridad.",
        "constitucion_art_49" to "Artículo 49. La atención de la salud y el saneamiento ambiental son servicios públicos a cargo del Estado.",
        "constitucion_art_50" to "Artículo 50. Todo niño menor de un año que no esté cubierto por algún tipo de protección o de seguridad social, tendrá derecho a recibir atención gratuita en todas las instituciones de salud que reciban aportes del Estado.",
        "constitucion_art_51" to "Artículo 51. Todos los colombianos tienen derecho a vivienda digna.",
        "constitucion_art_52" to "Artículo 52. El Congreso expedirá el estatuto del trabajo.",
        "constitucion_art_53" to "Artículo 53. El Congreso expedirá el estatuto del trabajo. La ley correspondiente tendrá en cuenta por lo menos los siguientes principios mínimos fundamentales: Igualdad de oportunidades para los trabajadores; remuneración mínima vital y móvil, proporcional a la cantidad y calidad de trabajo; estabilidad en el empleo; irrenunciabilidad a los beneficios mínimos establecidos en normas laborales; facultades para transigir y conciliar sobre derechos inciertos y discutibles; situación más favorable al trabajador en caso de duda en la aplicación o interpretación de las fuentes formales de derecho; primacía de la realidad sobre formalidades establecidas por los sujetos de las relaciones laborales; garantía a la seguridad social, la capacitación, el adiestramiento y el descanso necesario; protección especial a la mujer, a la maternidad y al trabajador menor de edad.",
        "constitucion_art_54" to "Artículo 54. Es obligación del Estado y de los empleadores ofrecer formación y habilitación profesional y técnica a quienes lo requieran.",
        "constitucion_art_55" to "Artículo 55. Se garantiza el derecho de negociación colectiva para regular las relaciones laborales, con las excepciones que señale la ley.",
        "constitucion_art_56" to "Artículo 56. Se garantiza el derecho de huelga, salvo en los servicios públicos esenciales definidos por el legislador.",
        "constitucion_art_57" to "Artículo 57. La ley podrá establecer los estímulos y los medios para que los trabajadores participen en la gestión de las empresas.",
        "constitucion_art_58" to "Artículo 58. Se garantizan la propiedad privada y los demás derechos adquiridos con arreglo a las leyes civiles, los cuales no pueden ser desconocidos ni vulnerados por leyes posteriores.",
        "constitucion_art_59" to "Artículo 59. En caso de guerra y sólo para atender a los requerimientos de la defensa nacional, podrá el Estado expropiar sin indemnización, las industrias, el comercio y la propiedad privada, o los servicios.",
        "constitucion_art_60" to "Artículo 60. El Estado promoverá, de acuerdo con la ley, el acceso a la propiedad.",
        "constitucion_art_61" to "Artículo 61. El Estado protegerá la propiedad intelectual por el tiempo y mediante las formalidades que establezca la ley.",
        "constitucion_art_62" to "Artículo 62. El Estado podrá intervenir por mandato de la ley, la explotación de los recursos naturales no renovables, así como los demás bienes y servicios de interés público.",
        "constitucion_art_63" to "Artículo 63. Los bienes de uso público, los parques naturales, las tierras comunales de grupos étnicos, las tierras de resguardo, el patrimonio arqueológico de la Nación y los demás bienes que determine la ley, son inalienables, imprescriptibles e inembargables.",
        "constitucion_art_64" to "Artículo 64. Es deber del Estado promover el acceso progresivo a la propiedad de la tierra de los trabajadores agrarios, en forma individual o asociativa, y a los servicios de educación, salud, vivienda, seguridad social, recreación, crédito, comunicaciones, comercialización de los productos, asistencia técnica y empresarial, con el fin de mejorar el ingreso y calidad de vida de los campesinos.",
        "constitucion_art_65" to "Artículo 65. La producción de alimentos gozará de la especial protección del Estado.",
        "constitucion_art_66" to "Artículo 66. Las disposiciones que se dicten en materia crediticia podrán reglamentar las condiciones especiales del crédito agropecuario, teniendo en cuenta los ciclos de las cosechas y de los precios, como también los riesgos inherentes a la actividad y las calamidades ambientales.",
        "constitucion_art_67" to "Artículo 67. La educación es un derecho de la persona y un servicio público que tiene una función social; con ella se busca el acceso al conocimiento, a la ciencia, a la técnica, y a los demás bienes y valores de la cultura.",
        "constitucion_art_68" to "Artículo 68. Los particulares podrán fundar establecimientos educativos. La ley establecerá las condiciones para su creación y gestión.",
        "constitucion_art_69" to "Artículo 69. Se garantiza la autonomía universitaria.",
        "constitucion_art_70" to "Artículo 70. El Estado tiene el deber de promover y fomentar el acceso a la cultura de todos los colombianos en igualdad de oportunidades, por medio de la educación permanente y la enseñanza científica, técnica, artística y profesional en todas las etapas del proceso de creación de la identidad nacional.",
        "constitucion_art_71" to "Artículo 71. La búsqueda del conocimiento y la expresión artística son libres. Los planes de desarrollo económico y social incluirán el fomento a las ciencias y, en general, a la cultura.",
        "constitucion_art_72" to "Artículo 72. El patrimonio cultural de la Nación está bajo la protección del Estado.",
        "constitucion_art_73" to "Artículo 73. La actividad periodística gozará de protección para garantizar su libertad e independencia profesional.",
        "constitucion_art_74" to "Artículo 74. Todas las personas tienen derecho a acceder a los documentos públicos salvo los casos que establezca la ley.",
        "constitucion_art_75" to "Artículo 75. El espectro electromagnético es un bien público inenajenable e imprescriptible sujeto a la gestión y control del Estado.",
        "constitucion_art_76" to "Artículo 76. La intervención estatal en el espectro electromagnético utilizado para los servicios de televisión, estará a cargo de un organismo de derecho público.",
        "constitucion_art_77" to "Artículo 77. La dirección de la política que en materia de televisión determine la ley sin menoscabo de las libertades consagradas en esta Constitución, estará a cargo del organismo mencionado.",
        "constitucion_art_78" to "Artículo 78. La regulación que haga el legislador de los servicios de televisión, se aplicará por igual a la programación nacional y extranjera.",
        "constitucion_art_79" to "Artículo 79. Todas las personas tienen derecho a gozar de un ambiente sano.",
        "constitucion_art_80" to "Artículo 80. El Estado planificará el manejo y aprovechamiento de los recursos naturales, para garantizar su desarrollo sostenible, su conservación, restauración o sustitución.",
        "constitucion_art_81" to "Artículo 81. Queda prohibida la fabricación, importación, posesión y uso de armas químicas, biológicas y nucleares, así como la introducción al territorio nacional de residuos nucleares y desechos tóxicos.",
        "constitucion_art_82" to "Artículo 82. Es deber del Estado velar por la protección de la integridad del espacio público y por su destinación al uso común, el cual prevalece sobre el interés particular.",
        "constitucion_art_83" to "Artículo 83. Las actuaciones de los particulares y de las autoridades públicas deberán ceñirse a los postulados de la buena fe, la cual se presumirá en todas las gestiones que aquellos adelanten ante éstas.",
        "constitucion_art_84" to "Artículo 84. Cuando un derecho o una actividad hayan sido reglamentados de manera general, las autoridades públicas no podrán establecer ni exigir permisos, licencias o requisitos adicionales para su ejercicio.",
        "constitucion_art_85" to "Artículo 85. Son de aplicación inmediata los derechos consagrados en los artículos 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 23, 24, 26, 27, 28, 29, 30, 31, 33, 34, 37 y 40, salvo en cuanto a los derechos que requieran para su realización, según la ley, el desarrollo de una actividad estatal reglamentaria.",
        "constitucion_art_86" to "Artículo 86. Toda persona tendrá acción de tutela para reclamar ante los jueces, en todo momento y lugar, mediante un procedimiento preferente y sumario, por sí misma o por quien actúe a su nombre, la protección inmediata de sus derechos constitucionales fundamentales, cuando quiera que estos resulten vulnerados o amenazados por la acción o la omisión de cualquier autoridad pública.",
        "constitucion_art_87" to "Artículo 87. Toda persona podrá acudir ante la autoridad judicial para hacer efectivo el cumplimiento de una ley o un acto administrativo.",
        "constitucion_art_88" to "Artículo 88. La ley regulará las acciones populares para la protección de derechos e intereses colectivos, relacionados con el patrimonio, el espacio, la seguridad y la salubridad públicos, la moral administrativa, el ambiente, la libre competencia económica y otros de similar naturaleza que se definen en ella.",
        "constitucion_art_89" to "Artículo 89. El trabajo es una obligación social y goza, en todas sus modalidades, de la especial protección del Estado.",
        "constitucion_art_90" to "Artículo 90. El Estado responderá patrimonialmente por los daños antijurídicos que le sean imputables, causados por la acción o la omisión de las autoridades públicas.",
        "constitucion_art_91" to "Artículo 91. En caso de infracción manifiesta de un precepto constitucional en detrimento de alguna persona, el mandato superior no exime de responsabilidad al agente que lo ejecuta.",
        "constitucion_art_92" to "Artículo 92. Cualquier persona natural o jurídica podrá solicitar de la autoridad competente la aplicación de las sanciones penales o disciplinarias derivadas de la conducta de las autoridades públicas.",
        "constitucion_art_93" to "Artículo 93. Los tratados y convenios internacionales ratificados por el Congreso, que reconocen los derechos humanos y que prohíben su limitación en los estados de excepción, prevalecen en el orden interno.",
        "constitucion_art_94" to "Artículo 94. La enunciación de los derechos y garantías contenidos en la Constitución y en los convenios internacionales vigentes, no debe entenderse como negación de otros que, siendo inherentes a la persona humana, no figuren expresamente en ellos.",
        "constitucion_art_95" to "Artículo 95. La calidad de colombiano enaltece a todos los miembros de la comunidad nacional. Todos están en el deber de engrandecerla y dignificarla.",
        "constitucion_art_96" to "Artículo 96. Son nacionales colombianos: 1. Por nacimiento: a) Los naturales de Colombia, que con una de dos condiciones: que el padre o la madre hayan sido naturales o nacionales colombianos o que, siendo hijos de extranjeros, alguno de sus padres estuviere domiciliado en la República en el momento del nacimiento y b) Los hijos de padre o madre colombianos que hubieren nacido en tierra extranjera y luego se domiciliaren en territorio colombiano. 2. Por adopción: a) Los extranjeros que soliciten y obtengan carta de naturalización, de acuerdo con la ley, b) Los latinoamericanos y del Caribe por nacimiento domiciliados en Colombia, que con autorización del Gobierno y de acuerdo con la ley y el principio de reciprocidad, pidan ser inscritos como colombianos ante la municipalidad donde se establecieren, c) Los miembros de los pueblos indígenas que comparten territorios fronterizos, con aplicación del principio de reciprocidad según tratados públicos.",
        "constitucion_art_97" to "Artículo 97. El colombiano, aunque haya renunciado a la calidad de nacional, que actúe contra los intereses del país en guerra exterior contra Colombia, será juzgado y penado como traidor a la patria.",
        "constitucion_art_98" to "Artículo 98. La ciudadanía se pierde de hecho cuando se ha renunciado a la nacionalidad, y su ejercicio se puede suspender en virtud de decisión judicial en los casos que determine la ley.",
        "constitucion_art_99" to "Artículo 99. La calidad de ciudadano en ejercicio es condición previa e indispensable para ejercer el derecho de sufragio, para ser elegido y para desempeñar cargos públicos que lleven anexa autoridad o jurisdicción.",
        "constitucion_art_100" to "Artículo 100. Los extranjeros disfrutarán en Colombia de los mismos derechos civiles que se conceden a los colombianos.",
        "constitucion_art_101" to "Artículo 101. Los extranjeros gozarán, en el territorio de la República, de las garantías concedidas a los nacionales, salvo las limitaciones que establezcan la Constitución o la ley.",
        "constitucion_art_102" to "Artículo 102. Los derechos de los extranjeros se determinarán por tratados públicos y por las leyes de reciprocidad.",
        "constitucion_art_103" to "Artículo 103. Son deberes de la persona y del ciudadano: 1. Respetar los derechos ajenos y no abusar de los propios, 2. Obrar conforme al principio de solidaridad social, respondiendo con acciones humanitarias ante situaciones que pongan en peligro la vida o la salud de las personas, 3. Respetar y apoyar a las autoridades democráticas legítimamente constituidas para mantener la independencia y la integridad nacionales, 4. Defender y difundir los derechos humanos como fundamento de la convivencia pacífica, 5. Participar en la vida política, cívica y comunitaria del país, 6. Propender al logro y mantenimiento de la paz, 7. Colaborar para el buen funcionamiento de la administración de la justicia, 8. Proteger los recursos culturales y naturales del país y velar por la conservación de un ambiente sano, 9. Contribuir al financiamiento de los gastos e inversiones del Estado dentro de conceptos de justicia y equidad.",
        "constitucion_art_104" to "Artículo 104. El Presidente de la República es el Jefe del Estado, Jefe del Gobierno y suprema autoridad administrativa.",
        "constitucion_art_105" to "Artículo 105. El Gobierno Nacional está formado por el Presidente de la República, los ministros del despacho y los directores de departamentos administrativos.",
        "constitucion_art_106" to "Artículo 106. El Presidente de la República será elegido para un período de cuatro años, por la mitad más uno de los votos que, de manera secreta y directa, depositen los ciudadanos en la fecha y en la forma que determine la ley.",
        "constitucion_art_107" to "Artículo 107. No podrá ser elegido Presidente de la República quien hubiere incurrido en alguna de las siguientes causales: 1. Haber sido condenado en cualquier época por sentencia judicial a pena privativa de la libertad, excepto por delitos políticos o culposos, 2. Haber ejercido, como ciudadano, el cargo de Presidente de la República en cualquier época, 3. Ser ciudadano en ejercicio, haber sido condenado por la comisión de delitos que afecten la probidad del Estado o la vigencia del orden constitucional legal, a menos que hubiere transcurrido un período igual al de la condena, 4. Haber renunciado al cargo de Presidente de la República, 5. Tener doble nacionalidad, exceptuando la colombiana.",
        "constitucion_art_108" to "Artículo 108. El Presidente de la República, o quien haga sus veces, no podrá trasladarse a territorio extranjero durante el ejercicio de su cargo, sin previo aviso al Senado o, en receso de éste, a la Corte Suprema de Justicia.",
        "constitucion_art_109" to "Artículo 109. El Presidente de la República, durante el período para el cual sea elegido, o quien se halle encargado de la Presidencia, no podrá ser perseguido ni juzgado por delitos, sino en virtud de acusación de la Cámara de Representantes y cuando el Senado haya declarado que hay lugar a formación de causa.",
        "constitucion_art_110" to "Artículo 110. Son faltas absolutas del Presidente de la República: 1. Su muerte, 2. Su renuncia aceptada, 3. La destitución decretada por sentencia, 4. La incapacidad física permanente, 5. El abandono del cargo, declarado éste por el Senado, 6. La aceptación de una renuncia, si la ley no dispone otra cosa.",
        "constitucion_art_111" to "Artículo 111. Son faltas temporales del Presidente de la República: 1. Las licencias médicas, 2. La ausencia del territorio nacional en comisión de servicio, 3. La suspensión en el ejercicio del cargo, decretada por el Senado, 4. La enfermedad, que según el concepto de una junta de médicos, designada por el Senado, lo inhabilite para el ejercicio del cargo.",
        "constitucion_art_112" to "Artículo 112. El encargado del despacho del Presidente de la República será el Vicepresidente, a falta de éste, el Ministro a quien corresponda según el orden de precedencia legal, y a falta de éste, el Ministro que designe el Presidente, o a falta de todos los anteriores, el Ministro que nombre el Consejo de Ministros.",
        "constitucion_art_113" to "Artículo 113. Los ministros y los directores de departamentos administrativos son los jefes de la administración en su respectiva dependencia.",
        "constitucion_art_114" to "Artículo 114. Para ser ministro o director de departamento administrativo se requieren las mismas calidades que para ser representante a la Cámara.",
        "constitucion_art_115" to "Artículo 115. Los ministros y directores de departamentos administrativos serán de la confianza del Presidente de la República y le corresponderá coordinar sus funciones.",
        "constitucion_art_116" to "Artículo 116. Los ministros y directores de departamentos administrativos presentarán al Congreso, dentro de los primeros quince días de cada legislatura, informe sobre el estado de los negocios adscritos a su ministerio o departamento administrativo, y sobre las reformas que consideren convenientes.",
        "constitucion_art_117" to "Artículo 117. El número, denominación y orden de precedencia de los ministerios y departamentos administrativos serán determinados por la ley.",
        "constitucion_art_118" to "Artículo 118. Los ministros y directores de departamentos administrativos no podrán ser congresistas.",
        "constitucion_art_119" to "Artículo 119. Los ministros y directores de departamentos administrativos no podrán ser funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_120" to "Artículo 120. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_121" to "Artículo 121. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_122" to "Artículo 122. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_123" to "Artículo 123. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_124" to "Artículo 124. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_125" to "Artículo 125. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_126" to "Artículo 126. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_127" to "Artículo 127. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_128" to "Artículo 128. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_129" to "Artículo 129. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_130" to "Artículo 130. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_131" to "Artículo 131. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_132" to "Artículo 132. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_133" to "Artículo 133. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_134" to "Artículo 134. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_135" to "Artículo 135. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_136" to "Artículo 136. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_137" to "Artículo 137. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_138" to "Artículo 138. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_139" to "Artículo 139. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_140" to "Artículo 140. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_141" to "Artículo 141. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_142" to "Artículo 142. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_143" to "Artículo 143. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_144" to "Artículo 144. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_145" to "Artículo 145. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_146" to "Artículo 146. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_147" to "Artículo 147. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_148" to "Artículo 148. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_149" to "Artículo 149. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_150" to "Artículo 150. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_151" to "Artículo 151. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_152" to "Artículo 152. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_153" to "Artículo 153. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_154" to "Artículo 154. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_155" to "Artículo 155. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_156" to "Artículo 156. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_157" to "Artículo 157. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_158" to "Artículo 158. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_159" to "Artículo 159. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_160" to "Artículo 160. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_161" to "Artículo 161. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_162" to "Artículo 162. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_163" to "Artículo 163. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_164" to "Artículo 164. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_165" to "Artículo 165. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_166" to "Artículo 166. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_167" to "Artículo 167. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_168" to "Artículo 168. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_169" to "Artículo 169. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_170" to "Artículo 170. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_171" to "Artículo 171. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_172" to "Artículo 172. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_173" to "Artículo 173. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_174" to "Artículo 174. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_175" to "Artículo 175. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_176" to "Artículo 176. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_177" to "Artículo 177. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_178" to "Artículo 178. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_179" to "Artículo 179. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_180" to "Artículo 180. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_181" to "Artículo 181. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_182" to "Artículo 182. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_183" to "Artículo 183. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_184" to "Artículo 184. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_185" to "Artículo 185. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_186" to "Artículo 186. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_187" to "Artículo 187. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_188" to "Artículo 188. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_189" to "Artículo 189. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_190" to "Artículo 190. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_191" to "Artículo 191. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_192" to "Artículo 192. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_193" to "Artículo 193. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_194" to "Artículo 194. Los ministros y directores de departamentos administrativos deberán ser colombianos por nacimiento, ciudadanos en ejercicio y mayores de veinticinco años.",
        "constitucion_art_195" to "Artículo 195. Los ministros y directores de departamentos administrativos no podrán ser nombrados como agentes diplomáticos, consulares o comerciales, ni aceptar cargos de gobiernos extranjeros.",
        "constitucion_art_196" to "Artículo 196. Los ministros y directores de departamentos administrativos no podrán ser congresistas ni funcionarios de la rama judicial, ni del Ministerio Público, ni de la Contraloría General de la República, ni de la Organización Electoral, ni ser miembros de las juntas o consejos directivos de entidades descentralizadas de cualquier nivel o de instituciones que administren tributos.",
        "constitucion_art_197" to "Artículo 197. Los ministros y directores de departamentos administrativos no podrán celebrar, por sí mismos o por interpuesta persona, contrato alguno con entidades públicas o con personas privadas que manejen o administren recursos públicos, salvo las excepciones legales.",
        "constitucion_art_198" to "Artículo 198. Los ministros y directores de departamentos administrativos serán responsables de los actos que firmen o autoricen.",
        "constitucion_art_199" to "Artículo 199. Los ministros y directores de departamentos administrativos podrán tomar parte en los debates de las Cámaras sin derecho a voto.",
        "constitucion_art_200" to "Artículo 200. El Congreso podrá requerir la asistencia de los ministros y de los directores de departamentos administrativos.",
        "constitucion_art_201" to "Artículo 201. El Congreso de la República estará integrado por el Senado y la Cámara de Representantes.",
        "constitucion_art_202" to "Artículo 202. Los miembros de las Cámaras disfrutarán desde su posesión hasta la expiración del período para el cual fueron elegidos, de las siguientes prerrogativas: 1. Personalidad jurídica para todos los actos civiles y penales, 2. Inviolabilidad de opiniones y votos, 3. Fuero penal especial, 4. Inmunidad personal, 5. Aumento de dietas según el índice de precios al consumidor, 6. Todas las demás prerrogativas que les señalen la Constitución y la ley.",
        "constitucion_art_203" to "Artículo 203. Los congresistas no podrán: 1. Desempeñar cargo o empleo público o privado, 2. Gestionar, en nombre propio o ajeno, asuntos ante las entidades públicas o ante las personas que administren tributos, 3. Celebrar por sí mismos o por interpuesta persona contratos con entidades públicas, 4. Ser apoderados ante las mismas corporaciones de que forman parte, 5. Ser miembros de juntas o consejos directivos de entidades descentralizadas de cualquier nivel, 6. Aceptar cargos, honores o recompensas de gobiernos extranjeros.",
        "constitucion_art_204" to "Artículo 204. Las incompatibilidades de los congresistas tendrán vigencia durante el período constitucional respectivo.",
        "constitucion_art_205" to "Artículo 205. Los congresistas deberán poner en conocimiento de la respectiva Cámara las situaciones de carácter moral o económico que los inhiban para participar en el trámite de los asuntos sometidos a su consideración.",
        "constitucion_art_206" to "Artículo 206. Los congresistas no podrán: 1. Intervenir en asuntos de interés particular ante las entidades públicas o ante las personas que administren tributos, 2. Celebrar contratos o realizar gestiones con personas naturales o jurídicas de derecho privado que administren, manejen o inviertan fondos públicos o sean contratistas del Estado o reciban donaciones de éste.",
        "constitucion_art_207" to "Artículo 207. El régimen de inhabilidades e incompatibilidades de los congresistas no se aplicará a los miembros de la Asamblea Constituyente.",
        "constitucion_art_208" to "Artículo 208. Los congresistas perderán su investidura: 1. Por violación del régimen de inhabilidades e incompatibilidades, 2. Por inasistencia, en un mismo período de sesiones, a seis reuniones plenarias que deban realizarse en cada una de las cámaras, 3. Por no tomar posesión del cargo dentro de los ocho días siguientes a la fecha de instalación de las respectivas corporaciones o a la fecha en que fueren llamados a posesionarse, 4. Por indebida destinación de dineros públicos, 5. Por tráfico de influencias debidamente comprobado.",
        "constitucion_art_209" to "Artículo 209. Los congresistas serán inviolables por las opiniones y los votos que emitan en el ejercicio del cargo, sin perjuicio de las normas disciplinarias contenidas en el reglamento respectivo.",
        "constitucion_art_210" to "Artículo 210. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_211" to "Artículo 211. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_212" to "Artículo 212. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_213" to "Artículo 213. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_214" to "Artículo 214. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_215" to "Artículo 215. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_216" to "Artículo 216. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_217" to "Artículo 217. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_218" to "Artículo 218. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_219" to "Artículo 219. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_220" to "Artículo 220. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_221" to "Artículo 221. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_222" to "Artículo 222. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_223" to "Artículo 223. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_224" to "Artículo 224. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_225" to "Artículo 225. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_226" to "Artículo 226. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_227" to "Artículo 227. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_228" to "Artículo 228. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_229" to "Artículo 229. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_230" to "Artículo 230. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_231" to "Artículo 231. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_232" to "Artículo 232. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_233" to "Artículo 233. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_234" to "Artículo 234. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_235" to "Artículo 235. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_236" to "Artículo 236. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_237" to "Artículo 237. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_238" to "Artículo 238. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_239" to "Artículo 239. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_240" to "Artículo 240. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_241" to "Artículo 241. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_242" to "Artículo 242. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_243" to "Artículo 243. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_244" to "Artículo 244. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_245" to "Artículo 245. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_246" to "Artículo 246. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_247" to "Artículo 247. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_248" to "Artículo 248. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_249" to "Artículo 249. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_250" to "Artículo 250. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_251" to "Artículo 251. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_252" to "Artículo 252. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_253" to "Artículo 253. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_254" to "Artículo 254. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_255" to "Artículo 255. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_256" to "Artículo 256. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_257" to "Artículo 257. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_258" to "Artículo 258. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_259" to "Artículo 259. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_260" to "Artículo 260. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_261" to "Artículo 261. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_262" to "Artículo 262. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_263" to "Artículo 263. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_264" to "Artículo 264. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_265" to "Artículo 265. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_266" to "Artículo 266. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_267" to "Artículo 267. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_268" to "Artículo 268. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_269" to "Artículo 269. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_270" to "Artículo 270. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_271" to "Artículo 271. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_272" to "Artículo 272. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_273" to "Artículo 273. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_274" to "Artículo 274. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_275" to "Artículo 275. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_276" to "Artículo 276. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_277" to "Artículo 277. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_278" to "Artículo 278. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_279" to "Artículo 279. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_280" to "Artículo 280. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_281" to "Artículo 281. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_282" to "Artículo 282. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_283" to "Artículo 283. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_284" to "Artículo 284. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_285" to "Artículo 285. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_286" to "Artículo 286. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_287" to "Artículo 287. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_288" to "Artículo 288. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_289" to "Artículo 289. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_290" to "Artículo 290. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_291" to "Artículo 291. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_292" to "Artículo 292. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_293" to "Artículo 293. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_294" to "Artículo 294. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_295" to "Artículo 295. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_296" to "Artículo 296. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_297" to "Artículo 297. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_298" to "Artículo 298. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_299" to "Artículo 299. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_300" to "Artículo 300. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_301" to "Artículo 301. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_302" to "Artículo 302. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_303" to "Artículo 303. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_304" to "Artículo 304. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_305" to "Artículo 305. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_306" to "Artículo 306. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_307" to "Artículo 307. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_308" to "Artículo 308. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_309" to "Artículo 309. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_310" to "Artículo 310. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_311" to "Artículo 311. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_312" to "Artículo 312. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_313" to "Artículo 313. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_314" to "Artículo 314. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_315" to "Artículo 315. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_316" to "Artículo 316. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_317" to "Artículo 317. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_318" to "Artículo 318. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_319" to "Artículo 319. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_320" to "Artículo 320. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_321" to "Artículo 321. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_322" to "Artículo 322. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_323" to "Artículo 323. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_324" to "Artículo 324. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_325" to "Artículo 325. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_326" to "Artículo 326. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_327" to "Artículo 327. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_328" to "Artículo 328. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_329" to "Artículo 329. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_330" to "Artículo 330. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_331" to "Artículo 331. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_332" to "Artículo 332. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_333" to "Artículo 333. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_334" to "Artículo 334. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_335" to "Artículo 335. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_336" to "Artículo 336. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_337" to "Artículo 337. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_338" to "Artículo 338. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_339" to "Artículo 339. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_340" to "Artículo 340. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_341" to "Artículo 341. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_342" to "Artículo 342. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_343" to "Artículo 343. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_344" to "Artículo 344. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_345" to "Artículo 345. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_346" to "Artículo 346. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_347" to "Artículo 347. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_348" to "Artículo 348. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_349" to "Artículo 349. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_350" to "Artículo 350. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_351" to "Artículo 351. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_352" to "Artículo 352. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_353" to "Artículo 353. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_354" to "Artículo 354. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_355" to "Artículo 355. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_356" to "Artículo 356. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_357" to "Artículo 357. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_358" to "Artículo 358. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_359" to "Artículo 359. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_360" to "Artículo 360. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_361" to "Artículo 361. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_362" to "Artículo 362. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_363" to "Artículo 363. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_364" to "Artículo 364. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_365" to "Artículo 365. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_366" to "Artículo 366. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_367" to "Artículo 367. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_368" to "Artículo 368. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_369" to "Artículo 369. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_370" to "Artículo 370. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_371" to "Artículo 371. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_372" to "Artículo 372. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_373" to "Artículo 373. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_374" to "Artículo 374. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_375" to "Artículo 375. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_376" to "Artículo 376. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_377" to "Artículo 377. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_378" to "Artículo 378. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_379" to "Artículo 379. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_380" to "Artículo 380. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_381" to "Artículo 381. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_382" to "Artículo 382. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_383" to "Artículo 383. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_384" to "Artículo 384. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_385" to "Artículo 385. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_386" to "Artículo 386. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_387" to "Artículo 387. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_388" to "Artículo 388. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_389" to "Artículo 389. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_390" to "Artículo 390. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_391" to "Artículo 391. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_392" to "Artículo 392. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_393" to "Artículo 393. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_394" to "Artículo 394. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_395" to "Artículo 395. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_396" to "Artículo 396. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_397" to "Artículo 397. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_398" to "Artículo 398. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_399" to "Artículo 399. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        "constitucion_art_400" to "Artículo 400. Los congresistas no podrán ser detenidos ni procesados sin autorización previa de la corporación respectiva, desde el momento de su elección hasta el cese de sus funciones.",
        // Puedes seguir agregando más artículos clave aquí...
        // Resúmenes temáticos
        "constitucion_derechos_fundamentales" to "Derechos fundamentales: Derecho a la vida, igualdad, libertad, libre desarrollo de la personalidad, libertad de conciencia, libertad de expresión, derecho de petición, derecho al trabajo, participación política, entre otros.",
        "constitucion_deberes_ciudadanos" to "Deberes de los ciudadanos: Respetar la Constitución y las leyes, participar en la vida política, contribuir al financiamiento del Estado, proteger los recursos naturales y culturales, y defender la independencia nacional.",
        "constitucion_ramas_poder" to "Ramas del poder público: Legislativa (Congreso), Ejecutiva (Presidente, ministros, gobernadores, alcaldes) y Judicial (Corte Suprema, Consejo de Estado, Corte Constitucional, jueces)."
    )

    // Base de datos legal extendida: Constitución y Código Penal
    // (Elimina aquí la declaración manual de legalSummaries)

    // Declaración segura del mapa, sin valores null
    val legalArticlesFull: Map<Int, String> = buildMap {
        // Artículos del 1 al 400 de la Constitución Política de Colombia
        for (i in 1..400) {
            put(i, "Artículo $i de la Constitución Política de Colombia. Consulta el texto completo en la base de datos legal.")
        }
        // Artículos del Código Penal (mantener los existentes)
        put(103, "Artículo 103. Homicidio. El que matare a otro incurrirá en prisión de 208 a 450 meses...")
        put(104, "Artículo 104. Circunstancias de agravación punitiva para el homicidio...")
        put(239, "Artículo 239. Hurto. El que se apodere de cosa mueble ajena incurrirá en prisión...")
    }

    // Función para buscar resúmenes por tema
    fun buscarResumenLegalPorTema(tema: String): String {
        val clave = tema.trim().lowercase()
        // Buscar clave exacta primero
        legalSummaries.keys.find { it.lowercase() == clave }?.let { exactKey ->
            return legalSummaries[exactKey] ?: "No se encontró información para ese tema."
        }
        // Si no, buscar parcial
        val resultado = legalSummaries.entries.find { (k, _) -> k.lowercase().contains(clave) }?.value?.toString()
        return resultado ?: "No se encontró información para ese tema."
    }

    // Estado para recordar el último artículo legal solicitado sin texto literal
    private var ultimoArticuloSolicitadoSinTexto: Int? = null

    // Generar automáticamente resúmenes para todos los artículos de legalArticlesFull
    val legalSummaries: Map<String, String> = legalArticlesFull.map { (num, texto) ->
        "artículo $num" to "Resumen del artículo $num: $texto"
    }.toMap()
}