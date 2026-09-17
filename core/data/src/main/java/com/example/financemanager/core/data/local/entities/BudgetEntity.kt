package com.example.financemanager.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val monthYear: String, // Format: "MM-YYYY"
    val monthlyLimit: Double
)