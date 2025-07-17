package com.example.baba.ui.record

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate
import com.example.baba.R
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyRecordListScreen(category: String, navController: NavController) {
    val categoryName = listOf("전체", "도서", "영화", "공연")
    val initialIndex = categoryName.indexOf(category).takeIf { it >= 0 } ?: 0
    var selected by rememberSaveable { mutableStateOf(initialIndex) }
    var isGridView by rememberSaveable { mutableStateOf(false) }

    var records by remember { mutableStateOf<List<RecordData>>(emptyList()) }
    var memberName by remember { mutableStateOf("") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        WatchedDateManager.initialize(context)
        try {
            // 회원 정보 조회 (이름만 가져오기)
            val memberResponse = RetrofitInstance.memberApi.getMyInfo()
            if (memberResponse.isSuccessful) {
                memberName = memberResponse.body()?.name ?: ""
            }

            // SessionManager에서 직접 userId 가져오기
            val currentUserId = SessionManager.userId
            if (currentUserId != null && currentUserId > 0) {
                val diaryResponse = RetrofitInstance.diaryApi.getAllMyDiaries(userId = currentUserId)
                if (diaryResponse.isSuccessful) {
                    val diaries = diaryResponse.body() ?: emptyList()
                    records = diaries.map {
                        RecordData(
                            id = it.id,
                            title = it.title,
                            category = when (it.category) {
                                "BOOK" -> "도서"
                                "MOVIE" -> "영화"
                                "PERFORMANCE" -> "공연"
                                else -> "기타"
                            },
                            rating = it.rating.toString(),
                            date = WatchedDateManager.getWatchedDate(it.id)
                                ?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                ?: it.createdDate.split(" ")[0],
                            comment = it.content,
                            image = it.image
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val filteredRecords = if (selected == 0) records else records.filter { it.category == categoryName[selected] }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (memberName.isNotEmpty()) "$memberName’s" else "",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
//                Row {
//                    IconButton(onClick = { isGridView = false }) {
//                        Icon(
//                            imageVector = Icons.Default.Build,
//                            contentDescription = "List View",
//                            tint = if (!isGridView) Color(0xFF3366FF) else Color.Gray
//                        )
//                    }
//                    IconButton(onClick = { isGridView = true }) {
//                        Icon(
//                            imageVector = Icons.Default.Star,
//                            contentDescription = "Grid View",
//                            tint = if (isGridView) Color(0xFF3366FF) else Color.Gray
//                        )
//                    }
//                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // 카테고리 탭
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    categoryName.forEachIndexed { index, name ->
                        val isSelected = index == selected
                        Text(
                            text = name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isSelected) White else CoolGray700,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Blue2 else CoolGray150)
                                .clickable { selected = index }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // 리스트/그리드 버튼
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isGridView = false },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_list),
                            contentDescription = "리스트 보기",
                            modifier = Modifier.size(20.dp),
                            tint = if (!isGridView) Blue2 else CoolGray500
                        )
                    }
                    IconButton(
                        onClick = { isGridView = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_grid),
                            contentDescription = "그리드 보기",
                            modifier = Modifier.size(20.dp),
                            tint = if (isGridView) Blue2 else CoolGray500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isGridView) {
                // Grid View
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRecords.size) { index ->
                        val record = filteredRecords[index]
                        GridImageItem(
                            imageBase64 = record.image,
                            title = record.title,
                            category = record.category,  // 카테고리 전달
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.7f)
                                .clip(RoundedCornerShape(8.dp)),
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("record", Record(
                                    id = record.id,
                                    title = record.title,
                                    date = record.date,
                                    category = record.category,
                                    rating = record.rating.toFloat(),
                                    content = record.comment,
                                    isPublic = false,
                                    photoUri = null
                                ))
                                navController.navigate("recordDetail")
                            }
                        )
                    }
                }
            } else {
                // List View
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .background(White)
                ) {
                    items(filteredRecords.size) { index ->
                        val record = filteredRecords[index]
                        RecordItem(
                            id = record.id,
                            title = record.title,
                            category = record.category,
                            rating = record.rating,
                            date = record.date,
                            comment = record.comment,
                            imageBase64 = record.image,
                            onClick = {
                                navController.currentBackStackEntry?.savedStateHandle?.set("record", Record(
                                    id = record.id,
                                    title = record.title,
                                    date = record.date,
                                    category = record.category,
                                    rating = record.rating.toFloat(),
                                    content = record.comment,
                                    isPublic = false,
                                    photoUri = null
                                ))
                                navController.navigate("recordDetail")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun rememberBase64Image(base64String: String?): androidx.compose.ui.graphics.ImageBitmap? {
    return remember(base64String) {
        try {
            if (base64String.isNullOrEmpty()) return@remember null
            val imageBytes = Base64.decode(base64String, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }
}

// Grid View용 이미지 아이템
@Composable
fun GridImageItem(
    imageBase64: String?,
    title: String,
    category: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val imageBitmap = rememberBase64Image(imageBase64)

    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.LightGray)
            .clickable { onClick() }
    ) {
        if (imageBitmap != null) {
            // 이미지가 있을 때는 이미지만 표시
            Image(
                bitmap = imageBitmap,
                contentDescription = "Thumbnail",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // 카테고리 아이콘
            Image(
                painter = painterResource(
                    id = when (category) {
                        "도서" -> R.drawable.recommend_book
                        "영화" -> R.drawable.recommend_movie
                        "공연" -> R.drawable.recommend_show
                        else -> R.drawable.ic_add
                    }
                ),
                contentDescription = "카테고리 아이콘",
                modifier = Modifier
                    .size(45.dp)
                    .align(Alignment.Center)
            )

            // 제목
            Text(
                text = title,
                color = CoolGray500,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .offset(y = (-16).dp)
                    .padding(horizontal = 4.dp)
            )
        }
    }
}

data class RecordData(
    val id: Long,
    val title: String,
    val category: String,
    val rating: String,
    val date: String,
    val comment: String,
    val image: String?
)

@Composable
fun RecordItem(
    id: Long,
    title: String,
    category: String,
    rating: String,
    date: String,
    comment: String,
    imageBase64: String?,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        WatchedDateManager.initialize(context)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageBitmap = rememberBase64Image(imageBase64)

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Image(
                        painter = painterResource(
                            id = when (category) {
                                "도서" -> R.drawable.recommend_book
                                "영화" -> R.drawable.recommend_movie
                                "공연" -> R.drawable.recommend_show
                                else -> R.drawable.ic_add
                            }
                        ),
                        contentDescription = "카테고리 아이콘",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 제목
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                // 카테고리와 연도
                Text(
                    text = "${category} ${date.split("-")[0]}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // 별점과 날짜
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rating_button_star),
                        contentDescription = null,
                        tint = Color.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text = " $rating", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(8.dp))

                    val watchedDate = WatchedDateManager.getWatchedDate(id)
                        ?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        ?: date

                    Text(text = watchedDate, fontSize = 12.sp, color = Color.Gray)
                }

                // 코멘트
                if (comment.isNotBlank()) {
                    Text(
                        text = comment,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}