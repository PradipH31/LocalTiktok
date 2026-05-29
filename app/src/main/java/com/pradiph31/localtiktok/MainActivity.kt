package com.pradiph31.localtiktok

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pradiph31.localtiktok.ui.screens.BrowseScreen
import com.pradiph31.localtiktok.ui.screens.FeedScreen
import com.pradiph31.localtiktok.ui.screens.HiddenFilesScreen
import com.pradiph31.localtiktok.ui.screens.IgnoredFoldersScreen
import com.pradiph31.localtiktok.ui.screens.LikedScreen
import com.pradiph31.localtiktok.ui.screens.SettingsScreen
import com.pradiph31.localtiktok.ui.screens.ViewerScreen
import com.pradiph31.localtiktok.ui.theme.LocalTiktokTheme
import com.pradiph31.localtiktok.viewmodel.MainViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Filled.Home)
    data object Liked : Screen("liked", "Liked", Icons.Filled.Favorite)
    data object Browse : Screen("browse", "Browse", Icons.Filled.FolderOpen)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
}

val bottomNavItems = listOf(Screen.Home, Screen.Liked, Screen.Browse, Screen.Settings)

class MainActivity : ComponentActivity() {

    private var hasPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermission = permissions.values.all { it }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        checkAndRequestPermissions()

        setContent {
            LocalTiktokTheme(darkTheme = true) {
                if (hasPermission) {
                    val viewModel: MainViewModel = viewModel()
                    MainApp(viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            hasPermission = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}

@Composable
fun MainApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determine if we should show bottom bar (hide on feed for immersive experience - actually show on all)
    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black.copy(alpha = 0.9f)
            ) {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.DarkGray
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                LaunchedEffect(Unit) {
                    viewModel.loadMedia()
                }
                FeedScreen(viewModel = viewModel)
            }
            composable(Screen.Liked.route) {
                LikedScreen(
                    viewModel = viewModel,
                    onItemClick = { item ->
                        navController.navigate("viewer/${Uri.encode(item.uniqueKey)}")
                    }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToHiddenFiles = { navController.navigate("hidden_files") },
                    onNavigateToIgnoredFolders = { navController.navigate("ignored_folders") }
                )
            }
            composable(Screen.Browse.route) {
                BrowseScreen(viewModel = viewModel)
            }
            composable("hidden_files") {
                HiddenFilesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("ignored_folders") {
                IgnoredFoldersScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("viewer/{itemKey}") { backStackEntry ->
                val itemKey = Uri.decode(backStackEntry.arguments?.getString("itemKey") ?: "")
                ViewerScreen(
                    viewModel = viewModel,
                    itemKey = itemKey,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
