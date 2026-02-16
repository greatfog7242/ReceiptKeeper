package com.receiptkeeper.data.local.dao

import androidx.room.*
import com.receiptkeeper.data.local.entity.PaymentMethodEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for PaymentMethod operations
 */
@Dao
interface PaymentMethodDao {

    @Query("SELECT * FROM payment_methods ORDER BY name ASC")
    fun getAllPaymentMethods(): Flow<List<PaymentMethodEntity>>

    @Query("SELECT * FROM payment_methods WHERE id = :paymentMethodId")
    fun getPaymentMethodById(paymentMethodId: Long): Flow<PaymentMethodEntity?>

    @Query("SELECT * FROM payment_methods WHERE name = :name")
    suspend fun getPaymentMethodByName(name: String): PaymentMethodEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPaymentMethod(paymentMethod: PaymentMethodEntity): Long

    @Update
    suspend fun updatePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Delete
    suspend fun deletePaymentMethod(paymentMethod: PaymentMethodEntity)

    @Query("DELETE FROM payment_methods WHERE id = :paymentMethodId")
    suspend fun deletePaymentMethodById(paymentMethodId: Long)
}
