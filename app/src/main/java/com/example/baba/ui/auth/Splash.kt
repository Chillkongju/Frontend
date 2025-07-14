package com.example.baba.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.baba.R
import kotlinx.coroutines.delay

@Composable
fun Splash(onFinished: () -> Unit) {
    var shrinkLogo by remember { mutableStateOf(false) }
    var showMainLogo by remember { mutableStateOf(false) }

    // 크기 애니메이션 (sub_logo)
    val scale by animateFloatAsState(
        targetValue = if (shrinkLogo) 0.5f else 1.5f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "scale"
    )

    // 투명도 애니메이션 (main_logo)
    val alpha by animateFloatAsState(
        targetValue = if (showMainLogo) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "alpha"
    )

    LaunchedEffect(Unit) {
        delay(1000)
        shrinkLogo = true
        delay(800)
        showMainLogo = true
        delay(1000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        // sub_logo
        if (!showMainLogo) {
            Image(
                painter = painterResource(id = R.drawable.sub_logo),
                contentDescription = "Sub Logo",
                modifier = Modifier
                    .size(230.dp)
                    .scale(scale)
            )
        }

        // main_logo
        Image(
            painter = painterResource(id = R.drawable.main_logo),
            contentDescription = "Main Logo",
            modifier = Modifier
                .size(180.dp)
                .alpha(alpha)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SplashPreview() {
    Splash(onFinished = {})
}

