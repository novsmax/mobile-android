package com.example.smarttracker.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.smarttracker.presentation.theme.ColorBackground
import com.example.smarttracker.presentation.theme.ColorPlaceholder
import com.example.smarttracker.presentation.theme.ColorPrimary
import com.example.smarttracker.presentation.theme.geologicaFontFamily

/**
 * Экран профиля пользователя.
 * Для MVP — заглушка. Реализация будет добавлена позже.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Профиль",
                        fontFamily = geologicaFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 20.sp,
                        color = ColorPrimary,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = ColorPrimary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ColorBackground,
                ),
            )
        },
        containerColor = ColorBackground,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Профиль\n(скоро)",
                fontFamily = geologicaFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = 18.sp,
                color = ColorPlaceholder,
                textAlign = TextAlign.Center,
            )
        }
    }
}
