package com.example.financemanager.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {
    @Serializable
    data object Dashboard : AppRoute
}