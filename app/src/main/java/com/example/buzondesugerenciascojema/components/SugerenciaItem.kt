package com.example.buzondesugerenciascojema.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.buzondesugerenciascojema.model.Sugerencia
import com.example.buzondesugerenciascojema.ui.theme.*
import com.example.buzondesugerenciascojema.R

@Composable
fun SugerenciaItem(
    sugerencia: Sugerencia,
    onVotar: (Boolean) -> Unit,
    onEliminar: () -> Unit,
    esAdmin: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AzulMuyClaro
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = sugerencia.contenido,
                style = MaterialTheme.typography.bodyLarge,
                color = AzulOscuro
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onVotar(true) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.like),
                            contentDescription = "Me gusta",
                            tint = AzulPrincipal
                        )
                    }
                    Text(
                        text = sugerencia.votosPositivos.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulOscuro
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = { onVotar(false) }) {
                        Icon(
                            painter = painterResource(id = R.drawable.dislike),
                            contentDescription = "No me gusta",
                            tint = RojoError
                        )
                    }
                    Text(
                        text = sugerencia.votosNegativos.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = AzulOscuro
                    )
                }

                if (esAdmin) {
                    TextButton(
                        onClick = onEliminar,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = RojoError
                        )
                    ) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}