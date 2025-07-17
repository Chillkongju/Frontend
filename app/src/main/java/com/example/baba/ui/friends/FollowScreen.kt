package com.example.baba.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import coil.compose.rememberAsyncImagePainter
import com.example.baba.R
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.ui.theme.Blue1

@Composable
fun FollowScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var followingList by remember { mutableStateOf<List<MemberInfoResponse>>(emptyList()) }
    var followerList by remember { mutableStateOf<List<MemberInfoResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var currentUsername by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val memberResponse = RetrofitInstance.memberApi.getMyInfo()
            if (memberResponse.isSuccessful && memberResponse.body() != null) {
                currentUsername = memberResponse.body()!!.username
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 탭 타이틀 동적 생성
    val tabTitles = listOf(
        "팔로잉 ${followingList.size}",
        "팔로워 ${followerList.size}"
    )

    // API 호출
    LaunchedEffect(currentUsername) {
        if (currentUsername.isNotEmpty()) {
            isLoading = true
            try {
                val followingResponse = RetrofitInstance.friendsApi.getFollowingList(currentUsername)
                followingList = followingResponse

                val followerResponse = RetrofitInstance.friendsApi.getFollowerList(currentUsername)
                followerList = followerResponse
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

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

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                val list = if (selectedTabIndex == 0) followingList else followerList
                items(list) { member ->
                    FriendListItem(
                        member = member,
                        onProfileClick = { showProfileScreen = true }
                    )
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
    }
}

@Composable
fun FriendListItem(
    member: MemberInfoResponse,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProfileClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = if (member.profileImageUrl != null) {
                rememberAsyncImagePainter(member.profileImageUrl)
            } else {
                painterResource(id = R.drawable.ic_default_profile)
            },
            contentDescription = null,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = member.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Text(
                text = member.username,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
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