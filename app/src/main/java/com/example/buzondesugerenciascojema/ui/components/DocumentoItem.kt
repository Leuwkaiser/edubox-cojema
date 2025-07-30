package com.example.buzondesugerenciascojema.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.buzondesugerenciascojema.R
import com.example.buzondesugerenciascojema.model.Documento
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentoItem(
    documento: Documento,
    onDocumentoClick: () -> Unit,
    onEliminarClick: (() -> Unit)? = null,
    mostrarAccionesAdmin: Boolean = false
) {
    val context = LocalContext.current
    var mostrarMenu by remember { mutableStateOf(false) }
    var mostrarDescripcion by remember { mutableStateOf(false) }
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val fechaSubida = dateFormat.format(Date(documento.fechaSubida))

    Card(
        onClick = onDocumentoClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = documento.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        // Removemos la descripción de aquí para que no ocupe espacio
                    }
                    
                    // Botón de 3 puntos para todos los usuarios
                    IconButton(
                        onClick = { mostrarMenu = true },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Más opciones")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${documento.asignatura} - Grado ${documento.grado}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Subido por: ${documento.subidoPor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = fechaSubida,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = documento.tipoArchivo.ifBlank { "ENLACE" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                // Mostrar información del enlace
                if (documento.esEnlaceExterno) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.globo),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = documento.nombreArchivo,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                if (!mostrarAccionesAdmin) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documento.url))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Abrir enlace")
                    }
                }
            }
            
            // Menú desplegable con opciones
            DropdownMenu(
                expanded = mostrarMenu,
                onDismissRequest = { mostrarMenu = false }
            ) {
                // Opción para ver descripción
                DropdownMenuItem(
                    text = { Text("Ver descripción") },
                    onClick = {
                        mostrarMenu = false
                        mostrarDescripcion = true
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Info, contentDescription = null)
                    }
                )
                
                // Opción para abrir enlace
                DropdownMenuItem(
                    text = { Text("Abrir enlace") },
                    onClick = {
                        mostrarMenu = false
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(documento.url))
                        context.startActivity(intent)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.ExitToApp, contentDescription = null)
                    }
                )
                
                // Opción de eliminar solo para admins
                if (mostrarAccionesAdmin && onEliminarClick != null) {
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            mostrarMenu = false
                            onEliminarClick()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        }
                    )
                }
            }
        }
        
        // Diálogo para mostrar la descripción
        if (mostrarDescripcion) {
            AlertDialog(
                onDismissRequest = { mostrarDescripcion = false },
                title = { Text("Descripción del documento") },
                text = { Text(documento.descripcion) },
                confirmButton = {
                    TextButton(onClick = { mostrarDescripcion = false }) {
                        Text("Cerrar")
                    }
                }
            )
        }
    }
} 