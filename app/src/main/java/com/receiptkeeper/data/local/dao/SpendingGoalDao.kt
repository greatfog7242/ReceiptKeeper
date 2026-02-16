package com.receiptkeeper.data.local.dao

import androidx.room.*
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.data.local.entity.SpendingGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for SpendingGoal operations
 */
@Dao
interface SpendingGoalDao {

    @Query("SELECT * FROM spending_goals ORDER BY createdAt DESC")
    fun getAllSpendingGoals(): Flow<List<SpendingGoalEntity>>

    @Query("SELECT * FROM spending_goals WHERE id = :goalId")
    fun getSpendingGoalById(goalId: Long): Flow<SpendingGoalEntity?>

    @Query("SELECT * FROM spending_goals WHERE period = :period")
    fun getSpendingGoalsByPeriod(period: GoalPeriod): Flow<List<SpendingGoalEntity>>

    @Query("SELECT * FROM spending_goals WHERE categoryId = :categoryId")
    fun getSpendingGoalsByCategory(categoryId: Long): Flow<List<SpendingGoalEntity>>

    @Query("SELECT * FROM spending_goals WHERE categoryId IS NULL")
    fun getGlobalSpendingGoals(): Flow<List<SpendingGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpendingGoal(goal: SpendingGoalEntity): Long

    @Update
    suspend fun updateSpendingGoal(goal: SpendingGoalEntity)

    @Delete
    suspend fun deleteSpendingGoal(goal: SpendingGoalEntity)

    @Query("DELETE FROM spending_goals WHERE id = :goalId")
    suspend fun deleteSpendingGoalById(goalId: Long)
}
