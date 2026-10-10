package id.my.selo.sekost.core.common

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Indonesian: Locale = Locale.forLanguageTag("id-ID")
private val periodFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Indonesian)
private val shortPeriodFormatter = DateTimeFormatter.ofPattern("MMM yy", Indonesian)
private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Indonesian)

/** "Oktober 2026" */
fun YearMonth.displayName(): String = format(periodFormatter).replaceFirstChar { it.titlecase(Indonesian) }

/** "Okt 26" */
fun YearMonth.shortName(): String = format(shortPeriodFormatter).replaceFirstChar { it.titlecase(Indonesian) }

/** "10 Okt 2026" */
fun LocalDate.displayName(): String = format(dateFormatter)
