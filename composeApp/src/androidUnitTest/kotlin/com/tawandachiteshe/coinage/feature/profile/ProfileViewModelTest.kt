package com.tawandachiteshe.coinage.feature.profile

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import com.tawandachiteshe.coinage.data.CategoryRepository
import com.tawandachiteshe.coinage.data.TransactionRepository
import com.tawandachiteshe.coinage.data.UserProfileRepository
import com.tawandachiteshe.coinage.db.ExpensifyDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var db: ExpensifyDatabase
    private lateinit var profileRepo: UserProfileRepository
    private lateinit var txRepo: TransactionRepository
    private lateinit var catRepo: CategoryRepository

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ExpensifyDatabase.Schema.create(driver)
        db = ExpensifyDatabase(driver)
        profileRepo = UserProfileRepository(db, testDispatcher)
        txRepo = TransactionRepository(db, testDispatcher)
        catRepo = CategoryRepository(db, testDispatcher)
    }

    @AfterTest
    fun teardown() {
        Dispatchers.resetMain()
    }

    // ── Badge fires ──────────────────────────────────────────────────────────

    @Test
    fun firstSaveFiresOnFirstTransaction() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        vm.events.test {
            insertTx("tx1", 10.0)
            assertEquals("first save", awaitBadge())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onARollFiresAtFiveTransactions() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        vm.events.test {
            repeat(5) { i -> insertTx("tx$i", 10.0) }

            // Exactly 2 badges unlock: "first save" (after tx0) and "on a roll" (after tx4)
            val badges = (1..2).map { awaitBadge() }
            assertTrue("first save" in badges)
            assertTrue("on a roll" in badges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun mountainMoverFiresWhenTotalExceeds1000() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        vm.events.test {
            // 1001.0 < 1 million, triggers first save + mountain mover (not big spender: 1001 > 500)
            insertTx("tx1", 1001.0)
            val badges = (1..3).map { awaitBadge() } // first save + mountain mover + big spender
            assertTrue("mountain mover" in badges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun bigSpenderFiresForSingleExpenseOver500() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        vm.events.test {
            insertTx("tx1", 501.0) // first save + big spender (501 > 500, but < 1000 so no mountain mover)
            val badges = (1..2).map { awaitBadge() }
            assertTrue("big spender" in badges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun streakKeeperFiresFor7ConsecutiveDays() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)
        val today = System.currentTimeMillis() / 86_400_000L

        vm.events.test {
            // Insert on 7 different consecutive days — one per day
            repeat(7) { i ->
                insertTx("tx$i", 10.0, date = (today - (6 - i)) * 86_400_000L)
            }
            // Badges that unlock: first save (tx0), on a roll (tx4), streak keeper (tx6)
            val badges = (1..3).map { awaitBadge() }
            assertTrue("streak keeper" in badges)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun longHaulerStateIsTrueWhenJoinedOver30DaysAgo() = runTest {
        // longHauler depends on joinedAt vs Clock.now() — it's set on the FIRST emission
        // so it can't fire as an event (first emission is always silent).
        // We test the state directly instead.
        val thirtyOneDaysAgo = System.currentTimeMillis() - (31L * 86_400_000L)
        db.userProfileQueries.upsertName("Tawanda", thirtyOneDaysAgo)

        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)
        testScheduler.advanceUntilIdle()

        assertTrue(vm.state.value.hasLongHauler)
    }

    // ── No re-fire ───────────────────────────────────────────────────────────

    @Test
    fun firstSaveDoesNotFireTwice() = runTest {
        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        vm.events.test {
            insertTx("tx1", 10.0) // earns first save
            assertEquals("first save", awaitBadge())

            insertTx("tx2", 10.0) // second tx — first save must not re-fire
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun badgesAlreadyEarnedBeforeScreenOpensDoNotFire() = runTest {
        // Pre-populate: first save and on a roll already earned
        repeat(5) { i -> insertTx("pre$i", 10.0) }

        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)

        // Collect events while inserting a 6th transaction
        val emitted = mutableListOf<ProfileEvent>()
        val job = backgroundScope.launch { vm.events.collect { emitted.add(it) } }

        insertTx("tx6", 10.0)
        testScheduler.advanceUntilIdle()
        job.cancel()

        val labels = emitted.filterIsInstance<ProfileEvent.BadgeEarned>().map { it.label }
        assertFalse("first save" in labels, "first save should not re-fire")
        assertFalse("on a roll" in labels, "on a roll should not re-fire")
    }

    // ── State reflects real data ─────────────────────────────────────────────

    @Test
    fun stateReflectsTxCountAndTotal() = runTest {
        insertTx("tx1", 100.0)
        insertTx("tx2", 250.0)
        insertTx("tx3", 50.0)

        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)
        testScheduler.advanceUntilIdle()

        val state = vm.state.value
        assertEquals(3L, state.txCount)
        assertEquals("\$400", state.totalTrackedLabel)
        assertTrue(state.hasFirstSave)
        assertFalse(state.hasOnARoll) // only 3 txns, need 5
    }

    @Test
    fun halfFullUnlocksWithThreeOrMoreJars() = runTest {
        insertTx("tx1", 10.0)

        val vm = ProfileViewModel(profileRepo, txRepo, catRepo)
        testScheduler.advanceUntilIdle()

        // Default categories seed 10+ active expense jars — halfFull needs jarCount >= 3
        assertTrue(vm.state.value.hasHalfFull)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private suspend fun insertTx(
        id: String,
        amount: Double,
        type: String = "EXPENSE",
        categoryId: String = "cat_other",
        date: Long = System.currentTimeMillis(),
    ) = txRepo.insert(id, amount, type, categoryId, "Test", null, "USD", date, System.currentTimeMillis())

    private suspend fun app.cash.turbine.TurbineTestContext<ProfileEvent>.awaitBadge(): String =
        (awaitItem() as ProfileEvent.BadgeEarned).label
}
