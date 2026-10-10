package id.my.selo.sekost.core.model

import java.time.LocalDate

/** A person renting a [Room]. Active while [checkOutDate] is `null`. */
data class Tenant(
    val id: Long = 0,
    val roomId: Long,
    val name: String,
    val phone: String = "",
    val checkInDate: LocalDate,
    val checkOutDate: LocalDate? = null,
) {
    val isActive: Boolean get() = checkOutDate == null
}
