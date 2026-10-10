package id.my.selo.sekost.core.domain

import java.time.LocalDate
import java.time.YearMonth

/** Indirection over the system clock so tests can pin "today". */
interface TimeProvider {
    fun today(): LocalDate
    fun currentPeriod(): YearMonth = YearMonth.from(today())
}

object SystemTimeProvider : TimeProvider {
    override fun today(): LocalDate = LocalDate.now()
}
