package com.receiptkeeper.features.analytics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.receiptkeeper.data.local.entity.DailySpending
import com.receiptkeeper.data.local.entity.GoalPeriod
import com.receiptkeeper.domain.model.SpendingGoal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.max
import kotlin.math.min

/**
 * Spending trend chart showing accumulated spending over time
 * with comparison to spending goals
 */
@Composable
fun SpendingTrendChart(
    dailySpending: List<DailySpending>,
    spendingGoals: List<SpendingGoal>,
    startDate: LocalDate,
    endDate: LocalDate,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 200.dp,
    padding: Dp = 16.dp
) {
    // Get colors outside Canvas
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val textColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val surfaceColor = MaterialTheme.colorScheme.onSurface

    if (dailySpending.isEmpty()) {
        // Show empty state
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(chartHeight + padding * 2),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No spending data in selected period",
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    // Calculate max accumulated value for scaling
    val maxAccumulated = dailySpending.maxOfOrNull { it.accumulatedTotal } ?: 0.0
    val maxGoalAmount = spendingGoals.maxOfOrNull { it.amount } ?: 0.0
    val maxYValue = max(maxAccumulated, maxGoalAmount) * 1.1 // Add 10% padding

    // Date range for x-axis
    val totalDays = startDate.until(endDate).days + 1

    // Calculate goal projection for each day based on average daily spend rate
    val goalProjections = mutableListOf<Pair<LocalDate, Double>>()
    spendingGoals.forEach { goal ->
        // Calculate average daily spend rate based on goal period
        val daysInGoalPeriod = when (goal.period) {
            GoalPeriod.DAILY -> 1
            GoalPeriod.WEEKLY -> 7
            GoalPeriod.MONTHLY -> {
                // Get actual days in the month of the start date
                startDate.lengthOfMonth()
            }
            GoalPeriod.YEARLY -> {
                // Get actual days in the year of the start date
                if (startDate.isLeapYear()) 366 else 365
            }
        }
        val dailyGoalAmount = goal.amount / daysInGoalPeriod
        var accumulated = 0.0
        for (dayOffset in 0 until totalDays) {
            val date = startDate.plusDays(dayOffset.toLong())
            accumulated += dailyGoalAmount
            goalProjections.add(date to accumulated)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Spending Trend",
            style = MaterialTheme.typography.titleLarge,
            color = surfaceColor
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight + padding * 2)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // Draw Y-axis grid lines and labels
                val ySteps = 5
                for (i in 0..ySteps) {
                    val y = canvasHeight * (1 - i.toFloat() / ySteps)

                    // Grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(canvasWidth, y),
                        strokeWidth = 1f
                    )

                    // Y-axis label
                    val value = maxYValue * i.toFloat() / ySteps
                    val text = "$${"%.0f".format(value)}"
                    val textLayoutResult = textMeasurer.measure(
                        text,
                        style = TextStyle(
                            fontSize = 10.sp,
                            color = textColor
                        )
                    )

                    drawText(
                        textLayoutResult,
                        topLeft = Offset(0f, y - textLayoutResult.size.height / 2)
                    )
                }

                // Draw X-axis grid lines and labels
                val xSteps = min(5, totalDays - 1)
                for (i in 0..xSteps) {
                    val x = canvasWidth * i.toFloat() / xSteps
                    val dayOffset = (totalDays - 1) * i.toFloat() / xSteps
                    val date = startDate.plusDays(dayOffset.toLong())

                    // Grid line
                    drawLine(
                        color = gridColor,
                        start = Offset(x, 0f),
                        end = Offset(x, canvasHeight),
                        strokeWidth = 1f
                    )

                    // X-axis label (show month/day for first, middle, and last)
                    if (i == 0 || i == xSteps / 2 || i == xSteps) {
                        val formatter = DateTimeFormatter.ofPattern("MM/dd")
                        val text = date.format(formatter)
                        val textLayoutResult = textMeasurer.measure(
                            text,
                            style = TextStyle(
                                fontSize = 10.sp,
                                color = textColor
                            )
                        )

                        drawText(
                            textLayoutResult,
                            topLeft = Offset(x - textLayoutResult.size.width / 2, canvasHeight + 4f)
                        )
                    }
                }

                // Draw goal projection lines (thicker green lines)
                if (goalProjections.isNotEmpty()) {
                    val goalColor = Color.Green.copy(alpha = 0.8f)

                    // Group by goal and draw each goal line
                    val goalsGrouped = goalProjections.groupBy { it.first }
                    val goalPath = Path()

                    var firstPoint = true
                    goalsGrouped.entries.sortedBy { it.key }.forEach { (date, projections) ->
                        val averageProjection = projections.map { it.second }.average()
                        val x = canvasWidth * startDate.until(date).days.toFloat() / (totalDays - 1)
                        val y = canvasHeight * (1 - (averageProjection / maxYValue).toFloat())

                        if (firstPoint) {
                            goalPath.moveTo(x, y)
                            firstPoint = false
                        } else {
                            goalPath.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = goalPath,
                        color = goalColor,
                        style = Stroke(
                            width = 3f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f), 0f)
                        )
                    )
                }

                // Draw actual spending line (red line)
                val spendingColor = Color.Red.copy(alpha = 0.8f)

                val spendingPath = Path()
                var firstSpendingPoint = true

                dailySpending.forEach { data ->
                    val x = canvasWidth * startDate.until(data.date).days.toFloat() / (totalDays - 1)
                    val y = canvasHeight * (1 - (data.accumulatedTotal / maxYValue).toFloat())

                    if (firstSpendingPoint) {
                        spendingPath.moveTo(x, y)
                        firstSpendingPoint = false
                    } else {
                        spendingPath.lineTo(x, y)
                    }

                    // Draw data point
                    drawCircle(
                        color = Color.Red,
                        radius = 4f,
                        center = Offset(x, y)
                    )
                }

                drawPath(
                    path = spendingPath,
                    color = spendingColor,
                    style = Stroke(width = 2f)
                )

                // Draw legend on right side
                val legendY = 20f
                val legendItemSpacing = 30f  // Space between legend items
                val rightPadding = 10f  // Padding from right edge

                // Goal projection legend (rightmost)
                if (spendingGoals.isNotEmpty()) {
                    val goalText = "Goal Projection"
                    val goalTextLayout = textMeasurer.measure(
                        goalText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = surfaceColor
                        )
                    )
                    val goalTextX = canvasWidth - rightPadding - goalTextLayout.size.width
                    val goalSymbolX = goalTextX - 25f  // Space before text

                    drawLine(
                        color = Color.Green,
                        start = Offset(goalSymbolX, legendY),
                        end = Offset(goalSymbolX + 20f, legendY),  // 20px line
                        strokeWidth = 3f
                    )
                    drawText(
                        goalTextLayout,
                        topLeft = Offset(goalTextX, legendY - goalTextLayout.size.height / 2)
                    )

                    // Actual spending legend (left of goal legend)
                    val actualText = "Actual Spending"
                    val actualTextLayout = textMeasurer.measure(
                        actualText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = surfaceColor
                        )
                    )
                    val actualTextX = goalSymbolX - legendItemSpacing - actualTextLayout.size.width
                    val actualSymbolX = actualTextX - 15f  // Space before text

                    drawCircle(
                        color = Color.Red,
                        radius = 6f,
                        center = Offset(actualSymbolX, legendY)
                    )
                    drawText(
                        actualTextLayout,
                        topLeft = Offset(actualTextX, legendY - actualTextLayout.size.height / 2)
                    )
                } else {
                    // Only actual spending legend (no goals)
                    val actualText = "Actual Spending"
                    val actualTextLayout = textMeasurer.measure(
                        actualText,
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = surfaceColor
                        )
                    )
                    val actualTextX = canvasWidth - rightPadding - actualTextLayout.size.width
                    val actualSymbolX = actualTextX - 15f  // Space before text

                    drawCircle(
                        color = Color.Red,
                        radius = 6f,
                        center = Offset(actualSymbolX, legendY)
                    )
                    drawText(
                        actualTextLayout,
                        topLeft = Offset(actualTextX, legendY - actualTextLayout.size.height / 2)
                    )
                }
            }
        }

        // Summary statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val lastSpending = dailySpending.lastOrNull()
            val totalSpent = lastSpending?.accumulatedTotal ?: 0.0

            Column {
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor
                )
                Text(
                    text = "$${"%.2f".format(totalSpent)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = surfaceColor
                )
            }

            if (spendingGoals.isNotEmpty()) {
                val totalGoal = spendingGoals.sumOf { it.amount }
                Column {
                    Text(
                        text = "Goal Total",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                    Text(
                        text = "$${"%.2f".format(totalGoal)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = surfaceColor
                    )
                }

                val progress = (totalSpent / totalGoal * 100).coerceIn(0.0, 100.0)
                Column {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor
                    )
                    Text(
                        text = "${"%.1f".format(progress)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (progress > 100) Color.Red else surfaceColor
                    )
                }
            }
        }
    }
}