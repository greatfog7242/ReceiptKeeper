package com.receiptkeeper.data.local.entity

import androidx.room.ColumnInfo
import java.time.LocalDate

/**
 * Data class for daily spending data
 * Used for trend analysis and accumulation charts
 */
data class DailySpending(
    @ColumnInfo(name = "transactionDate")
    val date: LocalDate,

    @ColumnInfo(name = "dailyTotal")
    val dailyTotal: Double,

    @ColumnInfo(name = "accumulatedTotal")
    val accumulatedTotal: Double
)