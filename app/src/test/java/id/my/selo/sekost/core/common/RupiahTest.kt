package id.my.selo.sekost.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RupiahTest {
    @Test fun `format groups thousands with dots`() {
        assertThat(Rupiah.format(0)).isEqualTo("Rp0")
        assertThat(Rupiah.format(950)).isEqualTo("Rp950")
        assertThat(Rupiah.format(1_500_000)).isEqualTo("Rp1.500.000")
        assertThat(Rupiah.format(-25_000)).isEqualTo("-Rp25.000")
    }

    @Test fun `compact uses Indonesian units`() {
        assertThat(Rupiah.compact(750_000)).isEqualTo("Rp750 rb")
        assertThat(Rupiah.compact(1_000_000)).isEqualTo("Rp1 jt")
        assertThat(Rupiah.compact(1_250_000)).isEqualTo("Rp1,25 jt")
        assertThat(Rupiah.compact(1_500_000)).isEqualTo("Rp1,5 jt")
        assertThat(Rupiah.compact(1_050_000)).isEqualTo("Rp1,05 jt")
        assertThat(Rupiah.compact(2_000_000_000)).isEqualTo("Rp2 M")
        assertThat(Rupiah.compact(500)).isEqualTo("Rp500")
    }

    @Test fun `parse strips formatting and rejects empty input`() {
        assertThat(Rupiah.parse("Rp 1.500.000")).isEqualTo(1_500_000)
        assertThat(Rupiah.parse("")).isNull()
        assertThat(Rupiah.parse("abc")).isNull()
    }
}
