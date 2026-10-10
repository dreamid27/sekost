package id.my.selo.sekost.core.domain

import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.model.Property
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.Room
import org.junit.Test

class FeeResolverTest {
    private val global = 1_000_000L
    private val propertyWithFee = Property(1, "Kost Melati", PropertyType.KOST, defaultMonthlyFee = 1_200_000)
    private val propertyWithoutFee = propertyWithFee.copy(defaultMonthlyFee = null)

    @Test fun `room override wins over everything`() {
        val room = Room(1, 1, "Kamar 01", customMonthlyFee = 1_500_000)
        assertThat(FeeResolver.resolve(room, propertyWithFee, global)).isEqualTo(ResolvedFee(1_500_000, FeeSource.ROOM))
    }

    @Test fun `property default applies when room has no override`() {
        val room = Room(1, 1, "Kamar 02")
        assertThat(FeeResolver.resolve(room, propertyWithFee, global)).isEqualTo(ResolvedFee(1_200_000, FeeSource.PROPERTY))
    }

    @Test fun `global fee is the final fallback`() {
        val room = Room(1, 1, "Kamar 03")
        assertThat(FeeResolver.resolve(room, propertyWithoutFee, global)).isEqualTo(ResolvedFee(global, FeeSource.GLOBAL))
        assertThat(FeeResolver.resolve(room, null, global).source).isEqualTo(FeeSource.GLOBAL)
    }
}
