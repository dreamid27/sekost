package id.my.selo.sekost.core.model

import java.time.LocalDate
import java.time.YearMonth

/** Rent paid for one room for one billing [period]. At most one per room per period. */
data class Payment(
    val id: Long = 0,
    val roomId: Long,
    val tenantId: Long?,
    val period: YearMonth,
    val amount: Long,
    val paidOn: LocalDate,
)
