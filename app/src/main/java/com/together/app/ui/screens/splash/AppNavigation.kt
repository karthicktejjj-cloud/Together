package com.together.app.navigation


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.together.app.ui.screens.home.HomeScreen
import com.together.app.ui.screens.splash.SplashScreen
import com.together.app.ui.screens.welcome.WelcomeScreen
import com.together.app.ui.screens.login.LoginScreen
import com.together.app.videos.VideoListScreen
import com.together.app.ui.screens.player.VideoPlayerScreen
import com.together.app.ui.screens.videos.VideoDetailsScreen
import com.together.app.ui.screens.room.CreateRoomScreen
import com.together.app.ui.screens.room.WaitingRoomScreen
import com.together.app.ui.screens.room.NearbyRoomsScreen
import com.together.app.ui.screens.room.JoinRoomScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.together.app.viewmodel.VideoViewModel
import com.together.app.viewmodel.RoomViewModel


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val videoViewModel: VideoViewModel = viewModel()
    val roomViewModel: RoomViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
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
            VideoListScreen(navController, videoViewModel)
        }

        composable(
            route = "details/{videoId}",
            arguments = listOf(
                navArgument("videoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            VideoDetailsScreen(navController, videoViewModel, roomViewModel, videoId)
        }

        composable("create_room/{videoId}",
            arguments = listOf(
                navArgument("videoId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val videoId = backStackEntry.arguments?.getLong("videoId") ?: 0L
            CreateRoomScreen(navController, videoViewModel, roomViewModel, videoId)
        }

        composable("nearby_rooms") {
            NearbyRoomsScreen(navController)
        }

        composable(
            route = "join_room/{roomCode}/{ipAddress}/{port}",
            arguments = listOf(
                navArgument("roomCode") { type = NavType.StringType },
                navArgument("ipAddress") { type = NavType.StringType },
                navArgument("port") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode") ?: ""
            val ipAddress = backStackEntry.arguments?.getString("ipAddress") ?: ""
            val port = backStackEntry.arguments?.getInt("port") ?: 0
            JoinRoomScreen(navController, roomViewModel, roomCode, ipAddress, port)
        }

        composable(
            route = "waiting_room/{roomId}",
            arguments = listOf(
                navArgument("roomId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            WaitingRoomScreen(navController, roomViewModel, roomId)
        }


        composable(
            route = "player/{videoUri}",
            arguments = listOf(
                navArgument("videoUri") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val uri = backStackEntry.arguments?.getString("videoUri") ?: ""
            VideoPlayerScreen(uri, roomViewModel)
        }
    }
}