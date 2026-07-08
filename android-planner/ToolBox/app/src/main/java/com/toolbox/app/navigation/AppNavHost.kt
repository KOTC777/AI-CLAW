package com.toolbox.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.toolbox.feature.checkin.ui.CheckinScreen
import com.toolbox.feature.inspiration.ui.NoteEditScreen
import com.toolbox.feature.inspiration.ui.NoteListScreen
import com.toolbox.feature.inspiration.ui.TemplatePickerScreen
import com.toolbox.feature.memo.ui.MemoEditScreen
import com.toolbox.feature.memo.ui.MemoListScreen
import com.toolbox.feature.password.ui.PasswordEditScreen
import com.toolbox.feature.password.ui.PasswordListScreen
import com.toolbox.feature.schedule.ui.CalendarScreen
import com.toolbox.feature.schedule.ui.EventEditScreen

sealed class Screen(val route: String) {
    data object Memo : Screen("memo")
    data object MemoEdit : Screen("memo_edit?memoId={memoId}") {
        fun createRoute(memoId: String? = null): String =
            if (memoId != null) "memo_edit?memoId=$memoId" else "memo_edit"
    }
    data object Schedule : Screen("schedule")
    data object EventEdit : Screen("event_edit?eventId={eventId}&date={date}") {
        fun createRoute(eventId: String? = null, date: Long? = null): String {
            val base = if (eventId != null) "event_edit?eventId=$eventId" else "event_edit"
            return if (date != null) "$base&date=$date" else base
        }
    }
    data object Checkin : Screen("checkin")
    data object Password : Screen("password")
    data object PasswordEdit : Screen("password_edit?entryId={entryId}") {
        fun createRoute(entryId: String? = null): String =
            if (entryId != null) "password_edit?entryId=$entryId" else "password_edit"
    }
    data object Inspiration : Screen("inspiration")
    data object InspirationTemplates : Screen("inspiration_templates")
    data object InspirationEdit : Screen("inspiration_edit?noteId={noteId}&templateId={templateId}") {
        fun createRoute(noteId: String? = null, templateId: String? = null): String {
            val params = mutableListOf<String>()
            noteId?.let { params.add("noteId=$it") }
            templateId?.let { params.add("templateId=$it") }
            return if (params.isEmpty()) "inspiration_edit"
            else "inspiration_edit?${params.joinToString("&")}"
        }
    }
}

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Memo.route,
        modifier = modifier
    ) {
        // Memo
        composable(Screen.Memo.route) {
            MemoListScreen(onNavigateToEdit = { navController.navigate(Screen.MemoEdit.createRoute(it)) })
        }
        composable(
            route = Screen.MemoEdit.route,
            arguments = listOf(navArgument("memoId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            MemoEditScreen(memoId = it.arguments?.getString("memoId"), onNavigateBack = { navController.popBackStack() })
        }

        // Schedule
        composable(Screen.Schedule.route) {
            CalendarScreen(
                onCreateEvent = { navController.navigate(Screen.EventEdit.createRoute()) },
                onEventClick = { navController.navigate(Screen.EventEdit.createRoute(it)) }
            )
        }
        composable(
            route = Screen.EventEdit.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("date") { type = NavType.LongType; defaultValue = 0L }
            )
        ) {
            EventEditScreen(
                eventId = it.arguments?.getString("eventId"),
                initialDate = it.arguments?.getLong("date")?.takeIf { d -> d > 0 },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // Checkin
        composable(Screen.Checkin.route) { CheckinScreen() }

        // Password
        composable(Screen.Password.route) { PasswordListScreen() }
        composable(
            route = Screen.PasswordEdit.route,
            arguments = listOf(navArgument("entryId") { type = NavType.StringType; nullable = true; defaultValue = null })
        ) {
            PasswordEditScreen(entryId = it.arguments?.getString("entryId"), onNavigateBack = { navController.popBackStack() })
        }

        // Inspiration
        composable(Screen.Inspiration.route) {
            NoteListScreen(
                onNavigateToEdit = { navController.navigate(Screen.InspirationEdit.createRoute(noteId = it)) },
                onNavigateToTemplates = { navController.navigate(Screen.InspirationTemplates.route) }
            )
        }
        composable(Screen.InspirationTemplates.route) {
            TemplatePickerScreen(
                onSelectTemplate = { templateId ->
                    navController.navigate(Screen.InspirationEdit.createRoute(templateId = templateId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.InspirationEdit.route,
            arguments = listOf(
                navArgument("noteId") { type = NavType.StringType; nullable = true; defaultValue = null },
                navArgument("templateId") { type = NavType.StringType; nullable = true; defaultValue = null }
            )
        ) {
            NoteEditScreen(
                noteId = it.arguments?.getString("noteId"),
                templateId = it.arguments?.getString("templateId"),
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
