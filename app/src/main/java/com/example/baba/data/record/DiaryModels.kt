package com.example.baba.data.record

data class DiaryCreateRequest(
    val title: String,
    val content: String,
    val category: String,
    val rating: Double,
    val watchedAt: String,
    val imageUrls: List<String>
    // val public: Boolean,
    // val spoiler: Boolean,
)