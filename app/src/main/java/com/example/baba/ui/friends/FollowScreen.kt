package com.example.baba.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.ui.theme.Blue1

@Composable
fun FollowScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showProfileScreen by remember { mutableStateOf(false) }
    val tabTitles = listOf("팔로잉 1", "팔로워 1")

    if (showProfileScreen) {
        FriendProfileScreen()
        return
    }

    Scaffold(
        topBar = {
            FollowTopBar(
                selectedTabIndex = selectedTabIndex,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = Blue1
                        )
                    },
                    containerColor = Color.Transparent
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    color = Blue1,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showProfileScreen = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_default_profile),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("hihihi", fontWeight = FontWeight.Medium)
                        Text("한땡", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun FollowTopBar(
    selectedTabIndex: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (selectedTabIndex == 0) "팔로잉 리스트 조회" else "팔로워 리스트 조회",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FollowScreenPreview() {
    MaterialTheme {
        FollowScreen(
            currentRoute = "friends",
            onNavigate = {},
            onBackClick = {}
        )
    }
}
