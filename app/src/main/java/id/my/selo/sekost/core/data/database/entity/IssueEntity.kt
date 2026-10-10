package id.my.selo.sekost.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.selo.sekost.core.model.Issue
import id.my.selo.sekost.core.model.IssueStatus
import id.my.selo.sekost.core.model.IssueType
import java.time.LocalDate

@Entity(
    tableName = "issues",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roomId"), Index("status")],
)
data class IssueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val type: IssueType,
    val title: String,
    val description: String,
    val status: IssueStatus,
    val reportedOn: LocalDate,
    val resolvedOn: LocalDate?,
)

fun IssueEntity.asModel() = Issue(id, roomId, type, title, description, status, reportedOn, resolvedOn)

fun Issue.asEntity() = IssueEntity(id, roomId, type, title.trim(), description.trim(), status, reportedOn, resolvedOn)
