package com.example.baba.ui.friends

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.ui.theme.*
import kotlinx.coroutines.launch

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    targetMember: MemberInfoResponse? = null,
    onBackClick: () -> Unit = {}
) {
    var isFollowing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isCheckingFollowStatus by remember { mutableStateOf(targetMember != null) }
    var followerCount by remember { mutableStateOf(0) }
    var currentUserInfo by remember { mutableStateOf<MemberInfoResponse?>(null) }

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    fun getUserDisplayName(username: String): String {
        return when (username) {
            "user1" -> "김민지"
            "user2" -> "정윤희"
            "admin" -> "관리자"
            else -> username
        }
    }

    LaunchedEffect(Unit) {
        try {
            Log.d("FriendProfile", "사용자 정보 조회 시작")

            val sessionUsername = SessionManager.username
            val sessionUserId = SessionManager.userId
            val sessionUserName = SessionManager.userName

            Log.d("FriendProfile", "SessionManager 정보:")
            Log.d("FriendProfile", "  - username: $sessionUsername")
            Log.d("FriendProfile", "  - userId: $sessionUserId")
            Log.d("FriendProfile", "  - userName: $sessionUserName")

            if (!sessionUsername.isNullOrEmpty() && sessionUserId != null) {
                currentUserInfo = MemberInfoResponse(
                    id = sessionUserId,
                    username = sessionUsername,
                    name = sessionUserName ?: getUserDisplayName(sessionUsername),
                    profileImageUrl = null,
                    bio = null,
                    preference = null,
                    link = null
                )
                Log.d("FriendProfile", "SessionManager에서 사용자 정보 설정 완료: ${currentUserInfo?.username}")
            } else {
                Log.d("FriendProfile", "SessionManager에 username이 없어서 API 호출 시도")

                try {
                    val response = RetrofitInstance.memberApi.getMyInfo()
                    if (response.isSuccessful && response.body() != null) {
                        currentUserInfo = response.body()
                        Log.d("FriendProfile", "API에서 현재 사용자 조회 성공: ${currentUserInfo?.username}")
                    } else {
                        Log.e("FriendProfile", "API 사용자 정보 조회 실패: ${response.code()}")
                        Log.e("FriendProfile", "에러 내용: ${response.errorBody()?.string()}")

                        // API 실패 시 기본값 설정
                        currentUserInfo = MemberInfoResponse(
                            id = 1L,
                            username = "user1", // 기본값
                            name = "김민지",
                            profileImageUrl = null,
                            bio = null,
                            preference = null,
                            link = null
                        )
                        Log.d("FriendProfile", "기본값으로 currentUserInfo 설정")
                    }
                } catch (e: Exception) {
                    Log.e("FriendProfile", "API 호출 예외: ${e.message}")
                    // 예외 발생 시에도 기본값 설정
                    currentUserInfo = MemberInfoResponse(
                        id = 1L,
                        username = "user1", // 기본값
                        name = "김민지",
                        profileImageUrl = null,
                        bio = null,
                        preference = null,
                        link = null
                    )
                    Log.d("FriendProfile", "예외 발생으로 기본값 설정")
                }
            }
        } catch (e: Exception) {
            Log.e("FriendProfile", "전체 사용자 정보 조회 오류: ${e.message}")
            currentUserInfo = MemberInfoResponse(
                id = 1L,
                username = "user1",
                name = "김민지",
                profileImageUrl = null,
                bio = null,
                preference = null,
                link = null
            )
        }
    }

    // 팔로워 수 조회
    LaunchedEffect(targetMember) {
        targetMember?.let { member ->
            try {
                Log.d("FriendProfile", "타겟 멤버 정보: username=${member.username}, name=${member.name}")

                val followerUsernameList = RetrofitInstance.friendsApi.getFollowerList(member.username)
                followerCount = followerUsernameList.size
                Log.d("FriendProfile", "${member.username}의 팔로워 수: $followerCount")
            } catch (e: Exception) {
                Log.e("FriendProfile", "팔로워 수 조회 실패: ${e.message}")
            }
        }
    }

    // 현재 팔로우 상태 확인
    LaunchedEffect(currentUserInfo, targetMember) {
        Log.d("FriendProfile", "팔로우 상태 확인 LaunchedEffect 시작")
        Log.d("FriendProfile", "currentUserInfo: ${currentUserInfo?.username}")
        Log.d("FriendProfile", "targetMember: ${targetMember?.username}")

        if (currentUserInfo != null && targetMember != null) {
            isCheckingFollowStatus = true
            try {
                Log.d("FriendProfile", "팔로우 상태 확인 시작 - from: ${currentUserInfo!!.username}, to: ${targetMember.username}")
                val followingUsernameList = RetrofitInstance.friendsApi.getFollowingList(currentUserInfo!!.username)

                Log.d("FriendProfile", "팔로잉 목록: $followingUsernameList")
                Log.d("FriendProfile", "타겟 사용자 username: ${targetMember.username}")

                isFollowing = followingUsernameList.contains(targetMember.username)
                Log.d("FriendProfile", "최종 팔로우 상태: $isFollowing")

            } catch (e: Exception) {
                Log.e("FriendProfile", "팔로우 상태 확인 실패: ${e.message}")
                isFollowing = false
            } finally {
                isCheckingFollowStatus = false
                Log.d("FriendProfile", "팔로우 상태 확인 완료 - isCheckingFollowStatus = false")
            }
        } else {
            Log.d("FriendProfile", "필요한 정보가 없어서 로딩 상태 해제")
            isCheckingFollowStatus = false
        }
    }

    val displayName = targetMember?.let { getUserDisplayName(it.username) } ?: "사용자"
    val followText = if (isFollowing) "팔로잉" else "팔로우"

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text ="$displayName's",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                        ) },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 8.dp)
                .padding(bottom = 30.dp)
        ) {
            item {
                if (isCheckingFollowStatus) {
                    // 팔로우 상태 확인 중 로딩
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "프로필 로딩 중...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    FriendProfileCard(
                        member = targetMember,
                        followerCount = followerCount,
                        followText = followText,
                        isFollowing = isFollowing,
                        isLoading = isLoading,
                        getUserDisplayName = ::getUserDisplayName,
                        onFollowClick = {
                            if (currentUserInfo == null || targetMember == null) {
                                Toast.makeText(context, "사용자 정보를 불러올 수 없습니다", Toast.LENGTH_SHORT).show()
                                return@FriendProfileCard
                            }

                            if (isFollowing) {
                                showBottomSheet = true
                            } else {
                                // 팔로우 처리
                                scope.launch {
                                    isLoading = true
                                    try {
                                        Log.d("FriendProfile", "팔로우 요청 - from: ${currentUserInfo!!.username}, to: ${targetMember.username}")
                                        val response = RetrofitInstance.friendsApi.followUser(
                                            fromUsername = currentUserInfo!!.username,
                                            toUsername = targetMember.username
                                        )

                                        if (response.isSuccessful) {
                                            isFollowing = true
                                            followerCount += 1
                                            Toast.makeText(context, "팔로우했습니다", Toast.LENGTH_SHORT).show()
                                        } else {
                                            val errorMessage = response.errorBody()?.string() ?: "팔로우 실패"
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    )
                }
            }

            item {
                FilterAndCategorySection(
                    selectedTabIndex = 0,
                    onTabClick = { /* TODO */ },
                    onPeriodClick = { /* TODO */ },
                    selectedCategory = 0,
                    onCategoryClick = { /* TODO */ }
                )
            }

            item {
                RecordList()
            }
        }
    }

    // 언팔로우 확인 바텀시트
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "언팔로우 하시겠습니까?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            showBottomSheet = false

                            try {
                                Log.d("FriendProfile", "언팔로우 요청 - from: ${currentUserInfo!!.username}, to: ${targetMember!!.username}")
                                val response = RetrofitInstance.friendsApi.unfollowUser(
                                    fromUsername = currentUserInfo!!.username,
                                    toUsername = targetMember!!.username
                                )

                                if (response.isSuccessful) {
                                    isFollowing = false
                                    followerCount = maxOf(0, followerCount - 1)
                                    Toast.makeText(context, "언팔로우했습니다", Toast.LENGTH_SHORT).show()
                                    Log.d("FriendProfile", "${targetMember.username} 언팔로우 성공")
                                } else {
                                    val errorMessage = response.errorBody()?.string() ?: "언팔로우 실패"
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                    Log.e("FriendProfile", "언팔로우 실패: ${response.code()}, $errorMessage")
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                                Log.e("FriendProfile", "언팔로우 오류: ${e.message}")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("언팔로우")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text("취소", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun FriendProfileCard(
    member: MemberInfoResponse?,
    followerCount: Int,
    followText: String,
    isFollowing: Boolean,
    isLoading: Boolean,
    getUserDisplayName: (String) -> String,
    onFollowClick: () -> Unit
) {
    val displayName = member?.let { getUserDisplayName(it.username) } ?: "사용자"

    Log.d("FriendProfile", "FriendProfileCard 렌더링:")
    Log.d("FriendProfile", "  - member?.username: ${member?.username}")
    Log.d("FriendProfile", "  - member?.name: ${member?.name}")
    Log.d("FriendProfile", "  - displayName: $displayName")
    Log.d("FriendProfile", "  - followText: $followText")
    Log.d("FriendProfile", "  - isFollowing: $isFollowing")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, CoolGray700, shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 프로필 이미지
                if (member?.profileImageUrl != null) {
                    androidx.compose.foundation.Image(
                        painter = rememberAsyncImagePainter(member.profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier.size(56.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = displayName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Text(
                    text = member?.bio ?: "안녕하세요.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 팔로워 버튼
            OutlinedButton(
                onClick = { /* TODO: 팔로워 목록 보기 */ },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("팔로워 $followerCount", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 팔로우/팔로잉 버튼
            Button(
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) CoolGray200 else CoolGray700,
                    contentColor = if (isFollowing) TextBlack else White
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = if (isFollowing) TextBlack else White
                    )
                } else {
                    Text(text = followText)
                    if (isFollowing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterAndCategorySection(
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit,
    onPeriodClick: () -> Unit,
    selectedCategory: Int,
    onCategoryClick: (Int) -> Unit
) {
    val tabs = listOf("올해", "이번 달", "평생")
    val categories = listOf("전체", "도서", "영화", "공연")
    val counts = listOf(0, 0, 0, 0) // TODO: 실제 데이터로 대체
    val categoryColors = listOf(
        Color(0xFFEAF0F8),
        Color(0xFFD4EBFF),
        Color(0xFFFFF3C8),
        Color(0xFFE9F5D4)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        // 상단 탭 필터
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tabs.forEachIndexed { index, title ->
                    OutlinedButton(
                        onClick = { onTabClick(index) },
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, CoolGray200),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (index == selectedTabIndex) Color.White else Color(0xFFF5F5F5),
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(text = title, fontSize = 12.sp)
                    }
                }
            }

            TextButton(onClick = onPeriodClick) {
                Text("기간 >", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 카테고리 카드
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { i, name ->
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(categoryColors[i], Color.White),
                                radius = 130f
                            )
                        )
                        .border(1.dp, CoolGray700, RoundedCornerShape(12.dp))
                        .clickable { onCategoryClick(i) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = name, fontSize = 12.sp, color = Color.DarkGray)
                    Text(text = counts[i].toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun RecordList() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
    ) {
        Text(
            text = "기록",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(16.dp))

        // TODO: 실제 기록 데이터로 대체
        Text(
            text = "기록이 없습니다",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FriendProfileScreenPreview() {
    val sampleMember = MemberInfoResponse(
        id = 1L,
        username = "sampleuser",
        name = "샘플 사용자",
        profileImageUrl = null,
        bio = "안녕하세요! 저는 샘플 사용자입니다.",
        preference = "영화, 도서",
        link = null
    )

    FriendProfileScreen(
        targetMember = sampleMember,
        onBackClick = {}
    )
}