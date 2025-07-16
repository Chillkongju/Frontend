package com.example.baba.ui.recommendation

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import android.widget.Toast
import com.example.baba.R
import com.example.baba.ui.theme.*
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.data.recommendation.RecommendationResponse
import com.example.baba.data.recommendation.CategoryRecommendationResponse
import kotlinx.coroutines.launch

// 월별 카테고리 추천 데이터 모델
data class CategoryRecommendation(
    val category: String,
    val recommendations: List<RecommendationResponse>
)

@Composable
fun RecommendationScreen() {
    var monthlyRecommendations by remember { mutableStateOf<List<CategoryRecommendation>>(emptyList()) }
    var isLoadingMonthly by remember { mutableStateOf(false) }
    var isGeneratingMonthly by remember { mutableStateOf(false) }
    var monthlyError by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // 월별 추천 로드
    LaunchedEffect(Unit) {
        isLoadingMonthly = true
        monthlyError = null

        try {
            val userId = SessionManager.userId
            if (userId != null) {
                Log.d("RecommendationScreen", "월별 추천 조회 시작 - userId: $userId")
                val response = RetrofitInstance.recommendationApi.getMonthlyRecommendations(userId)

                if (response.isSuccessful) {
                    val apiRecommendations = response.body() ?: emptyList()
                    Log.d("RecommendationScreen", "월별 추천 조회 성공: ${apiRecommendations.size}개 카테고리")

                    monthlyRecommendations = apiRecommendations
                        .filter { it.recommendations.isNotEmpty() } // 빈 카테고리 제외
                        .map { categoryResponse ->
                            Log.d("RecommendationScreen", "카테고리: ${categoryResponse.category}, 원본 추천 수: ${categoryResponse.recommendations.size}")

                            val filteredRecommendations = categoryResponse.recommendations.filter { recommendation ->
                                val isValid = recommendation.title.isNotBlank() &&
                                        recommendation.summary.isNotBlank() &&
                                        recommendation.title.trim().isNotEmpty() &&
                                        recommendation.summary.trim().isNotEmpty()
                                if (!isValid) {
                                    Log.d("RecommendationScreen", "필터링된 추천: title='${recommendation.title}', summary='${recommendation.summary.take(50)}...'")
                                }
                                isValid
                            }

                            Log.d("RecommendationScreen", "카테고리: ${categoryResponse.category}, 필터링 후 추천 수: ${filteredRecommendations.size}")

                            CategoryRecommendation(
                                category = categoryResponse.category,
                                recommendations = filteredRecommendations
                            )
                        }
                        .filter { it.recommendations.isNotEmpty() }
                } else {
                    Log.e("RecommendationScreen", "월별 추천 조회 실패: ${response.code()}")
                    monthlyError = "추천을 불러올 수 없습니다"
                }
            } else {
                monthlyError = "로그인이 필요합니다"
            }
        } catch (e: Exception) {
            Log.e("RecommendationScreen", "월별 추천 조회 오류: ${e.message}")
            monthlyError = "네트워크 오류가 발생했습니다"
        } finally {
            isLoadingMonthly = false
        }
    }

    // 월별 추천 생성 함수
    fun generateMonthlyRecommendations() {
        coroutineScope.launch {
            isGeneratingMonthly = true
            monthlyError = null

            try {
                val userId = SessionManager.userId
                if (userId != null) {
                    Log.d("RecommendationScreen", "월별 추천 생성 시작 - userId: $userId")
                    val response = RetrofitInstance.recommendationApi.recommendNextMonth(userId)

                    if (response.isSuccessful) {
                        val apiRecommendations = response.body() ?: emptyList()
                        Log.d("RecommendationScreen", "월별 추천 생성 성공: ${apiRecommendations.size}개 카테고리")

                        monthlyRecommendations = apiRecommendations
                            .filter { it.recommendations.isNotEmpty() } // 빈 카테고리 제외
                            .map { categoryResponse ->
                                Log.d("RecommendationScreen", "생성 - 카테고리: ${categoryResponse.category}, 원본 추천 수: ${categoryResponse.recommendations.size}")

                                val filteredRecommendations = categoryResponse.recommendations.filter { recommendation ->
                                    val isValid = recommendation.title.isNotBlank() &&
                                            recommendation.summary.isNotBlank() &&
                                            recommendation.title.trim().isNotEmpty() &&
                                            recommendation.summary.trim().isNotEmpty()
                                    if (!isValid) {
                                        Log.d("RecommendationScreen", "생성 - 필터링된 추천: title='${recommendation.title}', summary='${recommendation.summary.take(50)}...'")
                                    }
                                    isValid
                                }

                                Log.d("RecommendationScreen", "생성 - 카테고리: ${categoryResponse.category}, 필터링 후 추천 수: ${filteredRecommendations.size}")

                                CategoryRecommendation(
                                    category = categoryResponse.category,
                                    recommendations = filteredRecommendations
                                )
                            }
                            .filter { it.recommendations.isNotEmpty() }
                        Toast.makeText(context, "이번 달 기록을 바탕으로 새로운 추천을 생성했습니다!", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("RecommendationScreen", "월별 추천 생성 실패: ${response.code()}")
                        if (response.code() == 204) {
                            monthlyError = "이번 달 기록이 없어 추천을 생성할 수 없습니다"
                        } else {
                            monthlyError = "추천 생성에 실패했습니다"
                        }
                    }
                } else {
                    monthlyError = "로그인이 필요합니다"
                }
            } catch (e: Exception) {
                Log.e("RecommendationScreen", "월별 추천 생성 오류: ${e.message}")
                monthlyError = "네트워크 오류가 발생했습니다"
            } finally {
                isGeneratingMonthly = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "문화생활 추천",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextBlack
            )

            // 월별 추천 새로고침 버튼
            TextButton(
                onClick = { generateMonthlyRecommendations() },
                enabled = !isGeneratingMonthly
            ) {
                if (isGeneratingMonthly) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "새 추천",
                        fontSize = 14.sp,
                        color = Blue2
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(CoolGray100)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(1.dp))
            }

            // 월별 추천 섹션
            if (isLoadingMonthly) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "추천을 불러오는 중...",
                                fontSize = 14.sp,
                                color = CoolGray500
                            )
                        }
                    }
                }
            } else if (monthlyError != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = monthlyError!!,
                                fontSize = 14.sp,
                                color = CoolGray500,
                                fontWeight = FontWeight.Medium
                            )

                            if (monthlyError == "이번 달 기록이 없어 추천을 생성할 수 없습니다") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "문화생활 기록을 먼저 작성해보세요!",
                                    fontSize = 12.sp,
                                    color = CoolGray500
                                )
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { generateMonthlyRecommendations() },
                                    enabled = !isGeneratingMonthly
                                ) {
                                    Text("다시 시도")
                                }
                            }
                        }
                    }
                }
            } else if (monthlyRecommendations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "아직 추천이 없습니다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = CoolGray700
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "이번 달 기록을 바탕으로 추천을 생성해보세요!",
                                fontSize = 14.sp,
                                color = CoolGray500
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { generateMonthlyRecommendations() },
                                enabled = !isGeneratingMonthly
                            ) {
                                if (isGeneratingMonthly) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                } else {
                                    Text("추천 생성하기")
                                }
                            }
                        }
                    }
                }
            } else {
                // 월별 추천 표시
                monthlyRecommendations.forEach { categoryRecommendation ->
                    // 카테고리 헤더
                    item {
                        CategoryHeader(
                            categoryTitle = categoryRecommendation.category,
                            recommendationCount = categoryRecommendation.recommendations.size
                        )
                    }

                    categoryRecommendation.recommendations.forEachIndexed { index, recommendation ->
                        item {
                            RecommendationCard(
                                recommendation = recommendation,
                                modifier = Modifier.padding(
                                    bottom = if (index == categoryRecommendation.recommendations.size - 1)
                                        12.dp
                                    else
                                        4.dp
                                )
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun CategoryHeader(
    categoryTitle: String,
    recommendationCount: Int
) {
    val categoryIconRes = when (categoryTitle) {
        "영화" -> R.drawable.recommend_movie
        "도서" -> R.drawable.recommend_book
        "공연" -> R.drawable.recommend_show
        else -> R.drawable.recommend_movie
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Image(
            painter = painterResource(id = categoryIconRes),
            contentDescription = categoryTitle,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = "$categoryTitle 추천",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Blue2
        )
        Text(
            text = "(${recommendationCount}개)",
            fontSize = 14.sp,
            color = CoolGray500
        )
    }
}

@Composable
fun MonthlyRecommendationSection(
    categoryTitle: String,
    recommendations: List<RecommendationResponse>
) {
    val categoryIconRes = when (categoryTitle) {
        "영화" -> R.drawable.recommend_movie
        "도서" -> R.drawable.recommend_book
        "공연" -> R.drawable.recommend_show
        else -> R.drawable.recommend_movie
    }

    Column {
        // 카테고리 헤더
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Image(
                painter = painterResource(id = categoryIconRes),
                contentDescription = categoryTitle,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$categoryTitle 추천",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Blue2
            )
            Text(
                text = "(${recommendations.size}개)",
                fontSize = 14.sp,
                color = CoolGray500
            )
        }

        recommendations.forEach { recommendation ->
            RecommendationCard(
                recommendation = recommendation,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Composable
fun RecommendationCard(
    recommendation: RecommendationResponse,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val isLongText = recommendation.summary.length > 150

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clip(RoundedCornerShape(12.dp))
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = recommendation.title.split("\n")[0],
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextBlack
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val titleLines = recommendation.title.split("\n")
                if (titleLines.size > 1 && titleLines[1].contains("출간일")) {
                    Text(
                        text = titleLines[1],
                        fontSize = 12.sp,
                        color = CoolGray500
                    )
                } else if (recommendation.releaseDate.isNotEmpty()) {
                    Text(
                        text = recommendation.releaseDate,
                        fontSize = 12.sp,
                        color = CoolGray500
                    )
                }
                if (recommendation.genre.isNotEmpty()) {
                    Text(
                        text = "• ${recommendation.genre}",
                        fontSize = 12.sp,
                        color = CoolGray500
                    )
                }
            }

            Text(
                text = recommendation.summary,
                fontSize = 13.sp,
                color = CoolGray700,
                lineHeight = 18.sp,
                maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                overflow = if (isExpanded) TextOverflow.Visible else TextOverflow.Ellipsis
            )

            if (isLongText) {
                Text(
                    text = if (isExpanded) "접기" else "더보기",
                    fontSize = 12.sp,
                    color = Blue3,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable {
                        isExpanded = !isExpanded
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecommendationScreenPreview() {
    RecommendationScreen()
}