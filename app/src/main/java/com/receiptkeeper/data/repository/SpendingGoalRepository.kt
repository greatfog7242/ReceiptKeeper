package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.SpendingGoalDao
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.SpendingGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for SpendingGoal operations
 */
@Singleton
class SpendingGoalRepository @Inject constructor(
    private val spendingGoalDao: SpendingGoalDao
) {
    fun getAllSpendingGoals(): Flow<List<SpendingGoal>> {
        return spendingGoalDao.getAllSpendingGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getSpendingGoalById(goalId: Long): Flow<SpendingGoal?> {
        return spendingGoalDao.getSpendingGoalById(goalId).map { it?.toDomain() }
    }

    fun getSpendingGoalsByPeriod(period: GoalPeriod): Flow<List<SpendingGoal>> {
        return spendingGoalDao.getSpendingGoalsByPeriod(period).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getSpendingGoalsByCategory(categoryId: Long): Flow<List<SpendingGoal>> {
        return spendingGoalDao.getSpendingGoalsByCategory(categoryId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGlobalSpendingGoals(): Flow<List<SpendingGoal>> {
        return spendingGoalDao.getGlobalSpendingGoals().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertSpendingGoal(goal: SpendingGoal): Long {
        return spendingGoalDao.insertSpendingGoal(goal.toEntity())
    }

    suspend fun updateSpendingGoal(goal: SpendingGoal) {
        spendingGoalDao.updateSpendingGoal(goal.copy(updatedAt = Instant.now()).toEntity())
    }

    suspend fun deleteSpendingGoal(goal: SpendingGoal) {
        spendingGoalDao.deleteSpendingGoal(goal.toEntity())
    }

    suspend fun deleteSpendingGoalById(goalId: Long) {
        spendingGoalDao.deleteSpendingGoalById(goalId)
    }
}
