package com.example.baba.ui.home

import android.content.Intent
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.ui.theme.TextBlack
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val today = LocalDate.now()
    var currentMonth by remember { mutableStateOf(today.withDayOfMonth(1)) }

    var showCategorySheet by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val recordMap = if (currentMonth.year == today.year && currentMonth.monthValue == today.monthValue) {
        mapOf(
            4 to R.drawable.sample_movie,
            8 to R.drawable.sample_theater,
            12 to R.drawable.sample_movie2
        )
    } else emptyMap()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .background(Color.White)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Column {
                Text(
                    text = "BABA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2754C5)
                )
                Text(
                    text = "칠공주’s",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_left),
                    contentDescription = "Previous Month",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { currentMonth = currentMonth.minusMonths(1) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${currentMonth.year}.${currentMonth.monthValue.toString().padStart(2, '0')}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = "Next Month",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { currentMonth = currentMonth.plusMonths(1) }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
            ) {
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = currentMonth.dayOfWeek.value % 7

                items(firstDayOfWeek) {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                items(daysInMonth) { idx ->
                    val day = idx + 1
                    val date = currentMonth.withDayOfMonth(day)
                    val isToday = date == today
                    val hasRecord = recordMap.containsKey(day)

                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .size(64.dp)
                            .clickable {
                                selectedDate = date
                                showCategorySheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            hasRecord -> {
                                Image(
                                    painter = painterResource(id = recordMap[day]!!),
                                    contentDescription = "기록 이미지",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.small)
                                )
                                Text(
                                    text = day.toString(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 4.dp)
                                )
                            }
                            isToday -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFB3D4FF), shape = MaterialTheme.shapes.medium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = day.toString(),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextBlack
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_add),
                                            contentDescription = "오늘 기록 추가",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = day.toString(),
                                    fontSize = 12.sp,
                                    color = TextBlack
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "전체 기록 추가",
                    tint = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color(0xFF2754C5),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                        .clickable {
                            selectedDate = null
                            showCategorySheet = true
                        }
                        .padding(14.dp)
                )
            }
        }

        if (showCategorySheet) {
            CreateCategorySheet(
                selectedDate = selectedDate,
                onClose = { showCategorySheet = false },
                onCategoryClick = { category ->
                    val intent = Intent(context, CreateActivity::class.java).apply {
                        putExtra("category", category)
                        putExtra("selectedDate", selectedDate?.toString())
                    }
                    context.startActivity(intent)
                    showCategorySheet = false
                }
            )
        }
    }
}

@Composable
fun CreateCategorySheet(
    selectedDate: LocalDate?,
    onClose: () -> Unit,
    onCategoryClick: (String) -> Unit
) {
    val dateText = selectedDate
        ?.format(DateTimeFormatter.ofPattern("M월 d일에 무엇을 기록할까요?"))
        ?: "무엇을 기록할까요?"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .clickable { onClose() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.White)
                .padding(vertical = 24.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color.LightGray, shape = RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = dateText, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CategoryButton("도서", R.drawable.recommend_book) { onCategoryClick("도서") }
                CategoryButton("영화", R.drawable.recommend_movie) { onCategoryClick("영화") }
                CategoryButton("공연", R.drawable.recommend_show) { onCategoryClick("공연") }
            }
        }
    }
}

@Composable
fun CategoryButton(label: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Card(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    HomeScreen()
}
