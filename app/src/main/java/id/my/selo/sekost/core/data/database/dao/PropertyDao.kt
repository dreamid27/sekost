package id.my.selo.sekost.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.selo.sekost.core.data.database.entity.PropertyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PropertyDao {
    @Query("SELECT * FROM properties ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<PropertyEntity>>

    @Query("SELECT * FROM properties WHERE id = :id")
    fun observe(id: Long): Flow<PropertyEntity?>

    @Upsert
    suspend fun upsert(property: PropertyEntity): Long

    @Query("DELETE FROM properties WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT COUNT(*) FROM properties")
    suspend fun count(): Int
}
