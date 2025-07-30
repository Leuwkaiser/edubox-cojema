# Reglas de Firebase Firestore

## 📋 Descripción

Este proyecto utiliza Firebase Firestore como base de datos. Las reglas de seguridad están configuradas para proteger los datos y permitir solo las operaciones autorizadas.

## 🚨 PROBLEMA IDENTIFICADO: Ranking no funciona

### Diagnóstico:
El problema con el ranking que no muestra puntuaciones puede deberse a:

1. **Reglas de Firestore incorrectas**: Las reglas de producción requieren `fecha` como `timestamp`, pero el código usa `Long`
2. **Conexión a Firestore**: Problemas de autenticación o configuración
3. **Datos vacíos**: No hay datos en la colección `rankings`

### Solución Implementada:
- ✅ Corregido el tipo de dato para `fecha` (ahora usa `Timestamp.now()`)
- ✅ Agregados logs de depuración extensivos
- ✅ Función de prueba de conexión agregada
- ✅ Función para agregar datos de prueba

## 📁 Archivos de Reglas

### `firestore.rules` (Desarrollo)
- **Propósito**: Reglas para desarrollo y pruebas
- **Seguridad**: Permite todas las operaciones (NO USAR EN PRODUCCIÓN)
- **Uso**: Para desarrollo local y pruebas

### `firestore.rules.production` (Producción)
- **Propósito**: Reglas seguras para producción
- **Seguridad**: Reglas estrictas con validaciones
- **Uso**: Para el entorno de producción

## 🔐 Reglas por Colección

### 1. **Usuarios** (`/usuarios/{userId}`)
- **Lectura**: Solo el propio usuario
- **Escritura**: Solo el propio usuario

### 2. **Sugerencias** (`/sugerencias/{sugerenciaId}`)
- **Lectura**: Usuarios autenticados
- **Creación**: Usuarios autenticados (solo con su email)
- **Actualización**: Admin o creador de la sugerencia
- **Eliminación**: Solo administradores

### 3. **Documentos** (`/documentos/{documentoId}`)
- **Lectura**: Público (todos pueden ver documentos)
- **Escritura**: Solo administradores

### 4. **Eventos** (`/eventos/{eventoId}`)
- **Lectura**: Público (todos pueden ver eventos)
- **Escritura**: Solo administradores

### 5. **Rankings** (`/rankings/{rankingId}`) ⭐ NUEVO
- **Lectura**: Público (todos pueden ver rankings)
- **Creación**: Usuarios autenticados con validaciones:
  - Puntuación entre 1 y 10,000
  - Nombre de usuario válido (1-100 caracteres)
  - Email debe coincidir con el usuario autenticado
  - Fecha timestamp requerida
- **Actualización/Eliminación**: No permitido (solo crear nuevas puntuaciones)

## 🚀 Despliegue

### Para Desarrollo:
```bash
firebase deploy --only firestore:rules
```

### Para Producción:
1. Copiar `firestore.rules.production` a `firestore.rules`
2. Desplegar:
```bash
firebase deploy --only firestore:rules
```

## ⚠️ Importante

### Antes de Desplegar a Producción:
1. **Cambiar reglas**: Reemplazar `firestore.rules` con el contenido de `firestore.rules.production`
2. **Probar**: Verificar que todas las funcionalidades funcionen correctamente
3. **Validar**: Usar Firebase Emulator para probar las reglas

### Validación de Reglas:
```bash
# Instalar Firebase CLI si no está instalado
npm install -g firebase-tools

# Iniciar emulador
firebase emulators:start

# Probar reglas
firebase firestore:rules:test
```

## 🔧 Configuración del Proyecto

### Variables de Entorno:
- `GOOGLE_APPLICATION_CREDENTIALS`: Ruta al archivo de credenciales de servicio
- `FIREBASE_PROJECT_ID`: ID del proyecto de Firebase

### Estructura de Datos:
```
/usuarios/{userId}
  - nombreCompleto: string
  - email: string
  - grado: string
  - grupo: string
  - esAdmin: boolean

/sugerencias/{sugerenciaId}
  - titulo: string
  - descripcion: string
  - creadoPor: string (email)
  - fecha: timestamp
  - estado: string

/documentos/{documentoId}
  - titulo: string
  - descripcion: string
  - url: string
  - materia: string
  - grado: string
  - fechaSubida: timestamp

/eventos/{eventoId}
  - titulo: string
  - descripcion: string
  - fecha: timestamp
  - creadoPor: string

/rankings/{rankingId} ⭐ NUEVO
  - juego: string
  - puntuacion: number
  - nombreUsuario: string
  - emailUsuario: string
  - fecha: timestamp
```

## 🛡️ Seguridad

### Validaciones Implementadas:
- ✅ Autenticación requerida para operaciones sensibles
- ✅ Verificación de propiedad de datos
- ✅ Límites en tamaños de campos
- ✅ Validación de tipos de datos
- ✅ Prevención de manipulación de rankings

### Recomendaciones:
- Revisar logs de Firestore regularmente
- Monitorear intentos de acceso no autorizado
- Actualizar reglas según evolucione la aplicación

## 🔍 Solución de Problemas

### Ranking no muestra datos:
1. **Verificar conexión**: Usar la función `probarConexion()` en RankingService
2. **Verificar reglas**: Asegurar que las reglas permiten lectura pública
3. **Verificar datos**: Usar la función `agregarDatosPrueba()` para crear datos de prueba
4. **Verificar logs**: Revisar los logs de depuración en la consola

### Comandos útiles:
```bash
# Verificar estado de Firestore
firebase firestore:indexes

# Ver logs en tiempo real
firebase functions:log

# Probar reglas localmente
firebase emulators:start --only firestore
``` 