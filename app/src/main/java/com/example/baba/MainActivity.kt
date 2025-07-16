package com.example.baba

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.example.baba.data.network.SessionManager
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
            var isLoggedIn by remember { mutableStateOf(SessionManager.userId != null) }

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

                else -> MainScreen(
                    onLogout = {
                        isLoggedIn = false  // ← 로그아웃 콜백
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(onLogout: () -> Unit) {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavigationBar(
                currentRoute = navController.currentDestination?.route ?: Screen.Home.route,
                onItemClick = { route ->
                    navController.navigate(route) {
                        // 백스택 정리
                        popUpTo(navController.graph.startDestinationId) {
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
                RecordNavigation(onLogout = onLogout)  // ← 콜백 전달
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
                onLogout = onLogout  // ← 콜백 전달
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