package com.example.baba.ui.record

import android.util.Log
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.record.DiaryResponse
import androidx.compose.foundation.layout.Spacer

//화면 출력
@Preview(showBackground = true)
@Composable
fun MyRecordScreen() {
    var showRecordList by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("전체") }

    if (showRecordList) {
        MyRecordListScreen(category = selectedCategory)
    } else {
        MyRecordMainContent(
            onCategoryClick = { category ->
                selectedCategory = category
                showRecordList = true
            }
        )
    }
}

@Composable
fun MyRecordMainContent(
    onCategoryClick: (String) -> Unit
) {
    var categoryCounts by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    Scaffold(
        topBar = { TopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 56.dp)
        ) {
            item { ProfileCard() }
            item { TimeFilterTabs() }
            item {
                CategoryTabs(onCategoryClick, categoryCounts)
            }
            item {
                RecordList(onCountReady = { counts -> categoryCounts = counts })
            }
        }
    }
}


// 1. 탑 바 구현
@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("칠공주's", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Row {
            Icon(Icons.Default.Search, contentDescription = "Search")
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}

// 2. 프로필 카드 구현
@Composable
fun ProfileCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .height(250.dp)) {

            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 16.dp, top = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(75.dp)
                        .testTag("profile_image")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "칠공주",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.testTag("name")
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "안녕하세요 칠공주의 공간입니다.",
                    fontSize = 13.sp,
                    modifier = Modifier.testTag("coment")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
                ) {
                    Text("팔로워 3", fontSize = 12.sp, color = Color.Black)
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
                    onClick = { },
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


// 5. 기록 리스트
@Composable
fun RecordList(onCountReady: (Map<String, Int>) -> Unit = {}) {
    var diaries by remember { mutableStateOf<List<DiaryResponse>>(emptyList()) }

    LaunchedEffect(Unit) {
        try {
            val userId = 1L
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp)
    ) {
        Text("기록", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        diaries.forEach { diary ->
            DiaryCard(diary)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


@Composable
fun DiaryCard(diary: DiaryResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
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
            // 썸네일 이미지 (임시)
            AsyncImage(
                model = "https://via.placeholder.com/80", // 썸네일 이미지 URL 또는 diary.imageUrl
                contentDescription = "썸네일",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray)
            )

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
                Text(
                    text = diary.createdDate.split(" ")[0],  // 날짜만
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


//@Preview(showBackground = true, name = "MyRecord Preview")
@Composable
fun MyRecordScreenPreview() {
    MyRecordScreen()
}