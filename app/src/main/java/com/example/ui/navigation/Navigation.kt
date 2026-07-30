package com.example.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.viewmodel.*

object Routes {
    const val AUTH = "auth"
    const val HOME = "home"
    const val CHAT = "chat/{conversationId}"
    const val VOICE_CALL = "voice_call/{name}/{avatar}/{id}"
    const val VIDEO_CALL = "video_call/{name}/{avatar}/{id}"
    const val PROFILE = "profile"
    const val NOTIFICATIONS = "notifications"
    const val SETTINGS = "settings"

    fun buildChatRoute(conversationId: String) = "chat/$conversationId"

    fun buildVoiceCallRoute(name: String, avatar: String, id: String) =
        "voice_call/${Uri.encode(name)}/${Uri.encode(avatar)}/$id"

    fun buildVideoCallRoute(name: String, avatar: String, id: String) =
        "video_call/${Uri.encode(name)}/${Uri.encode(avatar)}/$id"
}

@Composable
fun ConnectXAppNavigation(
    authViewModel: AuthViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    callViewModel: CallViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel(),
    notificationViewModel: NotificationViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    val startDestination = if (isLoggedIn) Routes.HOME else Routes.AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                authViewModel = authViewModel,
                onAuthSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                homeViewModel = homeViewModel,
                onConversationClick = { id ->
                    navController.navigate(Routes.buildChatRoute(id))
                },
                onVoiceCallClick = { name, avatar, id ->
                    navController.navigate(Routes.buildVoiceCallRoute(name, avatar, id))
                },
                onVideoCallClick = { name, avatar, id ->
                    navController.navigate(Routes.buildVideoCallRoute(name, avatar, id))
                },
                onNotificationClick = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onProfileClick = {
                    navController.navigate(Routes.PROFILE)
                },
                onSettingsClick = {
                    navController.navigate(Routes.SETTINGS)
                }
            )
        }

        composable(
            route = Routes.CHAT,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: "conv_sarah"
            ChatScreen(
                conversationId = conversationId,
                chatViewModel = chatViewModel,
                onBackClick = { navController.popBackStack() },
                onVoiceCallClick = { name, avatar, id ->
                    navController.navigate(Routes.buildVoiceCallRoute(name, avatar, id))
                },
                onVideoCallClick = { name, avatar, id ->
                    navController.navigate(Routes.buildVideoCallRoute(name, avatar, id))
                }
            )
        }

        composable(
            route = Routes.VOICE_CALL,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("avatar") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "Sarah Connor")
            val avatar = Uri.decode(backStackEntry.arguments?.getString("avatar") ?: "")
            val id = backStackEntry.arguments?.getString("id") ?: "user_sarah"

            VoiceCallScreen(
                participantName = name,
                participantAvatar = avatar,
                participantId = id,
                callViewModel = callViewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.VIDEO_CALL,
            arguments = listOf(
                navArgument("name") { type = NavType.StringType },
                navArgument("avatar") { type = NavType.StringType },
                navArgument("id") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val name = Uri.decode(backStackEntry.arguments?.getString("name") ?: "Sarah Connor")
            val avatar = Uri.decode(backStackEntry.arguments?.getString("avatar") ?: "")
            val id = backStackEntry.arguments?.getString("id") ?: "user_sarah"

            VideoCallScreen(
                participantName = name,
                participantAvatar = avatar,
                participantId = id,
                callViewModel = callViewModel,
                onEndCall = { navController.popBackStack() }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                notificationViewModel = notificationViewModel,
                onBackClick = { navController.popBackStack() },
                onNotificationTargetClick = { convId ->
                    navController.navigate(Routes.buildChatRoute(convId))
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() },
                onLogoutClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
