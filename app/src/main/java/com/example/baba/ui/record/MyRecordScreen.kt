package com.example.baba.ui.record

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.record.DiaryResponse
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.baba.R
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.data.network.PersistentSessionManager
import com.example.baba.data.network.SessionManager
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.friends.FollowScreen
import com.example.baba.ui.theme.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter


// 필터링 함수
fun filterDiariesByTab(selectedTab: Int, diaries: List<DiaryResponse>): List<DiaryResponse> {
    val today = LocalDate.now()
    return when (selectedTab) {
        0 -> { // 올해
            diaries.filter {
                val watchedDate = WatchedDateManager.getWatchedDate(it.id)
                    ?: LocalDate.now() // watchedDate가 없으면 기본 현재 날짜 사용
                watchedDate.year == today.year
            }
        }
        1 -> { // 이번 달
            diaries.filter {
                val watchedDate = WatchedDateManager.getWatchedDate(it.id)
                    ?: LocalDate.now() // watchedDate가 없으면 기본 현재 날짜 사용
                watchedDate.year == today.year && watchedDate.month == today.month
            }
        }
        2 -> { // 평생
            diaries // 모든 데이터를 반환
        }
        else -> emptyList()
    }
}

// 대체 회원 정보 생성 함수
private fun createFallbackMemberInfo(): MemberInfoResponse? {
    val savedUserId = SessionManager.userId
    val savedUserName = SessionManager.userName
    val savedUsername = SessionManager.username

    return if (savedUserId != null && savedUserName != null && savedUsername != null) {
        MemberInfoResponse(
            id = savedUserId,
            username = savedUsername,
            name = savedUserName,
            profileImageUrl = null,
            bio = "안녕하세요!",
            preference = null,
            link = null
        )
    } else {
        null
    }
}

//화면 출력
@Composable
fun MyRecordScreen(navController: NavController, onLogout: () -> Unit) {
    var showRecordList by remember { mutableStateOf(false) }
    var showFollowScreen by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("전체") }

    when {
        showFollowScreen -> {
            FollowScreen(
                currentRoute = "friends",
                onNavigate = { /* TODO: 바텀 네비 전환 로직 */ },
                onBackClick = { showFollowScreen = false } // ← 뒤로가기 시 이전 화면
            )
        }

        showRecordList -> {
            MyRecordListScreen(category = selectedCategory, navController = navController)
        }

        else -> {
            MyRecordMainContent(
                onCategoryClick = { category ->
                    selectedCategory = category
                    showRecordList = true
                },
                onFollowerClick = { showFollowScreen = true },
                navController = navController,
                onLogout = onLogout
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecordMainContent(
    onCategoryClick: (String) -> Unit,
    onFollowerClick: () -> Unit,
    navController: NavController,
    onLogout: () -> Unit
) {
    var categoryCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var memberInfo by remember { mutableStateOf<MemberInfoResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                Log.d("MyRecordScreen", "회원 정보 조회 시작")
                val response = RetrofitInstance.memberApi.getMyInfo()
                Log.d("MyRecordScreen", "회원 정보 응답: ${response.isSuccessful}, code: ${response.code()}")

                if (response.isSuccessful) {
                    memberInfo = response.body()
                    Log.d("MyRecordScreen", "회원 정보 로드 성공: ${memberInfo?.name}")
                } else {
                    Log.e("MyRecordScreen", "회원 정보 조회 실패: ${response.errorBody()?.string()}")

                    // 실패 시 SessionManager에서 저장된 정보 사용
                    memberInfo = createFallbackMemberInfo()
                    Log.d("MyRecordScreen", "대체 회원 정보 사용: ${memberInfo?.name}")
                }
            } catch (e: Exception) {
                Log.e("MyRecordMainContent", "회원 정보 조회 실패: ${e.message}")

                // 예외 발생 시에도 대체 정보 사용
                memberInfo = createFallbackMemberInfo()
                Log.d("MyRecordScreen", "예외 발생으로 대체 회원 정보 사용: ${memberInfo?.name}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            TopBar(name = memberInfo?.name ?: "", onLogout = onLogout)
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
                MyProfileCard(
                    onFollowerClick = onFollowerClick,
                    navController = navController,
                    memberInfo = memberInfo
                )
            }

            item {
                FilterAndCategorySection(
                    selectedTabIndex = 0,
                    onTabClick = { /* TODO */ },
                    onPeriodClick = { /* TODO */ },
                    onCategoryClick = onCategoryClick,
                    categoryCounts = categoryCounts
                )
            }

            item {
                RecordList(
                    onCountReady = { counts -> categoryCounts = counts },
                    navController = navController
                )
            }
        }
    }
}

// 1. 탑 바 구현
@Composable
fun TopBar(name: String, onLogout: () -> Unit) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("$name's", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row {
            Icon(Icons.Default.Search, contentDescription = "Search")
            Spacer(modifier = Modifier.width(16.dp))

            Box {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.clickable { showDropdownMenu = true }
                )

                DropdownMenu(
                    expanded = showDropdownMenu,
                    onDismissRequest = { showDropdownMenu = false },
                    modifier = Modifier
                        .width(120.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "로그아웃",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        },
                        onClick = {
                            showDropdownMenu = false
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response = RetrofitInstance.authApi.logout()
                                    withContext(Dispatchers.Main) {
                                        if (response.isSuccessful) {
                                            PersistentSessionManager.clearSession()
                                            Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                                            onLogout()
                                        } else {
                                            Toast.makeText(context, "로그아웃 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        PersistentSessionManager.clearSession()
                                        Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
                                        onLogout()
                                    }
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

// 2. 프로필 카드 구현
@Composable
fun MyProfileCard(
    onFollowerClick: () -> Unit,
    navController: NavController,
    memberInfo: MemberInfoResponse?
) {
    var followerCount by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(memberInfo) {
        memberInfo?.let { info ->
            coroutineScope.launch {
                try {
                    val followerUsernames = RetrofitInstance.friendsApi.getFollowerList(info.username)
                    followerCount = followerUsernames.size
                    Log.d("ProfileCard", "팔로워: ${followerCount}")
                } catch (e: Exception) {
                    Log.e("ProfileCard", "팔로워 수 조회 실패: ${e.message}")
                }
            }
        }
    }

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
                if (memberInfo?.profileImageUrl != null) {
                    Image(
                        painter = rememberAsyncImagePainter(memberInfo.profileImageUrl),
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
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
                    text = memberInfo?.name ?: "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = memberInfo?.bio ?: "안녕하세요.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 팔로워 버튼만 표시 (팔로잉 제거)
            Button(
                onClick = onFollowerClick,
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
            ) {
                Text("팔로워 $followerCount", fontSize = 12.sp, color = Color.Black)
            }
        }

        // 오른쪽 버튼들
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Button(
                onClick = { },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF000))
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(16.dp),
                    tint = CoolGray700
                )
            }

            Button(
                onClick = { navController.navigate("editProfile") },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(36.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF000))
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    modifier = Modifier.size(16.dp),
                    tint = CoolGray700
                )
            }
        }
    }
}

@Composable
fun FilterAndCategorySection(
    selectedTabIndex: Int,
    onTabClick: (Int) -> Unit,
    onPeriodClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    categoryCounts: Map<String, Int>
) {
    val tabs = listOf("올해", "이번 달", "평생")
    val categories = listOf("전체", "도서", "영화", "공연")
    val counts = listOf(
        categoryCounts["전체"] ?: 0,
        categoryCounts["도서"] ?: 0,
        categoryCounts["영화"] ?: 0,
        categoryCounts["공연"] ?: 0
    )
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
                        .clickable { onCategoryClick(name) }
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

// 5. 기록 리스트 - 수정된 부분
@Composable
fun RecordList(
    onCountReady: (Map<String, Int>) -> Unit = {},
    navController: NavController
) {
    var diaries by remember { mutableStateOf<List<DiaryResponse>>(emptyList()) }
    val context = LocalContext.current

    // SessionManager에서 직접 userId 가져오기
    LaunchedEffect(Unit) {
        val userId = SessionManager.userId  // ← 직접 SessionManager 사용
        if (userId != null && userId > 0) {
            try {
                val response = RetrofitInstance.diaryApi.getAllMyDiaries(userId)
                if (response.isSuccessful) {
                    val data = response.body() ?: emptyList()
                    diaries = data

                    // 카테고리별 개수 계산
                    val counts = mapOf(
                        "전체" to data.size,
                        "도서" to data.count { it.category == "BOOK" },
                        "영화" to data.count { it.category == "MOVIE" },
                        "공연" to data.count { it.category == "PERFORMANCE" },
                    )
                    onCountReady(counts)
                }
            } catch (e: Exception) {
                Log.e("RecordList", "일기 불러오기 실패: ${e.message}")
            }
        }
    }

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

        diaries.forEach { diary ->
            Button(
                onClick = {
                    val record = Record(
                        id = diary.id,
                        title = diary.title,
                        date = diary.createdDate.split(" ")[0],
                        category = diary.categoryLabel,
                        rating = diary.rating.toFloat(),
                        content = diary.content,
                        isPublic = false,
                        photoUri = null
                    )

                    navController.currentBackStackEntry?.savedStateHandle?.set("record", record)
                    navController.navigate("recordDetail")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF5F5F5),
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(16.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Base64 이미지 처리
                    val imageBitmap = rememberBase64ImageBitmap(diary.image)

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.LightGray)
                    ) {
                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap,
                                contentDescription = "썸네일",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                painter = painterResource(
                                    id = when (diary.category) {
                                        "BOOK" -> R.drawable.recommend_book
                                        "MOVIE" -> R.drawable.recommend_movie
                                        "PERFORMANCE" -> R.drawable.recommend_show
                                        else -> R.drawable.ic_add
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(diary.title, fontWeight = FontWeight.Bold)
                        Text("${diary.categoryLabel}", fontSize = 12.sp, color = Color.Gray)

                        val watchedDate = WatchedDateManager.getWatchedDate(diary.id)
                            ?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                            ?: diary.createdDate.split(" ")[0]

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Done,
                                contentDescription = null,
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(16.dp)
                            )
                            Text("${diary.rating}", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(watchedDate, fontSize = 12.sp)
                        }

                        Text(diary.content, fontSize = 12.sp, maxLines = 1)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Base64 문자열을 ImageBitmap으로 변환하는 함수
@Composable
fun rememberBase64ImageBitmap(base64String: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(base64String) {
        try {
            if (base64String.isNullOrEmpty()) return@remember null
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            Log.e("Base64Image", "이미지 변환 실패: ${e.message}")
            null
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRecordScreenPreview() {
    val fakeNavController = rememberNavController()
    MyRecordScreen(navController = fakeNavController, onLogout = {})
}