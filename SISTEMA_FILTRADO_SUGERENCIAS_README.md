# Sistema de Filtrado Automático de Sugerencias - EduBox COJEMA

## 📋 Descripción General

El sistema de filtrado automático de sugerencias es una funcionalidad implementada en la aplicación EduBox COJEMA que tiene como objetivo mantener la calidad del contenido en el buzón de sugerencias, rechazando automáticamente sugerencias inapropiadas, bromas o contenido que no cumple con los estándares educativos.

## 🎯 Objetivos

- **Prevenir contenido inapropiado**: Detectar y rechazar sugerencias con lenguaje vulgar o ofensivo
- **Mantener calidad educativa**: Asegurar que las sugerencias sean constructivas y relevantes
- **Reducir carga administrativa**: Automatizar el proceso de moderación inicial
- **Fomentar participación responsable**: Guiar a los estudiantes hacia sugerencias de calidad

## 🔧 Componentes del Sistema

### 1. Validación de Contenido (`SugerenciaService.kt`)

#### Palabras Inapropiadas
El sistema incluye una base de datos de palabras y frases inapropiadas:

- **Groserías comunes**: Lista extensa de palabras vulgares en español
- **Palabras ofensivas**: Términos despectivos o insultantes
- **Expresiones vulgares**: Frases completas inapropiadas
- **Contenido sexual**: Palabras relacionadas con contenido sexual inapropiado
- **Violencia**: Términos relacionados con violencia o agresión
- **Insultos colombianos**: Palabras específicas del contexto costeño y colombiano

#### Insultos Colombianos y Costeños
El sistema incluye una base de datos especializada de insultos regionales:

- **Gonorrea**: Usado como insulto en Colombia (además de ser una enfermedad)
- **Hijueputa/Jueputa**: Versión colombiana de "hijo de puta"
- **Malparido/a**: Mal nacido, mal parado
- **Pichurria**: Algo insignificante, que no vale la pena
- **Pirobo**: Un bueno para nada
- **Zunga**: Palabra muy ofensiva para referirse a una mujer
- **Sapo hijueputa**: Para alguien muy chismoso
- **Huevon/a**: Tonto, lento
- **Lampara**: Persona sin vergüenza
- **Ñero/a**: Sucio, mal vestido, que habla feo
- **Gurrupleto/a**: Persona despreciable que actúa con maldad
- **Garbimba**: Mala calaña, sin costumbres
- **Care monda/Care chimba**: Cara de culo, cara de miembro
- **Tontarron/a**: Sinónimo de tonto pero con énfasis
- **Lambon/a**: Persona que adula excesivamente
- **Cacorro/a**: Homosexual (usado como insulto)
- **Palabras con "monda"**: Cualquier frase que contenga "monda" es automáticamente rechazada
- **Todos los insultos**: Cualquier frase que contenga insultos es automáticamente rechazada

#### Frases Inapropiadas
Detecta frases que indican que la sugerencia no es seria:

- "por joder", "para molestar", "para fastidiar"
- "no tiene sentido", "sin sentido", "tontería"
- "broma", "chiste", "gracia"
- "test", "prueba", "experimento"
- Saludos genéricos como "hola", "buenos días"

### 2. Análisis de Sentimiento

#### Palabras Constructivas
Identifica contenido positivo y constructivo:

- "mejorar", "sugerencia", "propuesta", "recomendación"
- "implementar", "cambiar", "agregar", "necesitamos"
- "debería", "sería", "podría", "sugiero"
- "considerar", "evaluar", "revisar", "actualizar"

#### Palabras Negativas
Detecta contenido excesivamente negativo:

- "odio", "detesto", "molesta", "fastidia"
- "terrible", "horrible", "pésimo", "inútil"
- "sin sentido", "desperdicio", "no sirve"

### 3. Validaciones Técnicas

#### Longitud de Contenido
- **Título**: Mínimo 5 caracteres, máximo 100 caracteres
- **Contenido**: Mínimo 20 caracteres, máximo 1000 caracteres

#### Detección de Spam
- **Repetición excesiva**: Detecta caracteres repetidos más de 5 veces seguidas
- **Caracteres especiales**: Verifica que al menos el 30% del contenido sean letras
- **Variedad de palabras**: Calcula el ratio de palabras únicas vs total

#### Validación Especial de Insultos
- **Detección automática**: Cualquier frase que contenga insultos es rechazada inmediatamente
- **Sin excepciones**: No importa el contexto, cualquier uso de insultos es considerado inapropiado
- **Validación prioritaria**: Se ejecuta antes que otras validaciones
- **Palabra "monda"**: Cualquier frase con "monda" es rechazada automáticamente
- **Ejemplos bloqueados**: 
  - "chupame la monda", "esa monda", "la monda esa"
  - "mucho huevon", "es un pirobo", "esa zunga"
  - "gonorrea hijueputa", "malparido", "pichurria"
  - Cualquier combinación con insultos colombianos

## 📊 Funcionalidades de Reportes

### Estadísticas de Sugerencias
- Total de sugerencias por grado y grupo
- Sugerencias aprobadas, rechazadas y pendientes
- Porcentaje de aprobación
- Visualización con gráficos y progreso

### Reporte de Contenido Problemático
- Total de sugerencias rechazadas
- Top 5 autores con más sugerencias rechazadas
- Top 5 motivos más comunes de rechazo
- Fecha de generación del reporte

### Notificaciones Automáticas
- **Notificación inmediata** a administradores cuando se detecta contenido inapropiado
- **Información detallada**: Nombre del estudiante, grado, grupo, motivo de rechazo
- **Contenido completo**: Título y contenido de la sugerencia rechazada
- **Tipo especial**: Notificación "SUGERENCIA_INNAPROPIADA" con ícono distintivo

## 🎨 Interfaz de Usuario

### Pantalla de Creación de Sugerencias
- **Validación en tiempo real**: Muestra errores mientras el usuario escribe
- **Reglas visibles**: Información clara sobre las reglas de validación
- **Mensajes de error específicos**: Explica exactamente por qué se rechazó la sugerencia
- **Indicadores visuales**: Campos en rojo cuando hay errores

### Panel de Administración Mejorado
- **Estadísticas expandibles**: Sección colapsable con métricas detalladas
- **Reporte de contenido problemático**: Análisis de patrones de rechazo
- **Gestión simplificada**: Botones claros para aprobar/rechazar
- **Actualización automática**: Recarga datos al procesar sugerencias

## 🔄 Flujo de Validación

1. **Entrada de datos**: Usuario ingresa título y contenido
2. **Validación en tiempo real**: Sistema verifica reglas básicas
3. **Análisis completo**: Al intentar crear, se ejecuta validación completa
4. **Validación especial de insultos**: Cualquier frase con insultos es rechazada inmediatamente
5. **Notificación automática**: Se notifica a los administradores del intento de sugerencia inapropiada
6. **Resultado**: 
   - ✅ **Aprobada**: Sugerencia válida, se crea normalmente
   - ❌ **Rechazada**: Muestra mensaje específico del error
7. **Feedback**: Usuario recibe explicación clara del problema

## 📈 Métricas y Análisis

### Cálculo de Sentimiento
```kotlin
ratioNegativo = palabrasNegativas / totalPalabras
ratioConstructivo = palabrasConstructivas / totalPalabras

esMuyNegativo = ratioNegativo > 0.1 (10%)
esPocoConstructivo = ratioConstructivo < 0.05 (5%) && totalPalabras > 10
```

### Detección de Spam
```kotlin
ratio = palabrasUnicas / totalPalabras
esSpam = ratio < 0.3 && totalPalabras > 10
```

## 🛠️ Configuración y Personalización

### Agregar Nuevas Palabras
Para agregar nuevas palabras inapropiadas, editar en `SugerenciaService.kt`:

```kotlin
private val palabrasInapropiadas = setOf(
    // Groserías comunes
    "puta", "hijo de puta", "hdp", "pendejo", "pendeja",
    
    // Insultos colombianos específicos
    "gonorrea", "hijueputa", "jueputa", "malparido", "pichurria",
    "pirobo", "zunga", "sapo hijueputa", "huevon", "lampara",
    "ñero", "gurrupleto", "garbimba", "care monda", "tontarron",
    "lambon", "cacorro",
    
    // Palabras con "monda"
    "monda", "mondas", "mondón", "mondero", "mondear",
    
    // Agregar nuevas palabras aquí
    "nueva_palabra_inapropiada",
    // ...
)
```

### Ajustar Umbrales
Modificar los valores de detección en las funciones de análisis:

```kotlin
// Cambiar porcentaje de palabras negativas permitidas
esMuyNegativo = ratioNegativo > 0.15 // 15% en lugar de 10%

// Cambiar porcentaje de palabras constructivas requeridas
esPocoConstructivo = ratioConstructivo < 0.03 // 3% en lugar de 5%
```

## 📱 Experiencia del Usuario

### Para Estudiantes
- **Guía clara**: Reglas visibles antes de crear sugerencias
- **Feedback inmediato**: Errores se muestran al escribir
- **Explicaciones específicas**: Saben exactamente qué corregir
- **Oportunidad de mejora**: Pueden reescribir sugerencias rechazadas

### Para Administradores
- **Vista general**: Estadísticas completas del sistema
- **Análisis de patrones**: Identificación de problemas recurrentes
- **Gestión eficiente**: Procesamiento rápido de sugerencias válidas
- **Reportes detallados**: Información para mejorar el sistema
- **Notificaciones automáticas**: Alertas inmediatas sobre intentos de contenido inapropiado
- **Información completa**: Datos del estudiante y contenido rechazado para seguimiento

## 🔮 Mejoras Futuras

### Análisis Avanzado
- **Machine Learning**: Detección más sofisticada de contenido problemático
- **Análisis de contexto**: Considerar el contexto de las palabras
- **Detección de sarcasmo**: Identificar comentarios irónicos

### Personalización
- **Configuración por grado**: Diferentes reglas según la edad
- **Ajustes por institución**: Personalización según políticas escolares
- **Listas personalizables**: Administradores pueden agregar palabras específicas

### Integración
- **API de moderación**: Conectar con servicios externos de moderación
- **Análisis de sentimiento**: Integrar con APIs de análisis de sentimiento
- **Reportes automáticos**: Envío de reportes por email

## 📞 Soporte

Para preguntas o sugerencias sobre el sistema de filtrado:

1. **Documentación técnica**: Revisar comentarios en el código
2. **Logs de error**: Verificar consola para errores de validación
3. **Configuración**: Ajustar parámetros según necesidades específicas

---

**Desarrollado para EduBox COJEMA**  
*Sistema de filtrado automático para mantener la calidad educativa* 