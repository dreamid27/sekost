package id.my.selo.sekost.core.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import id.my.selo.sekost.core.data.database.entity.IssueEntity
import id.my.selo.sekost.core.model.IssueStatus
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

@Dao
interface IssueDao {
    @Query("SELECT * FROM issues ORDER BY status = 'RESOLVED', reportedOn DESC, id DESC")
    fun observeAll(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE status = 'OPEN'")
    fun observeOpen(): Flow<List<IssueEntity>>

    @Query("SELECT * FROM issues WHERE roomId = :roomId ORDER BY status = 'RESOLVED', reportedOn DESC, id DESC")
    fun observeForRoom(roomId: Long): Flow<List<IssueEntity>>

    @Upsert
    suspend fun upsert(issue: IssueEntity): Long

    @Query("UPDATE issues SET status = :status, resolvedOn = :resolvedOn WHERE id = :id")
    suspend fun updateStatus(id: Long, status: IssueStatus, resolvedOn: LocalDate?)

    @Query("DELETE FROM issues WHERE id = :id")
    suspend fun delete(id: Long)
}
