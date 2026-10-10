package id.my.selo.sekost.core.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RoomBatchTest {
    @Test fun `generates zero padded sequential labels`() {
        assertThat(RoomBatch.labels("Kamar", start = 1, count = 3)).containsExactly("Kamar 01", "Kamar 02", "Kamar 03").inOrder()
    }

    @Test fun `padding widens when the range needs three digits`() {
        assertThat(RoomBatch.labels("A", start = 99, count = 2)).containsExactly("A 099", "A 100").inOrder()
    }

    @Test fun `blank prefix yields bare numbers`() {
        assertThat(RoomBatch.labels("  ", start = 9, count = 2)).containsExactly("09", "10").inOrder()
    }

    @Test fun `existing labels are skipped case insensitively`() {
        val labels = RoomBatch.labels("Kamar", start = 1, count = 4, existing = listOf("kamar 02", " Kamar 04 "))
        assertThat(labels).containsExactly("Kamar 01", "Kamar 03").inOrder()
    }

    @Test fun `invalid counts produce nothing`() {
        assertThat(RoomBatch.labels("Kamar", start = 1, count = 0)).isEmpty()
        assertThat(RoomBatch.labels("Kamar", start = -1, count = 5)).isEmpty()
    }

    @Test fun `next number continues after the highest matching label`() {
        val existing = listOf("Kamar 02", "Kamar 10", "Kamar 9", "Gudang 50", "Kamar VIP")
        assertThat(RoomBatch.nextNumber("Kamar", existing)).isEqualTo(11)
        assertThat(RoomBatch.nextNumber("Kamar", emptyList())).isEqualTo(1)
        assertThat(RoomBatch.nextNumber("Gudang", existing)).isEqualTo(51)
    }
}
