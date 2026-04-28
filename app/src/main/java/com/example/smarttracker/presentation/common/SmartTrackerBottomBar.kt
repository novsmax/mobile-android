package com.example.smarttracker.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smarttracker.R
import com.example.smarttracker.presentation.theme.ColorPrimary
import com.example.smarttracker.presentation.theme.ColorSecondary
import com.example.smarttracker.presentation.theme.geologicaFontFamily

/** Индексы вкладок нижней навигации. Порядок совпадает с WorkoutTab.ordinal. */
object AppTab {
    const val START    = 0
    const val WORKOUTS = 1
    const val MENU     = 2
}

/**
 * Единая нижняя навигационная панель приложения.
 *
 * Используется во всех экранах, где должна быть видна навигация —
 * как внутри WorkoutHomeScreen (вкладки), так и на отдельных
 * full-screen экранах вроде ProfileScreen.
 *
 * @param selectedIndex  Индекс активной вкладки — [AppTab.START], [AppTab.WORKOUTS] или [AppTab.MENU].
 * @param onTabSelected  Вызывается при нажатии на вкладку; принимает индекс выбранной вкладки.
 */
@Composable
fun SmartTrackerBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
) {
    Column {
        HorizontalDivider(color = ColorPrimary, thickness = 1.dp)
        NavigationBar(containerColor = Color.White) {
            listOf(
                Triple(AppTab.START,    R.drawable.ic_nav_start,    stringResource(R.string.tab_start)),
                Triple(AppTab.WORKOUTS, R.drawable.ic_nav_workouts, stringResource(R.string.tab_workouts)),
                Triple(AppTab.MENU,     R.drawable.ic_nav_menu,     stringResource(R.string.tab_menu)),
            ).forEach { (index, iconRes, label) ->
                NavigationBarItem(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    icon = {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = if (selectedIndex == index) ColorSecondary else Color.Transparent,
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                painter = painterResource(iconRes),
                                contentDescription = label,
                                tint = ColorPrimary,
                                modifier = Modifier.size(38.dp),
                            )
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            fontFamily = geologicaFontFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 14.sp,
                            color = ColorPrimary,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                    ),
                )
            }
        }
    }
}
