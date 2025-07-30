# 📚 Guía para Agregar Portadas a la Biblioteca

## 🎯 ¿Cómo funciona el sistema de portadas?

La aplicación **EduBox** ahora permite usar imágenes locales como portadas de los libros, lo que hace que la app sea más rápida y no dependa de enlaces externos.

## 📁 Portadas Disponibles Actualmente

Las siguientes portadas están disponibles en la aplicación:

| Nombre del Archivo | Nombre Amigable | Descripción |
|-------------------|-----------------|-------------|
| `libro.png` | Libro por defecto | Portada genérica de libro |
| `logo_cojema.jpeg` | Logo COJEMA | Logo del colegio |
| `logoia.jpeg` | Logo IA | Logo de inteligencia artificial |
| `gamepad.png` | Icono de juego | Icono de videojuegos |
| `calendario.png` | Icono de calendario | Icono de calendario |
| `buzon.png` | Icono de buzón | Icono de buzón de sugerencias |
| `snake.png` | Icono de serpiente | Icono del juego Snake |
| `logro.png` | Icono de logro | Icono de logros |

## ➕ Cómo Agregar Nuevas Portadas

### Paso 1: Preparar la imagen
1. **Formato**: Usa PNG, JPG o JPEG
2. **Tamaño recomendado**: 200x200 píxeles o más
3. **Nombre del archivo**: Solo letras minúsculas, números y guiones bajos
   - ✅ Correcto: `matematicas_grado6.png`
   - ❌ Incorrecto: `Matemáticas Grado 6.png`

### Paso 2: Agregar la imagen al proyecto
1. Coloca la imagen en la carpeta: `app/src/main/res/drawable/`
2. Asegúrate de que el nombre del archivo sea válido

### Paso 3: Actualizar el código
1. Abre el archivo: `app/src/main/java/com/example/buzondesugerenciascojema/model/Documento.kt`
2. En la sección `PortadasDisponibles`, agrega tu nueva portada:

```kotlin
object PortadasDisponibles {
    val PORTADAS = mapOf(
        "libro" to "Libro por defecto",
        "logo_cojema" to "Logo COJEMA",
        "logoia" to "Logo IA",
        "gamepad" to "Icono de juego",
        "calendario" to "Icono de calendario",
        "buzon" to "Icono de buzón",
        "snake" to "Icono de serpiente",
        "logro" to "Icono de logro",
        // 🆕 AGREGAR AQUÍ TU NUEVA PORTADA
        "matematicas_grado6" to "Matemáticas Grado 6"
    )
    // ... resto del código
}
```

### Paso 4: Actualizar las pantallas
Necesitas agregar el caso en las pantallas que muestran las portadas:

1. **BibliotecaScreen.kt** (línea ~350)
2. **VisualizarDocumentoScreen.kt** (línea ~80)
3. **SubirDocumentoScreen.kt** (línea ~180)

En cada archivo, agrega el caso en el `when`:

```kotlin
when (documento.portadaDrawable) {
    "libro" -> R.drawable.libro
    "logo_cojema" -> R.drawable.logo_cojema
    // ... otros casos
    "matematicas_grado6" -> R.drawable.matematicas_grado6  // 🆕 AGREGAR AQUÍ
    else -> R.drawable.libro
}
```

## 🎨 Ejemplos de Portadas por Materia

Aquí tienes algunas ideas para nombrar las portadas por materia:

### Matemáticas
- `matematicas_algebra.png`
- `matematicas_geometria.png`
- `matematicas_calculo.png`

### Ciencias
- `ciencias_biologia.png`
- `ciencias_quimica.png`
- `ciencias_fisica.png`

### Historia
- `historia_colombia.png`
- `historia_universal.png`
- `historia_contemporanea.png`

### Literatura
- `literatura_colombiana.png`
- `literatura_universal.png`
- `literatura_poesia.png`

## ✅ Ventajas de Usar Portadas Locales

1. **🚀 Más rápida**: No necesita descargar imágenes desde internet
2. **📱 Sin conexión**: Funciona sin internet
3. **🔄 Confiable**: No depende de enlaces que pueden romperse
4. **🎨 Consistente**: Todas las portadas tienen el mismo estilo
5. **💾 Menos datos**: No consume datos móviles

## 🔧 Cómo Usar en la Aplicación

1. **Como administrador**, ve a "Subir Nuevo Documento"
2. Selecciona "Portada local" en lugar de "Enlace externo"
3. Elige la portada que quieres usar de la lista
4. La portada se guardará automáticamente con el libro

## 🆘 Solución de Problemas

### La imagen no aparece
- Verifica que el nombre del archivo esté correcto
- Asegúrate de que esté en la carpeta `drawable`
- Revisa que el nombre en el código coincida exactamente

### Error de compilación
- Verifica que el nombre del archivo no tenga espacios ni caracteres especiales
- Asegúrate de que el archivo sea una imagen válida

### La portada no se muestra en la app
- Verifica que hayas agregado el caso en todos los archivos necesarios
- Reinicia la aplicación después de los cambios

---

**💡 Consejo**: Mantén un archivo de respaldo de todas las portadas que agregues para poder restaurarlas fácilmente si es necesario. 