package id.my.selo.sekost.core.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import id.my.selo.sekost.core.model.Payment
import java.time.LocalDate
import java.time.YearMonth

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["roomId", "period"], unique = true), Index("period")],
)
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roomId: Long,
    val tenantId: Long?,
    val period: YearMonth,
    val amount: Long,
    val paidOn: LocalDate,
)

fun PaymentEntity.asModel() = Payment(id, roomId, tenantId, period, amount, paidOn)

fun Payment.asEntity() = PaymentEntity(id, roomId, tenantId, period, amount, paidOn)
