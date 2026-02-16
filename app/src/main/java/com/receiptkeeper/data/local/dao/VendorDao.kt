package com.receiptkeeper.data.local.dao

import androidx.room.*
import com.receiptkeeper.data.local.entity.VendorEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for Vendor operations
 */
@Dao
interface VendorDao {

    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<VendorEntity>>

    @Query("SELECT * FROM vendors WHERE id = :vendorId")
    fun getVendorById(vendorId: Long): Flow<VendorEntity?>

    @Query("SELECT * FROM vendors WHERE name = :name")
    suspend fun getVendorByName(name: String): VendorEntity?

    @Query("SELECT * FROM vendors WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchVendors(query: String): Flow<List<VendorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVendor(vendor: VendorEntity): Long

    @Update
    suspend fun updateVendor(vendor: VendorEntity)

    @Delete
    suspend fun deleteVendor(vendor: VendorEntity)

    @Query("DELETE FROM vendors WHERE id = :vendorId")
    suspend fun deleteVendorById(vendorId: Long)
}
