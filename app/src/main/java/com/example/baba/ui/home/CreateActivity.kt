package com.example.baba.ui.home

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import com.example.baba.ui.theme.BABATheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CreateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 인텐트로부터 카테고리/날짜 받기
        val initialCategory = intent.getStringExtra("category") ?: "공연"
        val initialDate = intent.getStringExtra("selectedDate")
            ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            ?: LocalDate.now()

        setContent {
            BABATheme {
                // 단계 전환용 상태
                var showDetail by remember { mutableStateOf(false) }

                // 1단계 입력값
                var title by remember { mutableStateOf("") }
                var rating by remember { mutableStateOf(0f) }

                // 2단계 입력값
                var review by remember { mutableStateOf("") }
                var location by remember { mutableStateOf("") }
                var isPublic by remember { mutableStateOf(true) }
                var includeSpoiler by remember { mutableStateOf(false) }
                val photos = remember { mutableStateListOf<Uri>() }

                if (!showDetail) {
                    // ① 제목/별점 입력
                    CreateScreen(
                        selectedDate   = initialDate,
                        title          = title,
                        rating         = rating,
                        onBack         = { finish() },
                        onTitleChange  = { title = it },
                        onRatingChange = { rating = it },
                        onNext         = { showDetail = true }
                    )
                } else {
                    // ② 세부정보 입력
                    CreateDetailScreen(
                        category        = initialCategory,
                        date            = initialDate,
                        title           = title,
                        rating          = rating,
                        review          = review,
                        onReviewChange  = { review = it },
                        location        = location,
                        onLocationChange= { location = it },
                        isPublic        = isPublic,
                        onPublicChange  = { isPublic = it },
                        includeSpoiler  = includeSpoiler,
                        onSpoilerChange = { includeSpoiler = it },
                        photos          = photos,
                        onPhotosChange = { photos.clear(); photos.addAll(it) },
                        onRemovePhoto   = { idx -> photos.removeAt(idx) },
                        onBack          = { finish() },
                        onSave          = { /* TODO: 저장 로직 + finish() */ finish()  }
                    )
                }
            }
        }
    }
}
