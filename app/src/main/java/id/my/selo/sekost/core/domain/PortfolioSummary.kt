package id.my.selo.sekost.core.domain

import id.my.selo.sekost.core.model.RoomStatus

/** Aggregated numbers for the dashboard hero and per-property cards. */
data class PortfolioSummary(
    val totalRooms: Int,
    val occupied: Int,
    val vacant: Int,
    val maintenance: Int,
    val expectedIncome: Long,
    val collectedIncome: Long,
    val paidCount: Int,
    val unpaidCount: Int,
    val openIssues: Int,
) {
    val occupancyRate: Float get() = if (totalRooms == 0) 0f else occupied.toFloat() / totalRooms
    val collectionRate: Float get() = if (expectedIncome == 0L) 0f else (collectedIncome.toFloat() / expectedIncome).coerceAtMost(1f)
    val outstandingIncome: Long get() = (expectedIncome - collectedIncome).coerceAtLeast(0)

    companion object {
        val Empty = PortfolioSummary(0, 0, 0, 0, 0, 0, 0, 0, 0)

        fun from(overviews: List<RoomOverview>): PortfolioSummary {
            val occupied = overviews.filter { it.isOccupied }
            return PortfolioSummary(
                totalRooms = overviews.size,
                occupied = occupied.size,
                vacant = overviews.count { it.room.status == RoomStatus.VACANT },
                maintenance = overviews.count { it.room.status == RoomStatus.MAINTENANCE },
                // Expected = what occupied rooms owe, plus anything already received from rooms
                // that have since been vacated this period.
                expectedIncome = occupied.sumOf { it.payment?.amount ?: it.fee.amount } +
                    overviews.filterNot { it.isOccupied }.sumOf { it.payment?.amount ?: 0 },
                collectedIncome = overviews.sumOf { it.payment?.amount ?: 0 },
                paidCount = occupied.count { it.isPaid },
                unpaidCount = occupied.count { !it.isPaid },
                openIssues = overviews.sumOf { it.openIssues },
            )
        }
    }
}
