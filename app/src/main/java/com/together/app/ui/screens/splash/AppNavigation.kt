package com.together.app.navigation


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.together.app.ui.screens.home.HomeScreen
import com.together.app.ui.screens.splash.SplashScreen
import com.together.app.ui.screens.welcome.WelcomeScreen
import com.together.app.ui.screens.login.LoginScreen
import com.together.app.videos.VideoListScreen
import com.together.app.ui.screens.player.VideoPlayerScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {

        composable("splash") {
            SplashScreen(navController)
        }

        composable("welcome") {
            WelcomeScreen(navController)
        }

        composable("login") {
            LoginScreen(navController)
        }

        composable("home") {
            HomeScreen(navController)
        }

        composable("videos") {
            VideoListScreen(navController)
        }


        composable(
            route = "player/{videoUri}",
            arguments = listOf(
                navArgument("videoUri") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val uri =
                backStackEntry.arguments?.getString("videoUri") ?: ""

            VideoPlayerScreen(uri)
        }
    }
}