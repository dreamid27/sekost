package id.my.selo.sekost.core.model

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class AppSettings(
    val ownerName: String = "",
    val globalMonthlyFee: Long = DEFAULT_GLOBAL_FEE,
    val dueDay: Int = DEFAULT_DUE_DAY,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    /** Set once the first-run setup is finished (or skipped). */
    val onboardingCompleted: Boolean = false,
) {
    companion object {
        const val DEFAULT_GLOBAL_FEE = 1_000_000L
        const val DEFAULT_DUE_DAY = 10
    }
}
