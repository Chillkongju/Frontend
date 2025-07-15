package com.example.baba

import android.content.Context
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
import androidx.compose.ui.tooling.preview.Preview
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        setContent {
            var showSplash by remember { mutableStateOf(true) }
            var isSignUp by remember { mutableStateOf(false) }
            var isLoggedIn by remember { mutableStateOf(!token.isNullOrEmpty()) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                Screen.Home.route -> HomeScreen()
                Screen.Record.route -> MyRecordScreen()
                Screen.Recommendation.route -> RecommendationScreen()
                Screen.Friends.route -> FriendsScreen()
            }
        }
    }
}

//@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    BABATheme {
        MainScreen()
    }
}