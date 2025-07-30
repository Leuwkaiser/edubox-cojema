# EduBox - Buzón de Sugerencias COJEMA

## 📱 Descripción

EduBox es una aplicación móvil educativa desarrollada para el Colegio Jesús María (COJEMA) que permite a los estudiantes enviar sugerencias, acceder a una biblioteca virtual, jugar minijuegos educativos y utilizar un asistente virtual inteligente.

## ✨ Características Principales

### 🎯 Buzón de Sugerencias
- Envío de sugerencias por grado y grupo
- Sistema de filtrado inteligente
- Panel de administración para docentes
- Notificaciones automáticas

### 📚 Biblioteca Virtual
- Subida y visualización de documentos
- Categorización por materias
- Búsqueda avanzada
- Acceso desde cualquier dispositivo

### 🎮 Minijuegos Educativos
- Snake Game
- Tetris
- Space Invaders
- Bubble Shooter
- Dino Runner
- Tres en Raya
- Buscaminas
- Pong
- Trivia (preguntas por grado)

### 🤖 Asistente Virtual
- IA integrada para consultas educativas
- Ayuda contextual según la sección
- Respuestas personalizadas

### 📅 Calendario Escolar
- Eventos importantes del colegio
- Recordatorios personalizados
- Integración con notificaciones

### 🔔 Sistema de Notificaciones
- Notificaciones push automáticas
- Notificaciones locales
- Configuración personalizable

## 🛠️ Tecnologías Utilizadas

- **Frontend**: Jetpack Compose (Kotlin)
- **Backend**: Firebase (Firestore, Authentication, Cloud Functions)
- **Autenticación**: Google Sign-In, Firebase Auth
- **Notificaciones**: Firebase Cloud Messaging
- **Base de Datos**: Cloud Firestore
- **Hosting**: Firebase Hosting

## 📋 Requisitos del Sistema

- Android 6.0 (API level 23) o superior
- Conexión a internet
- Cuenta de Google (para autenticación)

## 🚀 Instalación

### Para Desarrolladores

1. **Clonar el repositorio**
   ```bash
   git clone https://github.com/tu-usuario/edubox-cojema.git
   cd edubox-cojema
   ```

2. **Configurar Firebase**
   - Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Descargar `google-services.json` y colocarlo en la carpeta `app/`
   - Configurar las reglas de Firestore

3. **Configurar Google Sign-In**
   - Obtener el SHA-1 fingerprint de tu proyecto
   - Configurar OAuth 2.0 en Google Cloud Console
   - Actualizar el Client ID en `GoogleAuthService.kt`

4. **Compilar y ejecutar**
   ```bash
   ./gradlew build
   ```

### Para Usuarios Finales

1. Descargar el APK desde la sección de releases
2. Instalar en dispositivo Android
3. Configurar cuenta de Google
4. ¡Listo para usar!

## 📁 Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/example/buzondesugerenciascojema/
│   │   ├── data/           # Servicios y modelos de datos
│   │   ├── ui/             # Interfaces de usuario
│   │   ├── screens/        # Pantallas de la aplicación
│   │   ├── viewmodels/     # ViewModels
│   │   └── util/           # Utilidades
│   └── res/                # Recursos (imágenes, strings, etc.)
├── build.gradle           # Configuración de Gradle
└── google-services.json   # Configuración de Firebase
```

## 🔧 Configuración

### Variables de Entorno

El proyecto utiliza las siguientes configuraciones:

- **Firebase Project ID**: Configurado en `google-services.json`
- **Google Sign-In Client ID**: Configurado en `GoogleAuthService.kt`
- **FCM Server Key**: Para notificaciones push

### Permisos Requeridos

- `INTERNET`: Para conexión con Firebase
- `POST_NOTIFICATIONS`: Para notificaciones (Android 13+)
- `READ_EXTERNAL_STORAGE`: Para subir documentos
- `WRITE_EXTERNAL_STORAGE`: Para descargar documentos

## 🎯 Funcionalidades por Rol

### 👨‍🎓 Estudiante
- Enviar sugerencias
- Acceder a biblioteca virtual
- Jugar minijuegos
- Usar asistente virtual
- Ver calendario escolar

### 👨‍🏫 Docente/Administrador
- Revisar sugerencias
- Gestionar documentos
- Enviar notificaciones
- Administrar contenido

## 🐛 Solución de Problemas

### Problema de Autenticación Google
Si tienes problemas con el inicio de sesión de Google, consulta el archivo `SOLUCION_GOOGLE_SIGN_IN.md` para la solución detallada.

### Logs de Depuración
Para ver logs en tiempo real:
```bash
adb logcat | grep "GOOGLE_SIGN_IN\|GoogleAuthService"
```

## 🤝 Contribución

1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Desarrollador

- **Nombre**: [Tu Nombre]
- **Email**: [tu-email@ejemplo.com]
- **GitHub**: [@tu-usuario]

## 🙏 Agradecimientos

- Colegio Jesús María (COJEMA) por la confianza
- Firebase por proporcionar la infraestructura
- Comunidad de desarrolladores Android
- Todos los contribuidores del proyecto

## 📞 Soporte

Si tienes alguna pregunta o problema:

1. Revisa la documentación
2. Busca en los issues existentes
3. Crea un nuevo issue con detalles del problema

---

**EduBox** - Transformando la educación a través de la tecnología 🚀 