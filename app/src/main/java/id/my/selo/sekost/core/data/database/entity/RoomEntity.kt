package id.my.selo.sekost.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.selo.sekost.core.model.Room
import id.my.selo.sekost.core.model.RoomStatus

@Entity(
    tableName = "rooms",
    foreignKeys = [
        ForeignKey(
            entity = PropertyEntity::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("propertyId")],
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val propertyId: Long,
    val label: String,
    val floor: String,
    val status: RoomStatus,
    val customMonthlyFee: Long?,
    val notes: String,
)

fun RoomEntity.asModel() = Room(id, propertyId, label, floor, status, customMonthlyFee, notes)

fun Room.asEntity() = RoomEntity(id, propertyId, label.trim(), floor.trim(), status, customMonthlyFee, notes.trim())
