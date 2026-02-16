package com.receiptkeeper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Room entity for spending goals/budgets
 */
@Entity(
    tableName = "spending_goals",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["categoryId"])
    ]
)
data class SpendingGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val amount: Double, // Goal amount

    val period: GoalPeriod, // DAILY, WEEKLY, MONTHLY, YEARLY

    val categoryId: Long? = null, // Null = goal applies to all categories

    val createdAt: Instant = Instant.now(),

    val updatedAt: Instant = Instant.now()
)

enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}
