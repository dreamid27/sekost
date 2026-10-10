package id.my.selo.sekost.core.data.repository

import androidx.room.withTransaction
import id.my.selo.sekost.core.data.database.SekostDatabase
import javax.inject.Inject

/** Runs multi-repository writes atomically, so observers never see half-finished state. */
interface TransactionRunner {
    suspend fun <T> run(block: suspend () -> T): T
}

internal class RoomTransactionRunner @Inject constructor(
    private val db: SekostDatabase,
) : TransactionRunner {
    override suspend fun <T> run(block: suspend () -> T): T = db.withTransaction { block() }
}
