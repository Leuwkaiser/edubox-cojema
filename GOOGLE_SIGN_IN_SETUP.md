# 🔐 Configuración de Google Sign-In para EduBox

## 📋 Requisitos

### 1. **Dependencias ya agregadas**
Las siguientes dependencias ya están incluidas en `app/build.gradle`:
```gradle
implementation 'com.google.android.gms:play-services-auth:20.7.0'
```

### 2. **Firebase ya configurado**
- ✅ Firebase Auth
- ✅ Firestore Database
- ✅ google-services.json

## 🔑 Configurar Google Sign-In

### **Paso 1: Configurar Firebase Console**

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Authentication** → **Sign-in method**
4. Habilita **Google** como proveedor
5. Configura el **Web Client ID**

### **Paso 2: Obtener Web Client ID**

1. En Firebase Console, ve a **Project Settings**
2. Pestaña **General**
3. Sección **Your apps**
4. Busca tu app web o crea una nueva
5. Copia el **Web Client ID** (empieza con números)

### **Paso 3: Configurar en la app**

1. Abre el archivo: `app/src/main/java/com/example/buzondesugerenciascojema/data/GoogleAuthService.kt`
2. Reemplaza `"YOUR_WEB_CLIENT_ID"` con tu Web Client ID real:
```kotlin
.requestIdToken("123456789-abcdefghijklmnop.apps.googleusercontent.com")
```

### **Paso 4: Configurar MainActivity**

Necesitas agregar el manejo de Google Sign-In en `MainActivity.kt`:

```kotlin
// Agregar imports
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts

// En MainActivity
class MainActivity : ComponentActivity() {
    private lateinit var googleAuthService: GoogleAuthService
    
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            handleGoogleSignIn(account)
        } catch (e: ApiException) {
            // Manejar error
        }
    }
    
    private fun handleGoogleSignIn(account: GoogleSignInAccount) {
        lifecycleScope.launch {
            val result = googleAuthService.signInWithGoogle(account)
            result.fold(
                onSuccess = { user ->
                    if (user.curso.isBlank() || user.grado.isBlank()) {
                        // Usuario nuevo, ir a completar perfil
                        navController.navigate("complete_profile")
                    } else {
                        // Usuario existente, ir a home
                        navController.navigate("home")
                    }
                },
                onFailure = { exception ->
                    // Manejar error
                }
            )
        }
    }
}
```

## 🚀 Cómo funciona

### **Flujo de autenticación:**

1. **Usuario presiona "Continuar con Google"**
2. **Se abre el selector de cuentas de Google**
3. **Usuario selecciona su cuenta**
4. **Si es usuario nuevo:**
   - Se crea registro básico en Firestore
   - Se navega a pantalla de completar perfil
   - Usuario ingresa nombre, curso, grado, código
5. **Si es usuario existente:**
   - Se cargan sus datos de Firestore
   - Se navega directamente a home

### **Datos que se guardan:**
- ✅ **ID único** (de Firebase Auth)
- ✅ **Nombre** (de Google)
- ✅ **Email** (de Google)
- ✅ **Curso** (ingresado por usuario)
- ✅ **Grado** (ingresado por usuario)
- ✅ **Código** (ingresado por usuario)
- ✅ **Fecha de registro**

## 🔧 Configuración Avanzada

### **Personalizar campos:**
En `CompleteProfileScreen.kt` puedes modificar:
- Lista de grados disponibles
- Lista de cursos disponibles
- Campos adicionales

### **Validaciones:**
- Email debe ser válido
- Código debe ser numérico
- Todos los campos son obligatorios

## ⚠️ Consideraciones

### **Seguridad:**
- ✅ Token de Google se valida en Firebase
- ✅ Datos se guardan en Firestore con reglas de seguridad
- ✅ Usuario solo puede editar su propio perfil

### **Privacidad:**
- ✅ Solo se solicita email y nombre de Google
- ✅ No se accede a otros datos de la cuenta
- ✅ Cumple con políticas de Google

## 🐛 Solución de Problemas

### **Error: "Google Sign-In failed"**
- Verifica que Google esté habilitado en Firebase
- Confirma que el Web Client ID sea correcto
- Asegúrate de que google-services.json esté actualizado

### **Error: "User not found"**
- Verifica las reglas de Firestore
- Confirma que la colección "usuarios" exista
- Revisa los logs de Firebase

### **Error: "Invalid token"**
- El token puede haber expirado
- Intenta cerrar sesión y volver a iniciar
- Verifica la configuración de Firebase

## 📞 Soporte

Si tienes problemas:
1. Verifica la configuración de Firebase
2. Confirma que el Web Client ID sea correcto
3. Revisa los logs de la app
4. Verifica las reglas de Firestore

---

**¡Listo! Tu app ahora tiene autenticación con Google. 🚀** 