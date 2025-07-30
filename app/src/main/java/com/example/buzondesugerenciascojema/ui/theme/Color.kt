package com.example.buzondesugerenciascojema.ui.theme

import androidx.compose.ui.graphics.Color

// Colores principales - Paleta violeta
val VioletaOscuro = Color(0xFF3A0CA3) // Violeta muy oscuro
val VioletaMedio = Color(0xFF7209B7) // Violeta medio
val VioletaClaro = Color(0xFFB5179E) // Violeta claro

// Colores principales - Paleta violeta
val AzulPrincipal = VioletaMedio
val AzulOscuro = VioletaOscuro
val AzulGradiente = VioletaClaro
val AzulClaro = Color(0xFF9D4EDD) // Violeta claro
val AzulMuyClaro = Color(0xFFE0AAFF) // Violeta muy claro para fondos

// Nuevos colores de acento para mejor contraste
val VerdeExito = Color(0xFF4CAF50) // Verde para éxitos
val VerdeClaro = Color(0xFF81C784) // Verde claro
val NaranjaAtencion = Color(0xFFFF9800) // Naranja para advertencias
val NaranjaClaro = Color(0xFFFFB74D) // Naranja claro
val RojoError = Color(0xFFD32F2F) // Rojo para errores y dislikes
val RojoClaro = Color(0xFFE57373) // Rojo claro
val AmarilloDestacado = Color(0xFFFFEB3B) // Amarillo para destacados
val AmarilloClaro = Color(0xFFFFF176) // Amarillo claro

// Colores neutros mejorados
val GrisClaro = Color(0xFF9E9E9E) // Gris para elementos inactivos
val GrisOscuro = Color(0xFF424242) // Gris para texto
val GrisMuyClaro = Color(0xFFF5F5F5) // Gris muy claro para fondos
val Blanco = Color(0xFFFFFFFF) // Blanco
val Negro = Color(0xFF000000) // Negro

// Colores para gradientes y efectos
val GradientePrimario = listOf(VioletaOscuro, VioletaMedio)
val GradienteSecundario = listOf(VerdeExito, VerdeClaro)
val GradienteAtencion = listOf(NaranjaAtencion, NaranjaClaro)
val GradienteError = listOf(RojoError, RojoClaro)

// Colores para juegos
val JuegoFondo = Color(0xFF1A1A1A) // Negro para fondos de juegos
val JuegoBorde = Color(0xFF333333) // Gris oscuro para bordes
val JuegoTexto = Color(0xFFFFFFFF) // Blanco para texto en juegos

// Colores del tema
val md_theme_light_primary = AzulPrincipal
val md_theme_light_onPrimary = Blanco
val md_theme_light_primaryContainer = AzulClaro
val md_theme_light_onPrimaryContainer = AzulOscuro
val md_theme_light_secondary = AzulPrincipal
val md_theme_light_onSecondary = Blanco
val md_theme_light_secondaryContainer = AzulClaro
val md_theme_light_onSecondaryContainer = AzulOscuro
val md_theme_light_tertiary = AzulPrincipal
val md_theme_light_onTertiary = Blanco
val md_theme_light_tertiaryContainer = AzulClaro
val md_theme_light_onTertiaryContainer = AzulOscuro
val md_theme_light_error = RojoError
val md_theme_light_errorContainer = Color(0xFFFFDAD6)
val md_theme_light_onError = Blanco
val md_theme_light_onErrorContainer = Color(0xFF410002)
val md_theme_light_background = AzulMuyClaro
val md_theme_light_onBackground = GrisOscuro
val md_theme_light_surface = Blanco
val md_theme_light_onSurface = GrisOscuro
val md_theme_light_surfaceVariant = AzulClaro
val md_theme_light_onSurfaceVariant = GrisOscuro
val md_theme_light_outline = GrisClaro
val md_theme_light_inverseOnSurface = Color(0xFFF1F0F4)
val md_theme_light_inverseSurface = GrisOscuro
val md_theme_light_inversePrimary = AzulClaro
val md_theme_light_surfaceTint = AzulPrincipal
val md_theme_light_outlineVariant = Color(0xFFC7C5D0)
val md_theme_light_scrim = Color(0xFF000000)

val md_theme_dark_primary = AzulClaro
val md_theme_dark_onPrimary = AzulOscuro
val md_theme_dark_primaryContainer = AzulOscuro
val md_theme_dark_onPrimaryContainer = AzulClaro
val md_theme_dark_secondary = AzulClaro
val md_theme_dark_onSecondary = AzulOscuro
val md_theme_dark_secondaryContainer = AzulOscuro
val md_theme_dark_onSecondaryContainer = AzulClaro
val md_theme_dark_tertiary = AzulClaro
val md_theme_dark_onTertiary = AzulOscuro
val md_theme_dark_tertiaryContainer = AzulOscuro
val md_theme_dark_onTertiaryContainer = AzulClaro
val md_theme_dark_error = RojoClaro
val md_theme_dark_errorContainer = Color(0xFF8B0000)
val md_theme_dark_onError = Negro
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
val md_theme_dark_background = AzulOscuro
val md_theme_dark_onBackground = Blanco
val md_theme_dark_surface = AzulOscuro
val md_theme_dark_onSurface = Blanco
val md_theme_dark_surfaceVariant = Color(0xFF49454F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)
val md_theme_dark_outline = Color(0xFF938F99)
val md_theme_dark_inverseOnSurface = AzulOscuro
val md_theme_dark_inverseSurface = Blanco
val md_theme_dark_inversePrimary = AzulOscuro
val md_theme_dark_surfaceTint = AzulClaro
val md_theme_dark_outlineVariant = Color(0xFF49454F)
val md_theme_dark_scrim = Color(0xFF000000)

// Colores para estados especiales
val EstadoExito = VerdeExito
val EstadoAtencion = NaranjaAtencion
val EstadoError = RojoError
val EstadoInfo = AzulPrincipal
val EstadoNeutral = GrisOscuro

// Colores para elementos interactivos
val BotonPrimario = AzulPrincipal
val BotonSecundario = AzulClaro
val BotonDestacado = VerdeExito
val BotonPeligro = RojoError
val BotonNeutral = GrisClaro

// Colores para texto
val TextoPrimario = GrisOscuro
val TextoSecundario = GrisClaro
val TextoInvertido = Blanco
val TextoDestacado = AzulPrincipal
val TextoExito = VerdeExito
val TextoError = RojoError