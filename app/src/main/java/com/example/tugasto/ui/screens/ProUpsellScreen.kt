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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tugasto.ui.theme.TuGastoBg
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGray700
import com.example.tugasto.ui.theme.TuGastoGray900
import com.example.tugasto.ui.theme.TuGastoGreen

@Composable
fun ProUpsellScreen(onSubscribeClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(TuGastoBlue.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = TuGastoBlue,
                modifier = Modifier.size(50.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Desbloquea TuGasto PRO",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            "Toma el control absoluto de tus finanzas con herramientas impulsadas por Inteligencia Artificial y automatización.",
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BenefitItem("Predicciones de IA con Groq")
            BenefitItem("Sincronización automática con Yape/Plin")
            BenefitItem("Seguimiento de Metas y Fondos de Emergencia")
            BenefitItem("Exportación de reportes avanzados")
        }

        Spacer(Modifier.weight(1f))

        Text(
            "S/ 9.90 / mes",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface
            )
        )
        Text(
            "Cancela en cualquier momento",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSubscribeClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TuGastoBlue),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Simular Suscripción", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(TuGastoGreen.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = TuGastoGreen, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
