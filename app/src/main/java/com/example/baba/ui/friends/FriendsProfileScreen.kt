package com.example.baba.ui.friends

import android.annotation.SuppressLint
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.baba.ui.theme.*


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendProfileScreen(
    initialIsFollowing: Boolean = false,
    onFollowClick: () -> Unit = {},
    onFollowChanged: (Boolean) -> Unit = {},
    initialSelectedCategory: Int = 0,
    onBackClick: () -> Unit = {}
) {
    var isFollowing by remember { mutableStateOf(initialIsFollowing) }
    val userName = "hihihi"
    val followText = if (isFollowing) "팔로잉" else "팔로우"
    val selectedTab = remember { mutableStateOf(0) }
    val selectedCategory = remember { mutableStateOf(initialSelectedCategory) }
    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("$userName's") },
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
                FriendProfileCard(
                    userName = userName,
                    followText = followText,
                    isFollowing = isFollowing,
                    onFollowClick = {
                        if (isFollowing) {
                            showBottomSheet = true // 언팔 바텀시트 열기
                        } else {
                            isFollowing = true // 팔로우 처리
                            onFollowChanged(isFollowing)
                        }
                    }

                )
            }

            item {
                FilterAndCategorySection(
                    selectedTabIndex = selectedTab.value,
                    onTabClick = { selectedTab.value = it },
                    onPeriodClick = { /* TODO */ },
                    selectedCategory = selectedCategory.value,
                    onCategoryClick = { selectedCategory.value = it }
                )
            }
            item {
                RecordList()
            }
        }
    }
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
                        isFollowing = false // 언팔 처리
                        onFollowChanged(false)
                        showBottomSheet = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
                ) {
                    Text("언팔로우")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = { showBottomSheet = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("취소", color = Color.Gray)
                }
            }
        }
    }

}

@Composable
fun FriendProfileCard(
    userName: String,
    followText: String,
    isFollowing: Boolean,
    onFollowClick: () -> Unit
) {
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
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.size(56.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(userName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("안녕하세요.", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 팔로워 버튼
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .wrapContentWidth()
                    .height(32.dp),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                border = BorderStroke(1.dp, Color.LightGray)
            ) {
                Text("팔로워 1", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 기존 팔로우/팔로잉 버튼
            Button(
                onClick = onFollowClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) CoolGray200 else CoolGray700,
                    contentColor = if (isFollowing) TextBlack else White
                )
            ) {
                Text(text = followText)
                if (isFollowing) Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
    val counts = listOf(3, 2, 0, 1)
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

        Button(
            onClick = { /* TODO: 기록 상세 보기 등 */ },
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
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.LightGray)
                ) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text("하데스타운", fontWeight = FontWeight.Bold)
                    Text("공연", fontSize = 12.sp, color = Color.Gray)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Done,
                            contentDescription = null,
                            tint = Color(0xFF1E88E5),
                            modifier = Modifier.size(16.dp)
                        )
                        Text("4.5", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("2025.07.09.", fontSize = 12.sp)
                    }

                    Text("추천해요", fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun FriendProfileScreenPreview() {
    FriendProfileScreen(
        initialIsFollowing = false,
        onBackClick = {},
        onFollowClick = {}
    )
}
