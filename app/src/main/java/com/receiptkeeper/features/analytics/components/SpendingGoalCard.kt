package com.receiptkeeper.features.analytics.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.domain.model.Category
import com.receiptkeeper.domain.model.SpendingGoal
import java.time.LocalDate

/**
 * Card showing spending goal with progress bar
 * Displays current spending vs goal amount
 */
@Composable
fun SpendingGoalCard(
    goal: SpendingGoal,
    currentSpending: Double,
    category: Category?,
    modifier: Modifier = Modifier
) {
    val progress = if (goal.amount > 0) (currentSpending / goal.amount).toFloat() else 0f
    val isOverBudget = currentSpending > goal.amount
    val percentSpent = if (goal.amount > 0) (currentSpending / goal.amount * 100).toInt() else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverBudget) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isOverBudget) Icons.Default.Warning else Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Column {
                        Text(
                            text = category?.name ?: "All Categories",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isOverBudget) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Text(
                            text = "${goal.period.name} Budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isOverBudget) {
                                MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                }

                Text(
                    text = if (isOverBudget) "${percentSpent}% over" else "${percentSpent}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isOverBudget) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = if (isOverBudget) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                trackColor = if (isOverBudget) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                },
            )

            // Spending summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    Text(
                        text = "$${"%.2f".format(currentSpending)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (isOverBudget) "Over by" else "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    Text(
                        text = "$${"%.2f".format(kotlin.math.abs(goal.amount - currentSpending))}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        }
                    )
                    Text(
                        text = "$${"%.2f".format(goal.amount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isOverBudget) {
                            MaterialTheme.colorScheme.onErrorContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            // Warning message if over budget
            if (isOverBudget) {
                Text(
                    text = "You've exceeded your ${goal.period.name.lowercase()} budget!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

/**
 * Calculate spending for a goal based on its period
 */
fun calculateGoalSpending(
    goal: SpendingGoal,
    totalSpendingForPeriod: Double,
    categorySpendingForPeriod: Double
): Double {
    return if (goal.categoryId != null) {
        categorySpendingForPeriod
    } else {
        totalSpendingForPeriod
    }
}

/**
 * Get date range for a goal period
 */
fun getGoalPeriodDateRange(period: GoalPeriod): Pair<LocalDate, LocalDate> {
    val now = LocalDate.now()
    return when (period) {
        GoalPeriod.DAILY -> {
            now to now
        }
        GoalPeriod.WEEKLY -> {
            val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
            startOfWeek to now
        }
        GoalPeriod.MONTHLY -> {
            val startOfMonth = now.withDayOfMonth(1)
            startOfMonth to now
        }
        GoalPeriod.YEARLY -> {
            val startOfYear = now.withDayOfYear(1)
            startOfYear to now
        }
    }
}
