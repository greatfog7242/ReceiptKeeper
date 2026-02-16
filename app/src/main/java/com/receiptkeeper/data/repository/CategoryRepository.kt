package com.receiptkeeper.data.repository

import com.receiptkeeper.data.local.dao.CategoryDao
import com.receiptkeeper.data.mapper.toDomain
import com.receiptkeeper.data.mapper.toEntity
import com.receiptkeeper.domain.model.Category
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Category operations
 */
@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {
    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getCategoryById(categoryId: Long): Flow<Category?> {
        return categoryDao.getCategoryById(categoryId).map { it?.toDomain() }
    }

    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toDomain()
    }

    fun getDefaultCategories(): Flow<List<Category>> {
        return categoryDao.getDefaultCategories().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    suspend fun deleteCategoryById(categoryId: Long) {
        categoryDao.deleteCategoryById(categoryId)
    }
}
