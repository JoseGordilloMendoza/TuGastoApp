package com.example.tugasto.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight
import com.example.tugasto.ui.theme.TuGastoBlueLight
import com.example.tugasto.ui.theme.TuGastoBg
import com.example.tugasto.ui.theme.TuGastoGray100
import com.example.tugasto.ui.theme.TuGastoGray200
import com.example.tugasto.ui.theme.TuGastoGray400
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGray700
import com.example.tugasto.ui.theme.TuGastoGray900

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YapeConfirmScreen(onBack: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TuGastoBg)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "TuGasto Pro",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TuGastoGray900
                    )
                    Spacer(Modifier.width(8.dp))
                    ProBadge()
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TuGastoGray900)
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    // Header with bolt icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(TuGastoBlueExtraLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = null,
                            tint = TuGastoBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "¡Yape Sincronizado Pro!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TuGastoGray900
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "DETECCIÓN AUTOMÁTICA ACTIVA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TuGastoGray500,
                            letterSpacing = 1.sp
                        )
                    )

                    Spacer(Modifier.height(28.dp))

                    // Merchant avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(TuGastoBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "DL",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TuGastoBlue
                            )
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "S/ 35.00",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TuGastoGray900
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tienda Don Lucho",
                        style = MaterialTheme.typography.titleMedium.copy(color = TuGastoGray700)
                    )

                    Spacer(Modifier.height(20.dp))

                    // Metadata row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, contentDescription = null, tint = TuGastoBlue, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "VÍA YAPE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = TuGastoBlue, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(20.dp)
                                .background(TuGastoGray200)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = TuGastoGray400, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Hace 2 minutos",
                                style = MaterialTheme.typography.labelMedium.copy(color = TuGastoGray500)
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    Divider(color = TuGastoGray200)
                    Spacer(Modifier.height(20.dp))

                    // Category selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TuGastoGray100)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = TuGastoGray500, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "CATEGORÍA",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TuGastoGray500, letterSpacing = 0.8.sp
                                )
                            )
                            Text(
                                "Hogar",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold, color = TuGastoGray900
                                )
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Editar",
                                style = MaterialTheme.typography.labelMedium.copy(color = TuGastoBlue)
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Default.Edit, contentDescription = null, tint = TuGastoBlue, modifier = Modifier.size(14.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Primary action button
                    Button(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Confirmar Gasto",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // Secondary button
                    OutlinedButton(
                        onClick = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TuGastoBlue)
                    ) {
                        Text(
                            "Ajustar Detalle",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Esta transacción se guardará en tu historial y se contabilizará en tu resumen mensual.",
                style = MaterialTheme.typography.bodySmall.copy(color = TuGastoGray400),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun ProBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(TuGastoBlueExtraLight)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            "PRO",
            style = MaterialTheme.typography.labelSmall.copy(
                color = TuGastoBlue,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        )
    }
}
