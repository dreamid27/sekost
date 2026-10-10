package id.my.selo.sekost.core.domain

/**
 * Generates sequential labels for adding many rooms at once, e.g. "Kamar 01" … "Kamar 10".
 *
 * Numbers are zero-padded to at least two digits (wider when the range needs it) so the
 * labels sort naturally. Labels that already exist in the property are skipped rather than
 * duplicated, so re-running a batch over an existing range is harmless.
 */
object RoomBatch {
    const val MAX_COUNT = 100

    fun labels(prefix: String, start: Int, count: Int, existing: Collection<String> = emptyList()): List<String> {
        if (count <= 0 || start < 0) return emptyList()
        val end = start + count - 1
        val width = maxOf(2, end.toString().length)
        val taken = existing.mapTo(HashSet()) { it.trim().lowercase() }
        val head = prefix.trim().let { if (it.isEmpty()) "" else "$it " }
        return (start..end)
            .map { head + it.toString().padStart(width, '0') }
            .filterNot { it.lowercase() in taken }
    }

    /** The number after the highest "[prefix] N" label already present, so a new batch continues the sequence. */
    fun nextNumber(prefix: String, existing: Collection<String>): Int {
        val pattern = Regex("^${Regex.escape(prefix.trim())}\\s*(\\d+)$", RegexOption.IGNORE_CASE)
        return existing.mapNotNull { pattern.find(it.trim())?.groupValues?.get(1)?.toIntOrNull() }.maxOrNull()?.plus(1) ?: 1
    }
}
