package id.my.selo.sekost.core.domain

import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.Room

/** Where a room's effective monthly fee comes from. */
enum class FeeSource { ROOM, PROPERTY, GLOBAL }

data class ResolvedFee(val amount: Long, val source: FeeSource)

/**
 * Fee precedence: room override → property default → global setting.
 * This is the single source of truth for "how much should this room pay".
 */
object FeeResolver {
    fun resolve(room: Room, property: Property?, globalFee: Long): ResolvedFee = when {
        room.customMonthlyFee != null -> ResolvedFee(room.customMonthlyFee, FeeSource.ROOM)
        property?.defaultMonthlyFee != null -> ResolvedFee(property.defaultMonthlyFee, FeeSource.PROPERTY)
        else -> ResolvedFee(globalFee, FeeSource.GLOBAL)
    }
}
