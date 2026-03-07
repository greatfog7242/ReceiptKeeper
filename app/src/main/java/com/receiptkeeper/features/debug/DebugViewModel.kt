package com.receiptkeeper.features.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.receiptkeeper.data.local.entity.PaymentType
import com.receiptkeeper.data.repository.*
import com.receiptkeeper.domain.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val categoryRepository: CategoryRepository,
    private val vendorRepository: VendorRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val receiptRepository: ReceiptRepository
) : ViewModel() {
    
    suspend fun createTestData(): String {
        return try {
            // Create test books
            val personalBookId = bookRepository.insertBook(
                Book(name = "Personal Expenses", description = "Personal spending")
            )
            val businessBookId = bookRepository.insertBook(
                Book(name = "Business Expenses", description = "Work-related spending")
            )
            
            // Get default categories
            val categories = categoryRepository.getAllCategories().first()
            val foodCategory = categories.find { it.name == "Food" } ?: categories.first()
            val groceryCategory = categories.find { it.name == "Grocery" } ?: categories[1]
            val transportationCategory = categories.find { it.name == "Transportation" } ?: categories[2]
            val entertainmentCategory = categories.find { it.name == "Entertainment" } ?: categories[3]
            val otherCategory = categories.find { it.name == "Other" } ?: categories.last()
            
            // Create test vendors
            val walmartVendorId = vendorRepository.getOrCreateVendor("Walmart")
            val targetVendorId = vendorRepository.getOrCreateVendor("Target")
            val starbucksVendorId = vendorRepository.getOrCreateVendor("Starbucks")
            val amazonVendorId = vendorRepository.getOrCreateVendor("Amazon")
            val gasStationVendorId = vendorRepository.getOrCreateVendor("Shell Gas Station")
            val movieTheaterVendorId = vendorRepository.getOrCreateVendor("AMC Theaters")
            
            // Create test payment methods
            val cashPaymentId = paymentMethodRepository.insertPaymentMethod(
                PaymentMethod(name = "Cash", type = PaymentType.CASH)
            )
            val creditCardPaymentId = paymentMethodRepository.insertPaymentMethod(
                PaymentMethod(name = "Visa ****1234", type = PaymentType.CREDIT_CARD, lastFourDigits = "1234")
            )
            
            // Create test receipts with varying amounts to test tree view percentages
            // These amounts are designed to create clear percentages for visualization
            
            // Large amounts (40%, 30%, 20%, 10% distribution)
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = walmartVendorId,
                    categoryId = groceryCategory.id,
                    paymentMethodId = creditCardPaymentId,
                    totalAmount = 400.00,
                    transactionDate = LocalDate.now().minusDays(1),
                    notes = "Weekly grocery shopping",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = amazonVendorId,
                    categoryId = otherCategory.id,
                    paymentMethodId = creditCardPaymentId,
                    totalAmount = 300.00,
                    transactionDate = LocalDate.now().minusDays(2),
                    notes = "Online shopping",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = targetVendorId,
                    categoryId = groceryCategory.id,
                    paymentMethodId = cashPaymentId,
                    totalAmount = 200.00,
                    transactionDate = LocalDate.now().minusDays(3),
                    notes = "Household items",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = starbucksVendorId,
                    categoryId = foodCategory.id,
                    paymentMethodId = cashPaymentId,
                    totalAmount = 100.00,
                    transactionDate = LocalDate.now().minusDays(4),
                    notes = "Coffee and snacks",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            // Add some smaller amounts to test aggregation into "Other" category
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = gasStationVendorId,
                    categoryId = transportationCategory.id,
                    paymentMethodId = creditCardPaymentId,
                    totalAmount = 50.00,
                    transactionDate = LocalDate.now().minusDays(5),
                    notes = "Gas fill-up",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = personalBookId,
                    vendorId = movieTheaterVendorId,
                    categoryId = entertainmentCategory.id,
                    paymentMethodId = cashPaymentId,
                    totalAmount = 40.00,
                    transactionDate = LocalDate.now().minusDays(6),
                    notes = "Movie tickets",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = businessBookId,
                    vendorId = starbucksVendorId,
                    categoryId = foodCategory.id,
                    paymentMethodId = creditCardPaymentId,
                    totalAmount = 25.00,
                    transactionDate = LocalDate.now().minusDays(7),
                    notes = "Client meeting coffee",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            receiptRepository.insertReceipt(
                Receipt(
                    bookId = businessBookId,
                    vendorId = amazonVendorId,
                    categoryId = otherCategory.id,
                    paymentMethodId = creditCardPaymentId,
                    totalAmount = 15.00,
                    transactionDate = LocalDate.now().minusDays(8),
                    notes = "Office supplies",
                    imageUri = null,
                    extractedText = null
                )
            )
            
            "Test data created successfully!\n" +
            "- 2 books (Personal, Business)\n" +
            "- 6 vendors\n" +
            "- 2 payment methods\n" +
            "- 8 receipts with varying amounts\n" +
            "Total spending: $1,130.00\n" +
            "Check Analytics tab to see tree view visualization."
            
        } catch (e: Exception) {
            "Error creating test data: ${e.message}"
        }
    }
    
    suspend fun clearTestData(): String {
        return try {
            // Get all receipts
            val receipts = receiptRepository.getAllReceipts().first()
            
            // Delete all receipts (cascade will handle related data)
            receipts.forEach { receipt ->
                receiptRepository.deleteReceiptById(receipt.id)
            }
            
            "Test data cleared successfully!"
        } catch (e: Exception) {
            "Error clearing test data: ${e.message}"
        }
    }
}