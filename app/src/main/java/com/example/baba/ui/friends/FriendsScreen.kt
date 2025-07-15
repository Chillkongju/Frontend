package com.example.baba.ui.friends

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baba.R
import com.example.baba.ui.theme.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.toMutableStateList
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.unit.IntOffset


// 더미 데이터 모델
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

data class FeedPost(
    val id: Int,
    val userName: String,
    val userProfileImage: Int,
    val timeAgo: String,
    val date: String,
    val contentTitle: String,
    val contentCategory: String,
    val contentYear: String,
    val contentImage: Int,
    val rating: Float,
    val reviewText: String,
    val likes: Int,
    val comments: Int
)

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

@Preview(showBackground = true)
@Composable
fun FriendsScreen(navController: NavController? = null) {
    var showCommentModal by remember { mutableStateOf(false) }
    var selectedPost by remember { mutableStateOf<FeedPost?>(null) }
    val commentCounts = remember { mutableStateMapOf<Int, Int>() }
    val postComments = remember { mutableStateMapOf<Int, SnapshotStateList<Comment>>() }
    val postLikeStates = remember { mutableStateMapOf<Int, Boolean>() }
    val postLikeCounts = remember { mutableStateMapOf<Int, Int>() }

    // 더미 데이터
    val feedPosts = listOf(
        FeedPost(
            id = 1,
            userName = "호두왕자",
            userProfileImage = R.drawable.ic_default_profile,
            timeAgo = "2분 전",
            date = "2025.07.14.",
            contentTitle = "알라딘",
            contentCategory = "영화",
            contentYear = "2019",
            contentImage = R.drawable.friend_movie_poster_1,
            rating = 4.0f,
            reviewText = "4dx로 봤는데 영화관 향기가 좋았음 디즈니 실사화 중 제일 맘에 들었음",
            likes = 10,
            comments = 10
        ),
        FeedPost(
            id = 2,
            userName = "쑤인",
            userProfileImage = R.drawable.ic_default_profile,
            timeAgo = "8분 전",
            date = "2025.07.08.",
            contentTitle = "해피엔드",
            contentCategory = "영화",
            contentYear = "2024",
            contentImage = R.drawable.friend_movie_poster_2,
            rating = 4.5f,
            reviewText = "재밌다.. 연출 넘 좋음",
            likes = 10,
            comments = 10
        ),
        FeedPost(
            id = 3,
            userName = "시새린",
            userProfileImage = R.drawable.ic_default_profile,
            timeAgo = "2시간 전",
            date = "2025.07.01.",
            contentTitle = "마침내 멸망하는 여름(스페셜 에디션)",
            contentCategory = "도서",
            contentYear = "2024",
            contentImage = R.drawable.friend_book_poster_1,
            rating = 4.5f,
            reviewText = "읽는 내내 꿈속에서 살고 있는 듯한 느낌이 들었다",
            likes = 2,
            comments = 10
        ),
        FeedPost(
            id = 4,
            userName = "mmmuuu",
            userProfileImage = R.drawable.ic_default_profile,
            timeAgo = "9시간 전",
            date = "2025.06.29.",
            contentTitle = "데스노트",
            contentCategory = "공연",
            contentYear = "2023",
            contentImage = R.drawable.friend_show_poster_1,
            rating = 4.5f,
            reviewText = "읽는 내내 꿈속에서 살고 있는 듯한 느낌이 들었다",
            likes = 2,
            comments = 10
        )
    )

    LaunchedEffect(feedPosts) {
        feedPosts.forEach { post ->
            if (!commentCounts.containsKey(post.id)) {
                commentCounts[post.id] = post.comments
            }
            if (!postComments.containsKey(post.id)) {
                postComments[post.id] = mutableListOf<Comment>().toMutableStateList()
            }
            if (!postLikeStates.containsKey(post.id)) {
                postLikeStates[post.id] = false
            }
            if (!postLikeCounts.containsKey(post.id)) {
                postLikeCounts[post.id] = post.likes
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "피드",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextBlack
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            modifier = Modifier.size(24.dp),
                            tint = CoolGray700
                        )
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            modifier = Modifier.size(24.dp),
                            tint = CoolGray700
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                item {
                    Spacer(modifier = Modifier.height(1.dp))
                }

                itemsIndexed(feedPosts) { index, post ->
                    FeedPostCard(
                        post = post.copy(
                            comments = commentCounts[post.id] ?: post.comments,
                        ),
                        isLiked = postLikeStates[post.id] ?: false,
                        likeCount = postLikeCounts[post.id] ?: post.likes,
                        onDetailClick = {
                            navController?.navigate("friendsRecordDetails/${post.id}")
                        },
                        onCommentClick = {
                            selectedPost = post
                            showCommentModal = true
                        },
                        onLikeClick = { postId ->
                            val currentLiked = postLikeStates[postId] ?: false
                            val currentCount = postLikeCounts[postId] ?: post.likes

                            postLikeStates[postId] = !currentLiked
                            postLikeCounts[postId] = if (currentLiked) currentCount - 1 else currentCount + 1
                        }
                    )

                    if (index < feedPosts.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 20.dp),
                            thickness = 1.dp,
                            color = CoolGray100
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }

        if (showCommentModal && selectedPost != null) {
            CommentBottomSheet(
                post = selectedPost!!,
                comments = postComments[selectedPost!!.id] ?: mutableListOf<Comment>().toMutableStateList(),
                onDismiss = { showCommentModal = false },
                onCommentAdded = { comment ->
                    val postId = selectedPost!!.id
                    if (postComments[postId] == null) {
                        postComments[postId] = mutableListOf<Comment>().toMutableStateList()
                    }
                    postComments[postId]?.add(comment)
                    commentCounts[postId] = (commentCounts[postId] ?: selectedPost!!.comments) + 1
                },
                onCommentDeleted = {
                    val postId = selectedPost!!.id
                    commentCounts[postId] = (commentCounts[postId] ?: 0) - 1
                }
            )
        }
    }
}

@Composable
fun FeedPostCard(
    post: FeedPost,
    isLiked: Boolean,
    likeCount: Int,
    modifier: Modifier = Modifier,
    onDetailClick: () -> Unit = {},
    onCommentClick: () -> Unit = {},
    onLikeClick: (Int) -> Unit = {}
) {

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onDetailClick() }
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 사용자 프로필
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = post.userProfileImage),
                    contentDescription = "${post.userName} 프로필",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = CoolGray150,
                            shape = CircleShape
                        )
                        .background(CoolGray200),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Text(
                        text = post.userName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CoolGray700
                    )
                    Text(
                        text = post.timeAgo,
                        fontSize = 12.sp,
                        color = CoolGray300
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "더보기",
                modifier = Modifier.size(20.dp),
                tint = CoolGray500
            )
        }

        // 친구 피드 콘텐츠
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = post.contentImage),
                contentDescription = post.contentTitle,
                modifier = Modifier
                    .width(68.dp)
                    .height(100.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(CoolGray200),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = post.contentTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextBlack,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${post.contentCategory} ${post.contentYear}",
                    fontSize = 12.sp,
                    color = CoolGray500
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 별점
            Box(
                modifier = Modifier
                    .width(68.dp)
                    .height(30.dp)
                    .background(
                        color = CoolGray100,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rating_button_star),
                        contentDescription = "별점",
                        modifier = Modifier.size(13.dp),
                        tint = Blue2
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = post.rating.toString(),
                        fontSize = 13.sp,
                        color = Blue2,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 날짜
            Text(
                text = post.date,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CoolGray500
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {

            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(CoolGray300)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = post.reviewText,
                    fontSize = 14.sp,
                    color = CoolGray700,
                    lineHeight = 20.sp,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis
                )

                if (post.reviewText.length > 50) {
                    Text(
                        text = "더보기",
                        fontSize = 12.sp,
                        color = Blue4,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable { onDetailClick() }
                    )
                }
            }
        }

        // 좋아요 및 댓글
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                    Icon(
                        painter = painterResource(
                            id = if (isLiked) R.drawable.ic_like_filled else R.drawable.ic_like_outline
                        ),
                        contentDescription = "좋아요",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                onLikeClick(post.id)
                            }
                    )
                }

                Text(
                    text = likeCount.toString(),
                    fontSize = 12.sp,
                    color = CoolGray700
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompositionLocalProvider(LocalContentColor provides Color.Unspecified) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_comment),
                        contentDescription = "댓글",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCommentClick() }
                    )
                }
                Text(
                    text = post.comments.toString(),
                    fontSize = 12.sp,
                    color = CoolGray700
                )
            }
        }
    }
}

@Composable
fun CommentBottomSheet(
    post: FeedPost,
    comments: SnapshotStateList<Comment>,
    onDismiss: () -> Unit,
    onCommentAdded: (Comment) -> Unit = {},
    onCommentDeleted: () -> Unit = {}
) {
    var commentText by remember { mutableStateOf(TextFieldValue("")) }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // 답글
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
                userName = "6812",
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
//                .clickable(enabled = false) { }
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        CoolGray300,
                        shape = RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
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
                                onCommentDeleted()
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
                        painter = painterResource(id = R.drawable.ic_default_profile),
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (comment.isReply) 44.dp else 0.dp,
                top = 8.dp,
                bottom = 8.dp
            )
    ) {
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
                                    onDeleteClick(comment)
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