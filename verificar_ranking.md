# 🔧 Verificación y Solución del Problema del Ranking

## 🚨 Problema Identificado
Los puntajes del ranking no aparecen en los minijuegos.

## 🔍 Diagnóstico

### Posibles Causas:
1. **Reglas de Firestore incorrectas**
2. **Problemas de conexión con Firebase**
3. **Datos vacíos en la colección rankings**
4. **Tipo de dato incorrecto para el campo fecha**

## ✅ Soluciones Implementadas

### 1. Corregido el tipo de dato para fecha
- **Problema**: Las reglas de producción requieren `fecha` como `timestamp`
- **Solución**: Cambiado de `Long` a `Timestamp.now()` en RankingService

### 2. Agregados logs de depuración
- Función `probarConexion()` para verificar conectividad
- Logs detallados en `obtenerTopRanking()`
- Logs en `guardarPuntuacion()`

### 3. Función para agregar datos de prueba
- Función `agregarDatosPrueba()` en RankingService
- Botón temporal en SnakeGameScreen para probar

## 🧪 Pasos para Verificar

### Paso 1: Verificar Conexión
1. Abrir el juego Snake
2. Presionar el botón de ranking (🏆)
3. Revisar los logs en Android Studio/Logcat
4. Buscar mensajes que empiecen con "DEBUG:"

### Paso 2: Agregar Datos de Prueba
1. En el juego Snake, con el ranking abierto
2. Presionar "Agregar Datos de Prueba"
3. Verificar que aparezcan los datos en el ranking

### Paso 3: Verificar Reglas de Firestore
```bash
# Verificar reglas actuales
firebase firestore:rules:get

# Desplegar reglas de desarrollo (permite todo)
firebase deploy --only firestore:rules

# O desplegar reglas de producción
cp firestore.rules.production firestore.rules
firebase deploy --only firestore:rules
```

## 📊 Logs Esperados

### Conexión Exitosa:
```
DEBUG: Probando conexión con Firestore...
DEBUG: Conexión exitosa. Colección accesible.
DEBUG: Obteniendo top ranking para juego: snake, límite: 3
DEBUG: Conectando a Firestore...
DEBUG: Snapshot obtenido, documentos: X
DEBUG: Top ranking obtenido: X entradas
```

### Conexión Fallida:
```
DEBUG: Probando conexión con Firestore...
DEBUG: Error de conexión: [mensaje de error]
```

## 🔧 Comandos de Firebase

### Verificar Estado del Proyecto:
```bash
firebase projects:list
firebase use edubox-f5e3c
firebase firestore:indexes
```

### Ver Logs:
```bash
firebase functions:log
firebase firestore:rules:test
```

### Emulador Local:
```bash
firebase emulators:start --only firestore
```

## 🎯 Próximos Pasos

1. **Probar la aplicación** con los cambios implementados
2. **Revisar logs** para identificar el problema específico
3. **Agregar datos de prueba** si la colección está vacía
4. **Verificar reglas** de Firestore según el entorno

## 📞 Si el Problema Persiste

1. Revisar logs de Firebase Console
2. Verificar configuración de google-services.json
3. Comprobar que el proyecto esté correctamente configurado
4. Verificar permisos de red en la aplicación

## 🔄 Rollback

Si algo no funciona, puedes revertir los cambios:
```bash
git checkout -- app/src/main/java/com/example/buzondesugerenciascojema/data/RankingService.kt
git checkout -- app/src/main/java/com/example/buzondesugerenciascojema/ui/screens/SnakeGameScreen.kt
``` 