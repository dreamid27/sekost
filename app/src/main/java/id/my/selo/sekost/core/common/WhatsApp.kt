package id.my.selo.sekost.core.common

/** Builds WhatsApp click-to-chat links from phone numbers as tenants type them. */
object WhatsApp {
    private const val COUNTRY_CODE = "62"
    private const val MIN_DIGITS = 8

    /**
     * Normalizes an Indonesian phone number to international digits:
     * "0812-3456-7890" / "+62 812 3456 7890" / "812 3456 7890" → "6281234567890".
     * Returns `null` when the input has too few digits to be a real number.
     */
    fun normalize(phone: String): String? {
        val digits = phone.filter(Char::isDigit)
        val international = when {
            phone.trimStart().startsWith("+") -> digits
            digits.startsWith("00") -> digits.drop(2)
            digits.startsWith("0") -> COUNTRY_CODE + digits.drop(1)
            digits.startsWith(COUNTRY_CODE) -> digits
            else -> COUNTRY_CODE + digits
        }
        return international.takeIf { it.length >= MIN_DIGITS }
    }

    /** `https://wa.me/<number>` link, or `null` when [phone] can't be normalized. */
    fun chatUrl(phone: String): String? = normalize(phone)?.let { "https://wa.me/$it" }
}
