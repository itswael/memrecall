package com.memrecall.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.memrecall.ui.screens.home.HomeScreen
import com.memrecall.ui.screens.subject.SubjectDetailScreen
import com.memrecall.ui.screens.subject.AddEditSubjectScreen
import com.memrecall.ui.screens.card.AddEditCardScreen
import com.memrecall.ui.screens.study.StudySessionScreen
import com.memrecall.ui.screens.study.SessionResultScreen
import com.memrecall.ui.screens.settings.SettingsScreen
import com.memrecall.ui.screens.stats.StatsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object SubjectDetail : Screen("subject/{subjectId}") {
        fun createRoute(id: Long) = "subject/$id"
    }
    object AddSubject : Screen("add_subject")
    object EditSubject : Screen("edit_subject/{subjectId}") {
        fun createRoute(id: Long) = "edit_subject/$id"
    }
    object AddCard : Screen("add_card/{subjectId}/{cardType}") {
        fun createRoute(subjectId: Long, cardType: String = "GENERAL") = "add_card/$subjectId/$cardType"
    }
    object EditCard : Screen("edit_card/{cardId}") {
        fun createRoute(id: Long) = "edit_card/$id"
    }
    object StudySession : Screen("study/{subjectIds}/{mode}") {
        fun createRoute(subjectIds: String, mode: String = "REVISION") = "study/$subjectIds/$mode"
    }
    object SessionResult : Screen("result/{sessionId}") {
        fun createRoute(id: Long) = "result/$id"
    }
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

@Composable
fun MemRecallNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(
            Screen.SubjectDetail.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
        ) {
            SubjectDetailScreen(navController = navController)
        }

        composable(Screen.AddSubject.route) {
            AddEditSubjectScreen(navController = navController, subjectId = null)
        }

        composable(
            Screen.EditSubject.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.LongType })
        ) {
            AddEditSubjectScreen(navController = navController, subjectId = it.arguments?.getLong("subjectId"))
        }

        composable(
            Screen.AddCard.route,
            arguments = listOf(
                navArgument("subjectId") { type = NavType.LongType },
                navArgument("cardType") { type = NavType.StringType }
            )
        ) {
            AddEditCardScreen(navController = navController, cardId = null)
        }

        composable(
            Screen.EditCard.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) {
            AddEditCardScreen(navController = navController, cardId = it.arguments?.getLong("cardId"))
        }

        composable(
            Screen.StudySession.route,
            arguments = listOf(
                navArgument("subjectIds") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType }
            )
        ) {
            StudySessionScreen(navController = navController)
        }

        composable(
            Screen.SessionResult.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.LongType })
        ) {
            SessionResultScreen(navController = navController)
        }

        composable(Screen.Stats.route) {
            StatsScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}
