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
            id = 3,
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
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
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