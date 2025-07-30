# Configuración de Git y GitHub para EduBox

## 🚀 Pasos para Configurar Git

### 1. Configurar tu Identidad
```bash
git config --global user.name "Tu Nombre"
git config --global user.email "tu-email@ejemplo.com"
```

### 2. Hacer el Primer Commit
```bash
git add .
git commit -m "Initial commit: EduBox - Buzón de Sugerencias COJEMA

- Aplicación móvil educativa completa
- Sistema de autenticación Google
- Buzón de sugerencias con filtrado
- Biblioteca virtual
- Minijuegos educativos
- Asistente virtual con IA
- Sistema de notificaciones
- Solución para problema de autenticación Google
- Documentación completa"
```

## 📁 Crear Repositorio en GitHub

### 1. Ir a GitHub.com
- Crear cuenta si no tienes una
- Hacer clic en "New repository"

### 2. Configurar el Repositorio
- **Repository name**: `edubox-cojema`
- **Description**: Aplicación móvil educativa para el Colegio Jesús María
- **Visibility**: Public (recomendado) o Private
- **NO** marcar "Add a README file" (ya tenemos uno)
- **NO** marcar "Add .gitignore" (ya tenemos uno)

### 3. Conectar tu Repositorio Local con GitHub
```bash
git remote add origin https://github.com/tu-usuario/edubox-cojema.git
git branch -M main
git push -u origin main
```

## 🔄 Comandos Git Básicos

### Ver Estado
```bash
git status
```

### Ver Cambios
```bash
git diff
```

### Hacer Commit
```bash
git add .
git commit -m "Descripción de los cambios"
```

### Subir Cambios
```bash
git push
```

### Crear Nueva Rama
```bash
git checkout -b nombre-de-la-rama
```

### Cambiar de Rama
```bash
git checkout main
```

## 📋 Flujo de Trabajo Recomendado

### Para Nuevas Características
1. Crear nueva rama: `git checkout -b feature/nueva-caracteristica`
2. Hacer cambios en el código
3. Hacer commit: `git commit -m "Agregar nueva característica"`
4. Subir rama: `git push origin feature/nueva-caracteristica`
5. Crear Pull Request en GitHub
6. Revisar y hacer merge

### Para Correcciones
1. Crear rama: `git checkout -b fix/nombre-del-problema`
2. Corregir el problema
3. Hacer commit: `git commit -m "Corregir problema específico"`
4. Subir y crear Pull Request

## 🛡️ Buenas Prácticas

### Mensajes de Commit
- Usar imperativo: "Agregar", "Corregir", "Mejorar"
- Ser específico: "Corregir problema de autenticación Google"
- Usar inglés o español consistentemente

### Estructura de Ramas
- `main`: Código estable y funcional
- `develop`: Rama de desarrollo
- `feature/*`: Nuevas características
- `fix/*`: Correcciones
- `hotfix/*`: Correcciones urgentes

### Archivos a NO Subir
- `google-services.json` (contiene claves secretas)
- Archivos de build (`build/`, `.gradle/`)
- Archivos de IDE (`.idea/`, `.vscode/`)
- Logs y archivos temporales

## 🔐 Seguridad

### Archivos Sensibles
- **NO** subir `google-services.json` a repositorios públicos
- **NO** subir claves de API o tokens
- **SÍ** subir archivos de configuración de ejemplo

### Alternativa para Archivos Sensibles
1. Crear `google-services.example.json`
2. Agregar `google-services.json` al `.gitignore`
3. Documentar en README cómo obtener el archivo real

## 📊 Beneficios de Usar GitHub

### Para tu Proyecto
- ✅ Respaldo automático en la nube
- ✅ Historial completo de cambios
- ✅ Colaboración con otros desarrolladores
- ✅ Portfolio profesional
- ✅ Control de versiones robusto

### Para tu Carrera
- ✅ Demostrar habilidades técnicas
- ✅ Participar en proyectos open source
- ✅ Aprender de otros desarrolladores
- ✅ Construir reputación profesional

## 🎯 Próximos Pasos

1. **Configurar Git** con tu información
2. **Crear repositorio** en GitHub
3. **Subir el código** inicial
4. **Configurar GitHub Pages** (opcional, para documentación)
5. **Crear releases** para versiones estables
6. **Invitar colaboradores** si es necesario

---

**¡Tu proyecto EduBox merece estar en GitHub!** 🚀 