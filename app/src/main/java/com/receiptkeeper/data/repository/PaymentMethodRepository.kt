package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.PaymentMethodDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for PaymentMethod operations
 */
@Singleton
class PaymentMethodRepository @Inject constructor(
    private val paymentMethodDao: PaymentMethodDao
) {
    fun getAllPaymentMethods(): Flow<List<PaymentMethod>> {
        return paymentMethodDao.getAllPaymentMethods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getPaymentMethodById(paymentMethodId: Long): Flow<PaymentMethod?> {
        return paymentMethodDao.getPaymentMethodById(paymentMethodId).map { it?.toDomain() }
    }

    suspend fun getPaymentMethodByName(name: String): PaymentMethod? {
        return paymentMethodDao.getPaymentMethodByName(name)?.toDomain()
    }

    suspend fun insertPaymentMethod(paymentMethod: PaymentMethod): Long {
        return paymentMethodDao.insertPaymentMethod(paymentMethod.toEntity())
    }

    suspend fun updatePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.updatePaymentMethod(paymentMethod.toEntity())
    }

    suspend fun deletePaymentMethod(paymentMethod: PaymentMethod) {
        paymentMethodDao.deletePaymentMethod(paymentMethod.toEntity())
    }

    suspend fun deletePaymentMethodById(paymentMethodId: Long) {
        paymentMethodDao.deletePaymentMethodById(paymentMethodId)
    }
}
