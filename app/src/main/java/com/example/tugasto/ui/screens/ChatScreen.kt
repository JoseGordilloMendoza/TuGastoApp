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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tugasto.ui.theme.TuGastoBg
import com.example.tugasto.ui.theme.TuGastoBlue
import com.example.tugasto.ui.theme.TuGastoBlueExtraLight
import com.example.tugasto.ui.theme.TuGastoGray100
import com.example.tugasto.ui.theme.TuGastoGray400
import com.example.tugasto.ui.theme.TuGastoGray200
import com.example.tugasto.ui.theme.TuGastoGray500
import com.example.tugasto.ui.theme.TuGastoGray900
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val time: String,
    val confirmLabel: String = ""
)

private val initialMessages = listOf(
    ChatMessage("Pasaje 2.50 S/", isUser = true, time = "10:28 AM"),
    ChatMessage("✓ Registrado\nPasaje · Transporte · S/ 2.50", isUser = false, time = "10:28 AM", confirmLabel = "Transporte"),
    ChatMessage("Menú 18.00 S/", isUser = true, time = "1:15 PM"),
    ChatMessage("✓ Registrado\nMenú · Alimentación · S/ 18.00", isUser = false, time = "1:15 PM", confirmLabel = "Alimentación"),
    ChatMessage("Cena 45.00 S/", isUser = true, time = "8:45 PM"),
    ChatMessage("✓ Registrado\nCena · Alimentación · S/ 45.00", isUser = false, time = "8:45 PM", confirmLabel = "Alimentación"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToYape: () -> Unit = {},
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = "Asistente TuGasto",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Spacer(Modifier.height(12.dp))
                DateIndicator("HOY")
                Spacer(Modifier.height(12.dp))
            }
            items(messages) { msg ->
                if (msg.isUser) {
                    UserBubble(msg)
                } else {
                    AssistantBubble(msg)
                }
                Spacer(Modifier.height(2.dp))
            }
            item { Spacer(Modifier.height(8.dp)) }
        }

        MessageInputBar(
            text = inputText,
            onTextChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                }
            }
        )
    }
}

@Composable
private fun DateIndicator(label: String) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            text = label,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp
            )
        )
    }
}

@Composable
private fun UserBubble(msg: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 48.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp))
                    .background(TuGastoBlue)
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Text(
                    text = msg.text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = msg.time,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AssistantBubble(msg: ChatMessage) {
    val isConfirmed = msg.confirmLabel.isNotBlank()

    Row(
        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar TuGasto
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(TuGastoBlue),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "TG",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp
                )
            )
        }
        Spacer(Modifier.width(8.dp))
        Column {
            Box(
                modifier = Modifier
                    .widthIn(max = 260.dp)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
                    .background(
                        if (isConfirmed) TuGastoBlueExtraLight
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 16.dp, vertical = 11.dp)
            ) {
                Column {
                    Text(
                        text = msg.text,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isConfirmed) {
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TuGastoBlue.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "✓ ${msg.confirmLabel}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = TuGastoBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(3.dp))
            Text(
                text = msg.time,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MessageInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Escribe tu gasto... ej: café 8 S/",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                shape = RoundedCornerShape(24.dp),
                maxLines = 3,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(Modifier.width(10.dp))
            IconButton(
                onClick = onSend,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar")
            }
        }
    }
}
