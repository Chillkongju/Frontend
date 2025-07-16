package com.example.baba.ui.record

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Record(
    val id: Long,
    val title: String,
    val date: String,
    val category: String,
    val rating: Float,
    val content: String,
    val isPublic: Boolean,
    val photoUri: Uri? = null
) : Parcelable
