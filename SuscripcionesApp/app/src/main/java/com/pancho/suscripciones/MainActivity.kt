package com.pancho.suscripciones

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pancho.suscripciones.ui.AppViewModel
import com.pancho.suscripciones.ui.home.HomeScreen
import com.pancho.suscripciones.ui.theme.SuscripcionesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: AppViewModel = viewModel()
            val state by viewModel.uiState.collectAsState()

            SuscripcionesTheme(darkTheme = state.darkTheme) {
              Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            viewModel = viewModel,
                            onAddCategory = { navController.navigate("category_edit") },
                            onAddSubscription = { categoryId -> navController.navigate("subscription_edit/$categoryId/-1") },
                            onEditCategory = { categoryId -> navController.navigate("category_edit?id=$categoryId") },
                            onOpenSubscription = { subId -> navController.navigate("subscription_detail/$subId") },
                            onOpenOptions = { navController.navigate("options") },
                        )
                    }
                    composable(
                        "subscription_detail/{subId}",
                        arguments = listOf(navArgument("subId") { type = NavType.LongType }),
                    ) { backStackEntry ->
                        val subId = backStackEntry.arguments?.getLong("subId") ?: -1L
                        com.pancho.suscripciones.ui.detail.SubscriptionDetailScreen(
                            viewModel = viewModel,
                            subscriptionId = subId,
                            onBack = { navController.popBackStack() },
                            onEdit = { catId, id -> navController.navigate("subscription_edit/$catId/$id") },
                        )
                    }
                    composable(
                        "subscription_edit/{categoryId}/{subId}",
                        arguments = listOf(
                            navArgument("categoryId") { type = NavType.LongType },
                            navArgument("subId") { type = NavType.LongType },
                        ),
                    ) { backStackEntry ->
                        val categoryId = backStackEntry.arguments?.getLong("categoryId") ?: -1L
                        val subId = backStackEntry.arguments?.getLong("subId") ?: -1L
                        com.pancho.suscripciones.ui.edit.SubscriptionEditScreen(
                            viewModel = viewModel,
                            categoryId = categoryId,
                            subscriptionId = if (subId == -1L) null else subId,
                            onDone = { navController.popBackStack() },
                        )
                    }
                    composable(
                        "category_edit?id={id}",
                        arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
                    ) { backStackEntry ->
                        val id = backStackEntry.arguments?.getLong("id") ?: -1L
                        com.pancho.suscripciones.ui.edit.CategoryEditScreen(
                            viewModel = viewModel,
                            categoryId = if (id == -1L) null else id,
                            onDone = { navController.popBackStack() },
                        )
                    }
                    composable("options") {
                        com.pancho.suscripciones.ui.options.OptionsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
              }
            }
        }
    }
}
