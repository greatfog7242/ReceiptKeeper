package com.receiptkeeper.data.mapper

import com.receiptkeeper.data.local.entity.*
import com.receiptkeeper.domain.model.*

/**
 * Extension functions to map between Entity and Domain models
 */

// Book mappings
fun BookEntity.toDomain(): Book = Book(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Book.toEntity(): BookEntity = BookEntity(
    id = id,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Category mappings
fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    colorHex = colorHex,
    isDefault = isDefault,
    createdAt = createdAt
)

// Vendor mappings
fun VendorEntity.toDomain(): Vendor = Vendor(
    id = id,
    name = name,
    createdAt = createdAt
)

fun Vendor.toEntity(): VendorEntity = VendorEntity(
    id = id,
    name = name,
    createdAt = createdAt
)

// PaymentMethod mappings
fun PaymentMethodEntity.toDomain(): PaymentMethod = PaymentMethod(
    id = id,
    name = name,
    type = type,
    lastFourDigits = lastFourDigits,
    createdAt = createdAt
)

fun PaymentMethod.toEntity(): PaymentMethodEntity = PaymentMethodEntity(
    id = id,
    name = name,
    type = type,
    lastFourDigits = lastFourDigits,
    createdAt = createdAt
)

// SpendingGoal mappings
fun SpendingGoalEntity.toDomain(): SpendingGoal = SpendingGoal(
    id = id,
    amount = amount,
    period = period,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SpendingGoal.toEntity(): SpendingGoalEntity = SpendingGoalEntity(
    id = id,
    amount = amount,
    period = period,
    categoryId = categoryId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// Receipt mappings
fun ReceiptEntity.toDomain(): Receipt = Receipt(
    id = id,
    bookId = bookId,
    vendorId = vendorId,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    totalAmount = totalAmount,
    transactionDate = transactionDate,
    notes = notes,
    imageUri = imageUri,
    extractedText = extractedText,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    bookId = bookId,
    vendorId = vendorId,
    categoryId = categoryId,
    paymentMethodId = paymentMethodId,
    totalAmount = totalAmount,
    transactionDate = transactionDate,
    notes = notes,
    imageUri = imageUri,
    extractedText = extractedText,
    createdAt = createdAt,
    updatedAt = updatedAt
)
