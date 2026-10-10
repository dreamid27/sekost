package id.my.selo.sekost.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.selo.sekost.core.data.database.entity.PaymentEntity
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE period = :period")
    fun observeForPeriod(period: YearMonth): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE roomId = :roomId ORDER BY period DESC")
    fun observeForRoom(roomId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments WHERE roomId = :roomId AND period = :period LIMIT 1")
    suspend fun find(roomId: Long, period: YearMonth): PaymentEntity?

    @Upsert
    suspend fun upsert(payment: PaymentEntity): Long

    @Query("DELETE FROM payments WHERE roomId = :roomId AND period = :period")
    suspend fun delete(roomId: Long, period: YearMonth)
}
