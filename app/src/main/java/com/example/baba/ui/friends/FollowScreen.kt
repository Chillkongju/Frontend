package com.example.baba.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.baba.data.friends.FriendResponse
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.ui.theme.Blue1
import com.example.baba.ui.theme.CoolGray500
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var followingList by remember { mutableStateOf<List<FriendResponse>>(emptyList()) }
    var followerList by remember { mutableStateOf<List<FriendResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // 탭 타이틀 동적 생성
    val tabTitles = listOf(
        "팔로잉 ${followingList.size}",
        "팔로워 ${followerList.size}"
    )

    // API 호출
    LaunchedEffect(selectedTabIndex) {
        coroutineScope.launch {
            isLoading = true
            try {
                if (selectedTabIndex == 0) {
                    followingList = RetrofitInstance.friendsApi.getFollowingList()
                } else {
                    followerList = RetrofitInstance.friendsApi.getFollowerList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Box {} },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Blue1
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                color = if (selectedTabIndex == index) Blue1 else CoolGray500,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val list = if (selectedTabIndex == 0) followingList else followerList
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(list) { friend ->
                        FriendListItem(friend = friend)
                    }
                }
            }
        }
    }
}

@Composable
fun FriendListItem(friend: FriendResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(friend.profileImageUrl ?: "https://default.url/to/profile.png"),
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(friend.nickname, fontWeight = FontWeight.Medium)
            Text(friend.username, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FollowScreenPreview() {
    MaterialTheme {
        FollowScreen(
            currentRoute = "friends",   // 현재 선택된 하단 탭
            onNavigate = {},
            onBackClick = {}
        )
    }
}