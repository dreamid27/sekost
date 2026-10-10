package id.my.selo.sekost.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import id.my.selo.sekost.core.data.database.entity.RoomEntity
import id.my.selo.sekost.core.model.RoomStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomDao {
    /** Natural-ish ordering so "Kamar 2" sorts before "Kamar 10". */
    @Query("SELECT * FROM rooms ORDER BY propertyId, LENGTH(label), label COLLATE NOCASE")
    fun observeAll(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun observe(id: Long): Flow<RoomEntity?>

    @Upsert
    suspend fun upsert(room: RoomEntity): Long

    /** Inserts in a single transaction, so a batch of rooms appears all at once or not at all. */
    @Insert
    suspend fun insertAll(rooms: List<RoomEntity>): List<Long>

    @Query("UPDATE rooms SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: RoomStatus)

    @Query("UPDATE rooms SET customMonthlyFee = :fee WHERE id = :id")
    suspend fun updateCustomFee(id: Long, fee: Long?)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun delete(id: Long)
}
