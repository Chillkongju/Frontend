package com.example.baba.ui.common

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.ui.theme.Blue3
import com.example.baba.ui.theme.Blue4
import com.example.baba.ui.theme.CoolGray200
import com.example.baba.ui.theme.CoolGray500
import com.example.baba.ui.theme.White

sealed class Screen(val route: String, val iconRes: Int, val labelRes: Int) {
    object Home : Screen("home", R.drawable.ic_nav_home, R.string.nav_home)
    object Record : Screen("record", R.drawable.ic_nav_record, R.string.nav_record)
    object Recommendation : Screen("recommendation", R.drawable.ic_nav_recommendation, R.string.nav_recommend)
    object Friends : Screen("friends", R.drawable.ic_nav_friend, R.string.nav_friends)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String,
    onItemClick: (String) -> Unit
) {
    val items = listOf(
        Screen.Home,
        Screen.Record,
        Screen.Recommendation,
        Screen.Friends
    )

    NavigationBar(
        modifier = Modifier
            .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))  // 윗모서리 반경
            .border(
                width = 1.dp,
                color = CoolGray200,
                shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
            ),
        containerColor = White,
        contentColor = CoolGray500
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconRes),
                        contentDescription = stringResource(id = screen.labelRes),
                        tint = if (isSelected) Blue3 else CoolGray500
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = screen.labelRes),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) Blue3 else CoolGray500
                    )
                },
                selected = isSelected,
                onClick = { onItemClick(screen.route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = Color.Transparent,
                    selectedIconColor = Blue3,
                    selectedTextColor = Blue3,
                    unselectedIconColor = CoolGray500,
                    unselectedTextColor = CoolGray500
                )
            )
        }
    }
}