package com.tawandachiteshe.coinage.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tawandachiteshe.coinage.db.Category
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepository(db: ExpensifyDatabase) {

    private val q = db.categoryQueries

    init {
        seedDefaults()
    }

    fun getAll(): Flow<List<Category>> =
        q.selectAll().asFlow().mapToList(Dispatchers.IO)

    fun getByType(type: String): Flow<List<Category>> =
        q.selectByType(type).asFlow().mapToList(Dispatchers.IO)

    fun getExpenseJars(): Flow<List<Category>> =
        q.selectExpenseJars().asFlow().mapToList(Dispatchers.IO)

    suspend fun updateBudget(id: String, budget: Double) =
        withContext(Dispatchers.IO) { q.updateBudget(budget, id) }

    suspend fun insert(
        id: String,
        name: String,
        icon: String,
        colorHex: String,
        type: String,
        budgetLimit: Double,
        isDefault: Long,
    ) = withContext(Dispatchers.IO) {
        q.insert(id, name, icon, colorHex, type, budgetLimit, isDefault)
    }

    suspend fun update(
        name: String,
        icon: String,
        colorHex: String,
        budgetLimit: Double,
        id: String,
    ) = withContext(Dispatchers.IO) {
        q.update(name, icon, colorHex, budgetLimit, id)
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) { q.delete(id) }

    fun seedDefaults() {
        DEFAULT_CATEGORIES.forEach { c ->
            q.insert(c.id, c.name, c.icon, c.colorHex, c.type, 0.0, 1L)
        }
        DEFAULT_BUDGETS.forEach { (id, budget) ->
            q.seedBudget(budget, id)
        }
    }

    companion object {
        data class CategorySeed(
            val id: String,
            val name: String,
            val icon: String,
            val colorHex: String,
            val type: String,
        )

        val DEFAULT_BUDGETS: Map<String, Double> = mapOf(
            "cat_groceries"     to 500.0,
            "cat_dining"        to 300.0,
            "cat_transport"     to 150.0,
            "cat_health"        to 100.0,
            "cat_housing"       to 1500.0,
            "cat_utilities"     to 200.0,
            "cat_entertainment" to 100.0,
            "cat_shopping"      to 200.0,
            "cat_savings"       to 500.0,
            "cat_other"         to 100.0,
        )

        val DEFAULT_CATEGORIES = listOf(
            CategorySeed("cat_salary",        "Salary",        "briefcase",    "#5fd7a4", "INCOME"),
            CategorySeed("cat_freelance",     "Freelance",     "laptop",       "#5fd7a4", "INCOME"),
            CategorySeed("cat_groceries",     "Groceries",     "shopping_cart","#ffd84a", "EXPENSE"),
            CategorySeed("cat_dining",        "Dining out",    "utensils",     "#ff7a2b", "EXPENSE"),
            CategorySeed("cat_transport",     "Transport",     "car",          "#7cc4ff", "EXPENSE"),
            CategorySeed("cat_health",        "Health",        "heart",        "#ff8da1", "EXPENSE"),
            CategorySeed("cat_housing",       "Housing",       "home",         "#8b5cf6", "EXPENSE"),
            CategorySeed("cat_utilities",     "Utilities",     "zap",          "#ffd84a", "EXPENSE"),
            CategorySeed("cat_entertainment", "Entertainment", "clapperboard", "#ff7a2b", "EXPENSE"),
            CategorySeed("cat_shopping",      "Shopping",      "shopping_bag", "#ff8da1", "EXPENSE"),
            CategorySeed("cat_savings",       "Savings",       "piggy_bank",   "#5fd7a4", "BOTH"),
            CategorySeed("cat_other",         "Other",         "other",        "#efd9b1", "BOTH"),
        )
    }
}