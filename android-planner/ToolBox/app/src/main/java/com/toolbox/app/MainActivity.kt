package com.toolbox.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.toolbox.app.navigation.AppNavHost
import com.toolbox.app.navigation.Screen
import com.toolbox.app.ui.SetupScreen
import com.toolbox.core.datastore.DataStoreManager
import com.toolbox.core.security.crypto.SecurityManager
import com.toolbox.core.ui.theme.AppTheme
import com.toolbox.core.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                MainApp()
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(
    private val securityManager: SecurityManager,
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    private val _isSetupComplete = MutableStateFlow<Boolean?>(null)
    val isSetupComplete: StateFlow<Boolean?> = _isSetupComplete.asStateFlow()

    private val _isDbReady = MutableStateFlow(false)
    val isDbReady: StateFlow<Boolean> = _isDbReady.asStateFlow()

    init {
        checkSetup()
    }

    private fun checkSetup() {
        viewModelScope.launch {
            _isSetupComplete.value = securityManager.isSetupComplete()
        }
    }

    fun setupMasterPassword(password: String) {
        viewModelScope.launch {
            try {
                val dbPassphrase = securityManager.setupMasterPassword(password.toCharArray())
                // TODO: Initialize AppDatabase with dbPassphrase
                _isSetupComplete.value = true
                _isDbReady.value = true
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

@Composable
fun MainApp() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val isSetupComplete by mainViewModel.isSetupComplete.collectAsState()

    when (isSetupComplete) {
        null -> {
            // Loading
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        false -> {
            // Setup flow
            SetupScreen(
                onSetupComplete = mainViewModel::setupMasterPassword
            )
        }
        true -> {
            // Main app
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(navController)
        }
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        BottomNavItem.items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    companion object {
        val items = listOf(
            BottomNavItem("备忘录", Icons.Default.Edit, Screen.Memo.route),
            BottomNavItem("日程", Icons.Default.DateRange, Screen.Schedule.route),
            BottomNavItem("打卡", Icons.Default.CheckCircle, Screen.Checkin.route),
            BottomNavItem("密码本", Icons.Default.Lock, Screen.Password.route),
            BottomNavItem("灵感", Icons.Default.Star, Screen.Inspiration.route)
        )
    }
}
