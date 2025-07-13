package com.example.baba.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.ui.theme.TextBlack


@Composable
fun MyRecordScreen() {
    Scaffold(
        topBar = { TopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 56.dp) // 바텀바 높이 고려
        ) {
            item { ProfileCard() }
            item { TimeFilterTabs() }
            item { CategoryTabs() }
            item { RecordList() }
        }
    }
}


// 1. 상단바
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

// 2. 프로필 카드
@Composable
fun ProfileCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp) // 고정 높이 지정
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // 공유 / 편집 아이콘 - 우측 상단
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(3.dp)
            ) {

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(15.dp),
                    //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Share, //임시 아이콘 (교체 예정)
                        contentDescription = "Share",
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(15.dp),
                    //contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF000))
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit, //임시 아이콘 (교체 예정)
                        contentDescription = "Edit",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // 프로필 내용 - 좌측 정렬
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
            ) {
                Column {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(75.dp)
                            .padding(end = 12.dp)
                            .testTag("profile_image")
                    )

                    Column {
                        Text(
                            "칠공주",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.testTag("name"))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "안녕하세요 칠공주의 공간입니다.",
                            fontSize = 13.sp,
                            modifier = Modifier.testTag("coment"))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 팔로워 버튼
                Button(
                    onClick = { /* TODO */ },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F1F1))
                ) {
                    Text("팔로워 3", fontSize = 12.sp, color = Color.Black)
                }
            }
        }
    }
}


// 3. 기간 필터 탭
@Composable
fun TimeFilterTabs() {
    val tabs = listOf("올해", "이번 달", "평생")
    var selectedTab by remember { mutableStateOf(0) }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        tabs.forEachIndexed { index, title ->
            Text(
                text = title,
                fontWeight = if (index == selectedTab) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable { selectedTab = index }
            )
        }
    }
}

// 4. 카테고리 탭
@Composable
fun CategoryTabs() {
    val categories = listOf("전체 6", "도서 1", "영화 3", "공연 2")
    var selected by remember { mutableStateOf(0) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        categories.forEachIndexed { i, text ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (i == selected) Color.LightGray else Color.White)
                    .clickable { selected = i }
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Text(text)
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

// 5. 기록 리스트
@Composable
fun RecordList() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.LightGray)
                ) {
                    Icon(Icons.Default.DateRange, //임시 아이콘 (교체 예정)
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("하데스타운", fontWeight = FontWeight.Bold)
                    Text("공연", fontSize = 12.sp, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Done, //임시 아이콘 (교체 예정)
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp))
                        Text("4.5", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2025.07.09.", fontSize = 12.sp)
                    }
                    Text("추천해요", fontSize = 12.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "MyRecord Preview")
@Composable
fun MyRecordScreenPreview() {
    MyRecordScreen()
}