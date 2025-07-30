package com.example.buzondesugerenciascojema.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buzondesugerenciascojema.data.RankingEntry

@Composable
fun RankingCard(
    rankings: List<RankingEntry>,
    titulo: String = "Ranking Global",
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6C63FF),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF6C63FF)
                    )
                }
            } else if (rankings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay puntuaciones aún",
                        fontSize = 14.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                rankings.forEachIndexed { index, ranking ->
                    RankingItem(
                        posicion = index + 1,
                        ranking = ranking,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < rankings.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RankingItem(
    posicion: Int,
    ranking: RankingEntry,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when (posicion) {
        1 -> Color(0xFFFFD700) // Oro
        2 -> Color(0xFFC0C0C0) // Plata
        3 -> Color(0xFFCD7F32) // Bronce
        else -> Color(0xFFF5F5F5)
    }
    
    val textColor = when (posicion) {
        1 -> Color(0xFF8B4513) // Marrón oscuro para oro
        2 -> Color(0xFF696969) // Gris oscuro para plata
        3 -> Color(0xFF8B4513) // Marrón oscuro para bronce
        else -> Color.Black
    }
    
    Row(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número de posición
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    when (posicion) {
                        1 -> Color(0xFFFFD700)
                        2 -> Color(0xFFC0C0C0)
                        3 -> Color(0xFFCD7F32)
                        else -> Color(0xFF6C63FF)
                    },
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = posicion.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = when (posicion) {
                    1, 2, 3 -> Color.White
                    else -> Color.White
                }
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Información del jugador
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = ranking.nombreUsuario ?: "Anónimo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                maxLines = 1
            )
            Text(
                text = "${ranking.puntuacion} puntos",
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f)
            )
        }
        
        // Medalla para los primeros 3 lugares
        if (posicion <= 3) {
            val medallaEmoji = when (posicion) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> ""
            }
            Text(
                text = medallaEmoji,
                fontSize = 20.sp
            )
        }
    }
} 