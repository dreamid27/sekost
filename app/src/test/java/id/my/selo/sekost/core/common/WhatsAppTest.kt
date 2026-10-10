package id.my.selo.sekost.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WhatsAppTest {
    @Test fun `normalize converts local numbers to international digits`() {
        assertThat(WhatsApp.normalize("0812-3456-7890")).isEqualTo("6281234567890")
        assertThat(WhatsApp.normalize("+62 812 3456 7890")).isEqualTo("6281234567890")
        assertThat(WhatsApp.normalize("6281234567890")).isEqualTo("6281234567890")
        assertThat(WhatsApp.normalize("812 3456 7890")).isEqualTo("6281234567890")
        assertThat(WhatsApp.normalize("0060123456789")).isEqualTo("60123456789")
        assertThat(WhatsApp.normalize("+60 12 345 6789")).isEqualTo("60123456789")
    }

    @Test fun `normalize rejects inputs too short to be a number`() {
        assertThat(WhatsApp.normalize("")).isNull()
        assertThat(WhatsApp.normalize("+")).isNull()
        assertThat(WhatsApp.normalize("0812")).isNull()
    }

    @Test fun `chatUrl builds a wa me link`() {
        assertThat(WhatsApp.chatUrl("0812 3456 7890")).isEqualTo("https://wa.me/6281234567890")
        assertThat(WhatsApp.chatUrl("12")).isNull()
    }
}
