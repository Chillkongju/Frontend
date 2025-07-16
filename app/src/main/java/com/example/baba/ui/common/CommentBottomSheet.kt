package com.example.baba.ui.common

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.network.SessionManager
import com.example.baba.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 댓글 데이터 모델
data class Comment(
    val id: Int,
    val userName: String,
    val userProfileImage: Int = R.drawable.ic_default_profile,
    val content: String,
    val timeAgo: String,
    val isReply: Boolean = false,
    val parentCommentId: Int? = null,
    val mentionedUser: String? = null,
    var isLiked: Boolean = false,
    var likeCount: Int = 0
)

// 멘션 시각적 변환
class MentionVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            text = buildAnnotatedString {
                val input = text.text
                val words = input.split(" ")

                words.forEachIndexed { index, word ->
                    if (word.startsWith("@")) {
                        pushStyle(SpanStyle(color = Blue2))
                        append(word)
                        pop()
                    } else {
                        append(word)
                    }

                    if (index < words.size - 1) {
                        append(" ")
                    }
                }
            },
            offsetMapping = OffsetMapping.Identity
        )
    }
}

@Composable
fun CommentBottomSheet(
    diaryId: Long,
    comments: SnapshotStateList<Comment>,
    onDismiss: () -> Unit,
    onCommentAdded: (Comment) -> Unit = {},
    onCommentDeleted: (Comment) -> Unit = {},
    currentUserName: String,
    currentUserProfileImage: Int = R.drawable.ic_default_profile
) {
    var commentText by remember { mutableStateOf(TextFieldValue("")) }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // 답글 시작
    val startReply = { comment: Comment ->
        replyingTo = comment
        val mentionText = "@${comment.userName} "
        commentText = TextFieldValue(
            text = mentionText,
            selection = TextRange(mentionText.length)
        )
    }

    // 댓글/답글 전송
    val sendComment = {
        if (commentText.text.isNotBlank()) {
            val newComment = Comment(
                id = System.currentTimeMillis().toInt(),
                userName = currentUserName,
                userProfileImage = currentUserProfileImage,
                content = commentText.text,
                timeAgo = "지금",
                isReply = replyingTo != null,
                parentCommentId = if (replyingTo != null) {
                    if (replyingTo!!.isReply) {
                        replyingTo!!.parentCommentId
                    } else {
                        replyingTo!!.id
                    }
                } else null,
                mentionedUser = replyingTo?.userName
            )
            onCommentAdded(newComment)
            commentText = TextFieldValue("")
            replyingTo = null
        }
    }

    // 댓글 정렬 (메인 댓글 + 답글)
    val organizedComments by remember {
        derivedStateOf {
            val mainComments = comments.filter { !it.isReply }
            val replies = comments.filter { it.isReply }
            val result = mutableListOf<Comment>()

            mainComments.forEach { mainComment ->
                result.add(mainComment)
                val mainCommentReplies = replies.filter { it.parentCommentId == mainComment.id }
                    .sortedBy { it.id }
                result.addAll(mainCommentReplies)
            }
            result
        }
    }

    // 배경 투명도 계산
    val backgroundAlpha = (0.5f - (dragOffset / 1000f)).coerceAtLeast(0f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = backgroundAlpha))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = {
                        if (dragOffset > 150) {
                            onDismiss()
                        } else {
                            dragOffset = 0f
                        }
                        isDragging = false
                    }
                ) { _, dragAmount ->
                    if (dragAmount.y > 0) {
                        dragOffset = (dragOffset + dragAmount.y).coerceAtLeast(0f)
                    }
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .offset { IntOffset(0, dragOffset.toInt()) }
                .background(
                    Color.White,
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                )
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // 드래그 핸들
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        CoolGray300,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 답글 작성 헤더
            if (replyingTo != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CoolGray100
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${replyingTo!!.userName}님에게 답글 작성 중",
                            fontSize = 14.sp,
                            color = CoolGray500,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                replyingTo = null
                                commentText = TextFieldValue("")
                            }
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 16.sp,
                                color = CoolGray500
                            )
                        }
                    }
                }
            }

            // 댓글 리스트
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                if (organizedComments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 200.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "아직 댓글이 없습니다",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = CoolGray700
                            )
                            Text(
                                text = "댓글을 남겨보세요",
                                fontSize = 14.sp,
                                color = CoolGray500,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(organizedComments.size) { index ->
                        CommentItem(
                            comment = organizedComments[index],
                            onReplyClick = startReply,
                            onLikeClick = { comment ->
                                val commentIndex = comments.indexOfFirst { it.id == comment.id }
                                if (commentIndex != -1) {
                                    val updatedComment = comments[commentIndex].copy(
                                        isLiked = !comments[commentIndex].isLiked,
                                        likeCount = if (comments[commentIndex].isLiked)
                                            comments[commentIndex].likeCount - 1
                                        else
                                            comments[commentIndex].likeCount + 1
                                    )
                                    comments[commentIndex] = updatedComment
                                }
                            },
                            onDeleteClick = { comment ->
                                comments.removeIf { it.id == comment.id }
                                onCommentDeleted(comment)
                            }
                        )
                    }
                }
            }

            // 댓글 입력 영역
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = currentUserProfileImage),
                        contentDescription = "내 프로필",
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(CoolGray200)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text(
                                if (replyingTo != null) "답글 달기" else "댓글 달기",
                                color = CoolGray300,
                                fontSize = 14.sp
                            )
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        shape = RoundedCornerShape(30.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoolGray200,
                            unfocusedBorderColor = CoolGray200,
                            cursorColor = Blue2,
                            focusedTextColor = TextBlack,
                            unfocusedTextColor = TextBlack
                        ),
                        visualTransformation = MentionVisualTransformation(),
                        trailingIcon = {
                            if (commentText.text.isNotBlank()) {
                                IconButton(onClick = sendComment) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_comment_send),
                                        contentDescription = "전송",
                                        tint = Blue2,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: Comment,
    onReplyClick: (Comment) -> Unit = {},
    onLikeClick: (Comment) -> Unit = {},
    onDeleteClick: (Comment) -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (comment.isReply) 44.dp else 0.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
        // 프로필 이미지
        Image(
            painter = painterResource(id = comment.userProfileImage),
            contentDescription = "프로필",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(CoolGray200)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = comment.userName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CoolGray700
                    )

                    // 답글인 경우 멘션 표시
                    if (comment.isReply && comment.mentionedUser != null) {
                        val annotatedString = buildAnnotatedString {
                            pushStyle(SpanStyle(color = Blue2))
                            append("@${comment.mentionedUser} ")
                            pop()
                            append(comment.content.removePrefix("@${comment.mentionedUser} "))
                        }
                        Text(
                            text = annotatedString,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    } else {
                        Text(
                            text = comment.content,
                            fontSize = 14.sp,
                            color = TextBlack,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = comment.timeAgo,
                            fontSize = 12.sp,
                            color = CoolGray300
                        )

                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "더보기",
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showDropdownMenu = true },
                            tint = CoolGray500
                        )
                    }

                    if (showDropdownMenu) {
                        DropdownMenu(
                            expanded = showDropdownMenu,
                            onDismissRequest = { showDropdownMenu = false },
                            modifier = Modifier
                                .width(60.dp)
                                .background(
                                    Color.White,
                                    shape = RoundedCornerShape(6.dp)
                                )
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "삭제",
                                        fontSize = 12.sp,
                                        color = Red1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false

                                    // API 호출로 실제 댓글 삭제
                                    coroutineScope.launch {
                                        try {
                                            val username = try {
                                                val memberResponse = RetrofitInstance.memberApi.getMyInfo()
                                                if (memberResponse.isSuccessful) {
                                                    memberResponse.body()?.username
                                                } else {
                                                    SessionManager.username
                                                }
                                            } catch (e: Exception) {
                                                SessionManager.username
                                            }

                                            if (username != null) {
                                                Log.d("CommentItem", "댓글 삭제 시작 - commentId: ${comment.id}")
                                                val response = RetrofitInstance.commentApi.deleteComment(
                                                    username = username,
                                                    commentId = comment.id.toLong()
                                                )

                                                if (response.isSuccessful) {
                                                    Log.d("CommentItem", "댓글 삭제 성공")
                                                    onDeleteClick(comment)
                                                } else {
                                                    Log.e("CommentItem", "댓글 삭제 실패: ${response.code()}")
                                                    withContext(Dispatchers.Main) {
                                                        Toast.makeText(context, "댓글 삭제에 실패했습니다", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            } else {
                                                Log.e("CommentItem", "username을 가져올 수 없음")
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "사용자 정보를 찾을 수 없습니다", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            Log.e("CommentItem", "댓글 삭제 오류: ${e.message}")
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "네트워크 오류가 발생했습니다", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                        Icon(
                            painter = painterResource(
                                id = if (comment.isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
                            ),
                            contentDescription = "좋아요",
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onLikeClick(comment) },
                            tint = if (comment.isLiked) Red1 else CoolGray500
                        )
                    }

                    if (comment.likeCount > 0) {
                        Text(
                            text = comment.likeCount.toString(),
                            fontSize = 12.sp,
                            color = CoolGray500
                        )
                    }
                }

                CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "답글",
                        modifier = Modifier
                            .size(14.dp)
                            .clickable { onReplyClick(comment) }
                    )
                }
            }
        }
    }
}