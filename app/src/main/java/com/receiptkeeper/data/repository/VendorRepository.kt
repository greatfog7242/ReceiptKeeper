package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.VendorDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.Vendor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Vendor operations
 */
@Singleton
class VendorRepository @Inject constructor(
    private val vendorDao: VendorDao
) {
    fun getAllVendors(): Flow<List<Vendor>> {
        return vendorDao.getAllVendors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getVendorById(vendorId: Long): Flow<Vendor?> {
        return vendorDao.getVendorById(vendorId).map { it?.toDomain() }
    }

    suspend fun getVendorByName(name: String): Vendor? {
        return vendorDao.getVendorByName(name)?.toDomain()
    }

    fun searchVendors(query: String): Flow<List<Vendor>> {
        return vendorDao.searchVendors(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertVendor(vendor: Vendor): Long {
        return vendorDao.insertVendor(vendor.toEntity())
    }

    suspend fun updateVendor(vendor: Vendor) {
        vendorDao.updateVendor(vendor.toEntity())
    }

    suspend fun deleteVendor(vendor: Vendor) {
        vendorDao.deleteVendor(vendor.toEntity())
    }

    suspend fun deleteVendorById(vendorId: Long) {
        vendorDao.deleteVendorById(vendorId)
    }

    /**
     * Get or create vendor by name
     * Used by OCR to automatically create vendors from receipt text
     */
    suspend fun getOrCreateVendor(name: String): Long {
        val existing = getVendorByName(name)
        return if (existing != null) {
            existing.id
        } else {
            insertVendor(Vendor(name = name))
        }
    }
}
