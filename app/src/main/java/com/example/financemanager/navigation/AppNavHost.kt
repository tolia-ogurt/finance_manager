package com.example.financemanager.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.financemanager.feature.dashboard.presentation.DashboardViewModel
import com.example.financemanager.feature.dashboard.ui.DashboardScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier
) {
    val backStack = remember { mutableStateListOf<Any>(AppRoute.Dashboard) }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<AppRoute.Dashboard> {
                val viewModel = hiltViewModel<DashboardViewModel>()
                DashboardScreen(viewModel = viewModel)
            }
        }
    )
}