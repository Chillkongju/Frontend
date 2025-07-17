package com.example.baba

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import com.example.baba.data.network.PersistentSessionManager
import com.example.baba.data.network.SessionManager
import com.example.baba.data.record.WatchedDateManager
import com.example.baba.ui.profile.EditProfileScreen
import com.example.baba.ui.record.RecordDetailScreen
import com.example.baba.ui.record.Record
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.navOptions


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 초기화
        RetrofitInstance.init(applicationContext)
        PersistentSessionManager.initialize(applicationContext)
        WatchedDateManager.initialize(applicationContext)

        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var isSignUp by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(false) }
            var isCheckingSession by remember { mutableStateOf(true) }

            // 앱 시작 시 저장된 세션 확인
            LaunchedEffect(Unit) {
                // 스플래시 화면 시간
                kotlinx.coroutines.delay(3000)
                showSplash = false

                // 저장된 로그인 상태 확인
                val sessionExists = PersistentSessionManager.isLoggedIn()
                isLoggedIn = sessionExists

                // 디버깅 로그
                Log.d("MainActivity", "세션 확인 결과: $sessionExists")
                Log.d("MainActivity", "SessionManager - userId: ${SessionManager.userId}, userName: ${SessionManager.userName}, username: ${SessionManager.username}")

                isCheckingSession = false
            }

            when {
                showSplash || isCheckingSession -> {
                    Splash { /* 스플래시 완료는 LaunchedEffect에서 처리 */ }
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

                else -> MainScreen(
                    onLogout = {
                        isLoggedIn = false
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    // 현재 경로 추적
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute ?: Screen.Home.route, // 현재 경로 전달
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(navController = navController)
            }

            composable(Screen.Record.route) {
                RecordNavigation(onLogout = onLogout)
            }

            composable(Screen.Recommendation.route) {
                RecommendationScreen()
            }

            composable(Screen.Friends.route) {
                FriendsScreen()
            }
        }
    }
}

@Composable
fun RecordNavigation(onLogout: () -> Unit) {
    val recordNavController = rememberNavController()

    NavHost(
        navController = recordNavController,
        startDestination = "recordList"
    ) {
        composable("editProfile") {
            EditProfileScreen(recordNavController)
        }

        composable("recordList") {
            MyRecordScreen(
                navController = recordNavController,
                onLogout = onLogout
            )
        }

        composable("recordDetail") {
            val record = recordNavController
                .previousBackStackEntry
                ?.savedStateHandle
                ?.get<Record>("record")

            record?.let {
                RecordDetailScreen(
                    record = it,
                    navController = recordNavController
                )
            }
        }
    }
}

@Composable
fun MainScreenPreview() {
    BABATheme {
        MainScreen(onLogout = {})
    }
}