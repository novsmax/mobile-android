package com.example.smarttracker.presentation.menu.sensors

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.smarttracker.R
import com.example.smarttracker.presentation.theme.ColorPrimary
import com.example.smarttracker.presentation.theme.geologicaFontFamily

/**
 * Оверлей «Датчики» поверх экрана тренировки (тап по HR-бейджу).
 *
 * НЕ навигация намеренно: navController.navigate() с экрана с живой картой
 * разрушает MapView, и неотменяемые аниматоры LocationComponent роняют
 * процесс IllegalStateException-ом изнутри MapLibre (нюанс 36; три
 * тайминговые митигации не закрыли гонку). Оверлей рисуется ПОВЕРХ экрана —
 * карта остаётся в композиции, разрушения нет. Тот же паттерн, что оверлей
 * итогов тренировки (SummaryOverlay).
 *
 * ViewModel скоупится на backstack-entry Home — переживает закрытие оверлея,
 * поэтому остановка скана при закрытии делается явно (DisposableEffect →
 * [SensorsViewModel.onScanStopRequested]); соединение с датчиком при этом
 * НЕ рвётся (им владеет HrmManager).
 *
 * @param onClose закрытие оверлея (стрелка «назад» и системный Back)
 */
@Composable
fun SensorsOverlay(onClose: () -> Unit) {
    val viewModel: SensorsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Системный Back закрывает оверлей, а не экран под ним
    BackHandler(onBack = onClose)

    // Скан не должен жить после закрытия оверлея: VM не умирает
    // (скоуп — backstack-entry Home), onCleared не сработает
    DisposableEffect(Unit) {
        onDispose { viewModel.onScanStopRequested() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        // Шапка в стиле CenterAlignedTopAppBar остальных экранов:
        // стрелка «назад» слева, заголовок по центру.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.CenterStart),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Text(
                text = stringResource(R.string.sensors_title),
                fontFamily = geologicaFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        SensorsScreenContent(
            state = state,
            onPermissionsGranted = viewModel::onPermissionsGranted,
            onPermissionsDenied = viewModel::onPermissionsDenied,
            onScanClick = viewModel::onScanClick,
            onDeviceClick = viewModel::onDeviceClick,
            onConnectSavedClick = viewModel::onConnectSavedClick,
            onForgetClick = viewModel::onForgetClick,
        )
    }
}
