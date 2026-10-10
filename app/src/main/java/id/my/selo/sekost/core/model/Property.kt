package id.my.selo.sekost.core.model

enum class PropertyType { KOST, HOUSE }

/**
 * A building the owner rents out — a boarding house (kost) with many rooms, or a whole house.
 *
 * @property defaultMonthlyFee fee applied to rooms that don't set their own. `null` means the
 * property follows the app-wide fee from [AppSettings.globalMonthlyFee].
 */
data class Property(
    val id: Long = 0,
    val name: String,
    val type: PropertyType,
    val address: String = "",
    val defaultMonthlyFee: Long? = null,
)
