package com.example.financemanager.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.financemanager.core.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE monthYear = :monthYear LIMIT 1")
    fun getBudgetForMonth(monthYear: String): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: BudgetEntity)
}