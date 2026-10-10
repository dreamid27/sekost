package id.my.selo.sekost.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.selo.sekost.core.data.database.entity.TenantEntity
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface TenantDao {
    @Query("SELECT * FROM tenants WHERE checkOutDate IS NULL")
    fun observeActive(): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE roomId = :roomId ORDER BY checkInDate DESC, id DESC")
    fun observeForRoom(roomId: Long): Flow<List<TenantEntity>>

    @Query("SELECT * FROM tenants WHERE roomId = :roomId AND checkOutDate IS NULL LIMIT 1")
    suspend fun activeForRoom(roomId: Long): TenantEntity?

    @Upsert
    suspend fun upsert(tenant: TenantEntity): Long

    @Query("UPDATE tenants SET checkOutDate = :date WHERE roomId = :roomId AND checkOutDate IS NULL")
    suspend fun checkOutRoom(roomId: Long, date: LocalDate)
}
