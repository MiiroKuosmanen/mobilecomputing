package com.example.mobcomp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.mobcomp.ui.theme.MobcompTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mobcomp.ui.theme.HomeScreen
import com.example.mobcomp.ui.theme.InfoScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MobcompTheme {
                NavigationAppHost()
            }
        }
    }
}

@Composable
fun NavigationAppHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("info") {
            InfoScreen(onNavigateToHome = {
                navController.navigate("home") {
                    popUpTo("home") {
                        inclusive = true
                    }
                }
            }
            )
        }
        composable("home") {
            HomeScreen(onNavigateToInfo = {
                    navController.navigate("info")
                }
            )
        }
    }
}

