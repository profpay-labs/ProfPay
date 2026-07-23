package com.profpay.wallet.ui.components.custom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Тип Snackbar уведомления
 */
enum class SnackbarType {
    ERROR,
    SUCCESS,
    WARNING,
    INFO
}

/**
 * Данные для отображения Snackbar
 */
data class SnackbarData(
    val message: String,
    val type: SnackbarType = SnackbarType.ERROR,
    val actionLabel: String? = null,
    val durationMillis: Long = 4000L,
)

/**
 * Кастомный Snackbar с иконкой и стилизацией по типу
 */
@Composable
fun AppSnackbarHost(
    snackbarData: SnackbarData?,
    onDismiss: () -> Unit,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(snackbarData) {
        if (snackbarData != null) {
            isVisible = true
            delay(snackbarData.durationMillis.milliseconds)
            isVisible = false
            delay(300.milliseconds) // Ждём окончания анимации
            onDismiss()
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = isVisible && snackbarData != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        ) {
            snackbarData?.let { data ->
                StyledSnackbar(
                    data = data,
                    onAction = {
                        onAction?.invoke()
                        isVisible = false
                    },
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun StyledSnackbar(
    data: SnackbarData,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (containerColor, contentColor, icon) = when (data.type) {
        SnackbarType.ERROR -> Triple(
            Color(0xFFD32F2F),
            Color.White,
            Icons.Filled.Warning
        )
        SnackbarType.SUCCESS -> Triple(
            Color(0xFF388E3C),
            Color.White,
            Icons.Filled.Warning // Можно заменить на CheckCircle
        )
        SnackbarType.WARNING -> Triple(
            Color(0xFFF57C00),
            Color.White,
            Icons.Filled.Warning
        )
        SnackbarType.INFO -> Triple(
            MaterialTheme.colorScheme.inverseSurface,
            MaterialTheme.colorScheme.inverseOnSurface,
            Icons.Filled.Warning // Можно заменить на Info
        )
    }

    Snackbar(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        action = data.actionLabel?.let {
            {
                TextButton(onClick = onAction) {
                    Text(
                        text = it,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = contentColor,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = data.message,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
