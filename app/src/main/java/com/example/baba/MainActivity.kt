package com.example.baba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.draw.alpha
import com.example.baba.ui.auth.LoginScreen
import com.example.baba.ui.auth.SignupScreen
import com.example.baba.ui.auth.Splash
import com.example.baba.ui.common.BottomNavigationBar
import com.example.baba.ui.home.HomeScreen
import com.example.baba.ui.record.MyRecordScreen
import com.example.baba.ui.recommendation.RecommendationScreen
import com.example.baba.ui.friends.FriendsScreen
import com.example.baba.ui.theme.BABATheme
import com.example.baba.ui.common.Screen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.baba.data.network.RetrofitInstance
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.profile.EditProfileScreen
import com.example.baba.ui.record.RecordDetailScreen
import com.example.baba.ui.record.Record
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitInstance.init(applicationContext)

        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var isSignUp by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(false) }

            when {
                showSplash -> Splash {
                    showSplash = false
                }

                isSignUp -> SignupScreen(
                    onSignupComplete = {
                        isSignUp = false
                    }
                )

                !isLoggedIn -> LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn = true
                    },
                    onSignupClick = {
                        isSignUp = true
                    }
                )

                else -> MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentRoute by remember { mutableStateOf(Screen.Home.route) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onItemClick = { route ->
                    currentRoute = route
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 모든 화면을 항상 렌더링하되, 보이기/숨기기만 제어
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (currentRoute == Screen.Home.route) 1f else 0f)
            ) {
                HomeScreen()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (currentRoute == Screen.Record.route) 1f else 0f)
            ) {
                AppNavigation()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (currentRoute == Screen.Recommendation.route) 1f else 0f)
            ) {
                RecommendationScreen()
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (currentRoute == Screen.Friends.route) 1f else 0f)
            ) {
                FriendsScreen()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "recordList") {
        composable("editProfile") {
            EditProfileScreen(navController)
        }

        composable("recordList") {
            MyRecordScreen(navController = navController)
        }

        composable("recordDetail") {
            val record = navController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.get<Record>("record")

            record?.let {
                val actualWatchedDate = WatchedDateManager.getWatchedDate(record.id)
                    ?: try {
                        when {
                            it.date.contains(".") -> {
                                val dateStr = it.date.replace(".", "-").removeSuffix("-")
                                LocalDate.parse(dateStr)
                            }
                            it.date.contains("-") -> {
                                LocalDate.parse(it.date)
                            }
                            else -> LocalDate.now()
                        }
                    } catch (e: Exception) {
                        LocalDate.now()
                    }

                RecordDetailScreen(
                    title = it.title,
                    date = actualWatchedDate,
                    rating = it.rating,
                    content = it.content,
                    isPublic = it.isPublic,
                    category = it.category,
                    photoUri = it.photoUri,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun MainScreenPreview() {
    BABATheme {
        MainScreen()
    }
}