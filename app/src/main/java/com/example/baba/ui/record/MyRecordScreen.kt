package com.example.baba.ui.record

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
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
import com.example.baba.MainActivity
import com.example.baba.R
import com.example.baba.data.member.MemberInfoResponse
import com.example.baba.data.network.SessionManager
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.friends.FollowScreen
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
                val response = RetrofitInstance.memberApi.getMyInfo()
                if (response.isSuccessful) {
                    memberInfo = response.body()
                }
            } catch (e: Exception) {
                Log.e("MyRecordMainContent", "회원 정보 조회 실패: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = { TopBar(name = memberInfo?.name ?: "", onLogout = onLogout) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 56.dp)
        ) {
            item { ProfileCard(onFollowerClick, navController, memberInfo = memberInfo) }
            item { TimeFilterTabs() }
            item {
                CategoryTabs(onCategoryClick, categoryCounts)
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
                                            // 세션 정보 클리어
                                            SessionManager.userId = null
                                            SessionManager.needsRefresh = false

                                            Toast.makeText(context, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()

                                            // 콜백으로 로그아웃 처리
                                            onLogout()  // ← Intent 대신 콜백 사용
                                        } else {
                                            Toast.makeText(context, "로그아웃 실패", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "로그아웃 오류: ${e.message}", Toast.LENGTH_SHORT).show()
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
fun ProfileCard(
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
                    val followers = RetrofitInstance.friendsApi.getFollowerList(info.username)
                    followerCount = followers.size
                } catch (e: Exception) {
                    Log.e("ProfileCard", "팔로워 수 조회 실패: ${e.message}")
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(250.dp)
        ) {
            if (memberInfo == null) {
                // 로딩 상태
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(start = 16.dp, top = 16.dp)
                ) {
                    // 프로필 이미지
                    if (memberInfo.profileImageUrl != null) {
                        Image(
                            painter = rememberAsyncImagePainter(memberInfo.profileImageUrl),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(75.dp)
                                .clip(CircleShape)
                                .testTag("profile_image"),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(75.dp)
                                .testTag("profile_image")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = memberInfo.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.testTag("name")
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = memberInfo.bio ?: "안녕하세요!",
                        fontSize = 13.sp,
                        modifier = Modifier.testTag("comment"),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onFollowerClick,
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
                    ) {
                        Text("팔로워 $followerCount", fontSize = 12.sp, color = Color.Black)
                    }
                }

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
                            modifier = Modifier.size(16.dp)
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
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// 3. 시간 필터 탭
@Composable
fun TimeFilterTabs() {
    val tabs = listOf("올해", "이번 달", "평생")
    var selectedTab by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 필터 버튼들
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Button(
                    onClick = { selectedTab = index },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (index == selectedTab) Color.LightGray else Color.White
                    )
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        color = if (index == selectedTab) Color.Black else Color.Gray
                    )
                }
            }
        }

        // 오른쪽 "기간" 버튼
        Button(
            onClick = { /* TODO: 기간 설정 기능 */ },
            shape = RoundedCornerShape(20.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Text("기간 >", fontSize = 8.sp, color = Color.Black)
        }
    }

    Spacer(modifier = Modifier.height(8.dp)) // 하단 여백
}

// 4. 기록 분류
@Composable
fun CategoryTabs(
    onCategoryClick: (String) -> Unit,
    categoryCounts: Map<String, Int> = mapOf("전체" to 0, "도서" to 0, "영화" to 0, "공연" to 0)
) {
    val categoryName = listOf("전체", "도서", "영화", "공연")
    var selected by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        categoryName.forEachIndexed { i, name ->
            val count = categoryCounts[name] ?: 0
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray)
                    .clickable {
                        selected = i
                        onCategoryClick(name)
                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$name $count",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
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
        Text("기록", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        diaries.forEach { diary ->
            DiaryCard(diary, navController)
            Spacer(modifier = Modifier.height(12.dp))
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

@Composable
fun DiaryCard(diary: DiaryResponse, navController: NavController) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        WatchedDateManager.initialize(context)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable {
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Base64 이미지 처리
            val imageBitmap = rememberBase64ImageBitmap(diary.image)

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
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
                    // 이미지가 없을 때 플레이스홀더
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (diary.category) {
                                    "BOOK" -> R.drawable.recommend_book
                                    "MOVIE" -> R.drawable.recommend_movie
                                    "PERFORMANCE" -> R.drawable.recommend_show
                                    else -> R.drawable.ic_add
                                }
                            ),
                            contentDescription = "카테고리 아이콘",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 텍스트 정보
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = diary.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                Text(
                    text = diary.categoryLabel,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                val watchedDate = WatchedDateManager.getWatchedDate(diary.id)
                    ?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    ?: diary.createdDate.split(" ")[0]

                Text(
                    text = watchedDate,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Text(
                    text = diary.content,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    maxLines = 1
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyRecordScreenPreview() {
    val fakeNavController = rememberNavController()
    MyRecordScreen(navController = fakeNavController, onLogout = {})
}