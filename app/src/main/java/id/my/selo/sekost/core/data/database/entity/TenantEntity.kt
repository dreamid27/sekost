package id.my.selo.sekost.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.selo.sekost.core.model.Tenant
import java.time.LocalDate

@Entity(
    tableName = "tenants",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("roomId")],
)
data class TenantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val name: String,
    val phone: String,
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate?,
)

fun TenantEntity.asModel() = Tenant(id, roomId, name, phone, checkInDate, checkOutDate)

fun Tenant.asEntity() = TenantEntity(id, roomId, name.trim(), phone.trim(), checkInDate, checkOutDate)
