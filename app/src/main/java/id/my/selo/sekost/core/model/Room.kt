package id.my.selo.sekost.core.model

enum class RoomStatus { VACANT, OCCUPIED, MAINTENANCE }

/**
 * A rentable unit inside a [Property]. For a whole-house rental there is usually exactly one.
 *
 * @property customMonthlyFee fee override for this room only. `null` means it follows the
 * property default (or the global default when the property has none).
 */
data class Room(
    val id: Long = 0,
    val propertyId: Long,
    val label: String,
    val floor: String = "",
    val status: RoomStatus = RoomStatus.VACANT,
    val customMonthlyFee: Long? = null,
    val notes: String = "",
)
