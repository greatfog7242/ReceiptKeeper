package com.receiptkeeper.domain.model

import com.receiptkeeper.data.local.entity.GoalPeriod
import java.time.Instant

/**
 * Domain model for SpendingGoal
 */
data class SpendingGoal(
    val id: Long = 0,
    val amount: Double,
    val period: GoalPeriod,
    val categoryId: Long? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
)
