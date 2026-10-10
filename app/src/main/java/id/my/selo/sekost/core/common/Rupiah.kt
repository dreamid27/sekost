package id.my.selo.sekost.core.common

/**
 * Indonesian Rupiah formatting without relying on device locale data
 * (keeps output identical across devices and JVM tests).
 */
object Rupiah {
    /** 1500000 → "Rp1.500.000" */
    fun format(amount: Long): String {
        val sign = if (amount < 0) "-" else ""
        return "${sign}Rp${group(kotlin.math.abs(amount))}"
    }

    /** 1500000 → "1.500.000" — used for input fields where the "Rp" prefix is drawn separately. */
    fun group(amount: Long): String = amount.toString().reversed().chunked(3).joinToString(".").reversed()

    /** Compact form for tight spaces: 1500000 → "Rp1,5 jt", 750000 → "Rp750 rb". */
    fun compact(amount: Long): String {
        val abs = kotlin.math.abs(amount)
        val sign = if (amount < 0) "-" else ""
        return when {
            abs >= 1_000_000_000 -> "${sign}Rp${decimal(abs, 1_000_000_000)} M"
            abs >= 1_000_000 -> "${sign}Rp${decimal(abs, 1_000_000)} jt"
            abs >= 1_000 -> "${sign}Rp${decimal(abs, 1_000)} rb"
            else -> format(amount)
        }
    }

    /** Parses user input like "1.500.000" or "Rp 1500000" into a number; `null` when empty. */
    fun parse(input: String): Long? = input.filter(Char::isDigit).take(MAX_DIGITS).toLongOrNull()

    private fun decimal(value: Long, unit: Long): String {
        val whole = value / unit
        val hundredths = (value % unit) * 100 / unit
        if (hundredths == 0L) return "$whole"
        return "$whole," + hundredths.toString().padStart(2, '0').trimEnd('0')
    }

    const val MAX_DIGITS = 12
}
