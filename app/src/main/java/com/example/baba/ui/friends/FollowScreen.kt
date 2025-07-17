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
import com.example.baba.data.network.SessionManager
import com.example.baba.ui.theme.Blue1
import kotlinx.coroutines.launch
import android.util.Log

@Composable
fun FollowScreen(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    var showProfileScreen by remember { mutableStateOf(false) }
    var selectedMember by remember { mutableStateOf<MemberInfoResponse?>(null) }
    var followingUsernameList by remember { mutableStateOf<List<String>>(emptyList()) }
    var followerUsernameList by remember { mutableStateOf<List<String>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<MemberInfoResponse>>(emptyList()) }
    var followerList by remember { mutableStateOf<List<MemberInfoResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentUsername by remember { mutableStateOf("") }

    // 새로고침 트리거 - 이 값이 변경되면 데이터를 다시 로드
    var refreshTrigger by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()

    // 사용자 정보 가져오기
    LaunchedEffect(Unit) {
        try {
            Log.d("FollowScreen", "사용자 정보 조회 시작")

            // 먼저 SessionManager에서 확인
            val sessionUsername = SessionManager.username
            Log.d("FollowScreen", "SessionManager username: $sessionUsername")
            Log.d("FollowScreen", "SessionManager userId: ${SessionManager.userId}")
            Log.d("FollowScreen", "SessionManager userName: ${SessionManager.userName}")

            if (!sessionUsername.isNullOrEmpty()) {
                currentUsername = sessionUsername
                Log.d("FollowScreen", "SessionManager에서 username 설정: $currentUsername")
            } else {
                // SessionManager에 없으면 API 호출
                Log.d("FollowScreen", "SessionManager에 username이 없어서 API 호출")
                val memberResponse = RetrofitInstance.memberApi.getMyInfo()
                if (memberResponse.isSuccessful && memberResponse.body() != null) {
                    currentUsername = memberResponse.body()!!.username
                    Log.d("FollowScreen", "API에서 가져온 username: $currentUsername")
                } else {
                    Log.e("FollowScreen", "사용자 정보 조회 실패: ${memberResponse.code()}")
                    Log.e("FollowScreen", "응답 body: ${memberResponse.errorBody()?.string()}")
                    errorMessage = "사용자 정보를 가져올 수 없습니다"
                }
            }
        } catch (e: Exception) {
            Log.e("FollowScreen", "사용자 정보 조회 오류: ${e.message}", e)
            errorMessage = "네트워크 오류가 발생했습니다"
        }
    }

    // 팔로워/팔로잉 목록 로드 - refreshTrigger 값이 변경될 때마다 실행
    LaunchedEffect(currentUsername, refreshTrigger) {
        if (currentUsername.isNotEmpty()) {
            isLoading = true
            errorMessage = null

            Log.d("FollowScreen", "팔로잉/팔로워 목록 조회 시작 - username: $currentUsername, refresh: $refreshTrigger")

            try {
                // 팔로잉 목록 조회 (username 리스트)
                Log.d("FollowScreen", "팔로잉 목록 API 호출 중...")
                val followingResponse = RetrofitInstance.friendsApi.getFollowingList(currentUsername)
                followingUsernameList = followingResponse
                Log.d("FollowScreen", "팔로잉 username 목록 조회 성공: ${followingUsernameList.size}명")

                // 각 username으로 MemberInfo 조회
                val followingMembers = mutableListOf<MemberInfoResponse>()
                for (username in followingUsernameList) {
                    try {
                        // 하드코딩으로 username을 name으로 매핑
                        val displayName = when (username) {
                            "user1" -> "김민지"
                            "user2" -> "정윤희"
                            "user3" -> "양서영"
                            "admin" -> "관리자"
                            else -> username
                        }

                        val memberInfo = MemberInfoResponse(
                            id = username.hashCode().toLong(),
                            username = username,
                            name = displayName,
                            profileImageUrl = null,
                            bio = "안녕하세요!",
                            preference = null,
                            link = null
                        )
                        followingMembers.add(memberInfo)
                    } catch (e: Exception) {
                        Log.e("FollowScreen", "멤버 정보 조회 실패: $username, ${e.message}")
                    }
                }
                followingList = followingMembers

                // 팔로워 목록 조회 (username 리스트)
                Log.d("FollowScreen", "팔로워 목록 API 호출 중...")
                val followerResponse = RetrofitInstance.friendsApi.getFollowerList(currentUsername)
                followerUsernameList = followerResponse
                Log.d("FollowScreen", "팔로워 username 목록 조회 성공: ${followerUsernameList.size}명")

                // 각 username으로 MemberInfo 조회
                val followerMembers = mutableListOf<MemberInfoResponse>()
                for (username in followerUsernameList) {
                    try {
                        // 하드코딩으로 username을 name으로 매핑
                        val displayName = when (username) {
                            "user1" -> "김민지"
                            "user2" -> "정윤희"
                            "user3" -> "양서영"
                            "admin" -> "관리자"
                            else -> username
                        }

                        val memberInfo = MemberInfoResponse(
                            id = username.hashCode().toLong(),
                            username = username,
                            name = displayName,
                            profileImageUrl = null,
                            bio = "안녕하세요!",
                            preference = null,
                            link = null
                        )
                        followerMembers.add(memberInfo)
                    } catch (e: Exception) {
                        Log.e("FollowScreen", "멤버 정보 조회 실패: $username, ${e.message}")
                    }
                }
                followerList = followerMembers

                Log.d("FollowScreen", "최종 팔로잉 리스트: ${followingList.size}명")
                Log.d("FollowScreen", "최종 팔로워 리스트: ${followerList.size}명")

            } catch (e: Exception) {
                Log.e("FollowScreen", "팔로잉/팔로워 목록 조회 오류: ${e.message}", e)
                errorMessage = "목록을 불러오는데 실패했습니다: ${e.message}"
            } finally {
                isLoading = false
                Log.d("FollowScreen", "로딩 완료 - isLoading: false")
            }
        } else {
            Log.w("FollowScreen", "currentUsername이 비어있어서 API 호출 안함")
        }
    }

    // 프로필 화면에서 돌아올 때 새로고침 처리
    LaunchedEffect(showProfileScreen) {
        if (!showProfileScreen && selectedMember != null) {
            // 프로필 화면에서 돌아왔을 때 새로고침
            Log.d("FollowScreen", "프로필 화면에서 돌아와서 새로고침 실행")
            refreshTrigger++
            selectedMember = null
        }
    }

    if (showProfileScreen && selectedMember != null) {
        FriendProfileScreen(
            targetMember = selectedMember,
            onBackClick = {
                showProfileScreen = false
                // selectedMember는 LaunchedEffect에서 처리
            }
        )
        return
    }

    // 탭 타이틀 동적 생성
    val tabTitles = listOf(
        "팔로잉 ${followingList.size}",
        "팔로워 ${followerList.size}"
    )

    Scaffold(
        topBar = {
            FollowTopBar(
                selectedTabIndex = selectedTabIndex,
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 탭 행
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

            // 컨텐츠 영역
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "목록을 불러오는 중...",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    errorMessage != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = errorMessage!!,
                                    fontSize = 16.sp,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        // 재시도 - refreshTrigger 증가로 새로고침
                                        refreshTrigger++
                                    }
                                ) {
                                    Text("다시 시도")
                                }
                            }
                        }
                    }

                    else -> {
                        val currentList = if (selectedTabIndex == 0) followingList else followerList
                        Log.d("FollowScreen", "UI 렌더링 - selectedTabIndex: $selectedTabIndex, currentList.size: ${currentList.size}")

                        if (currentList.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (selectedTabIndex == 0) "팔로잉한 사용자가 없습니다" else "팔로워가 없습니다",
                                    fontSize = 16.sp,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(vertical = 8.dp)
                            ) {
                                items(currentList) { member ->
                                    Log.d("FollowScreen", "렌더링 중인 멤버: ${member.name}")
                                    FriendListItem(
                                        member = member,
                                        onProfileClick = {
                                            Log.d("FollowScreen", "프로필 클릭: ${member.name}")
                                            selectedMember = member
                                            showProfileScreen = true
                                        }
                                    )
                                }
                            }
                        }
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

        Text(
            text = "팔로우",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
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
        // 프로필 이미지
        Box(
            modifier = Modifier.size(48.dp)
        ) {
            if (member.profileImageUrl != null) {
                Image(
                    painter = rememberAsyncImagePainter(member.profileImageUrl),
                    contentDescription = "프로필 이미지",
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.ic_default_profile),
                    contentDescription = "기본 프로필",
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = member.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )

            // bio가 있으면 표시
            if (!member.bio.isNullOrEmpty()) {
                Text(
                    text = member.bio,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
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