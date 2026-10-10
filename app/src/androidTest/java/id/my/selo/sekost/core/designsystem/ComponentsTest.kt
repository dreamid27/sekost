package id.my.selo.sekost.core.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import id.my.selo.sekost.core.designsystem.component.MoneyTextField
import id.my.selo.sekost.core.designsystem.component.Segment
import id.my.selo.sekost.core.designsystem.component.SegmentedPicker
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.model.RoomStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComponentsTest {
    @get:Rule val compose = createComposeRule()

    @Test fun segmentedPickerSelectsTappedOption() {
        var selected by mutableStateOf(RoomStatus.VACANT)
        compose.setContent {
            SekostTheme {
                SegmentedPicker(
                    segments = listOf(
                        Segment(RoomStatus.VACANT, "Kosong"),
                        Segment(RoomStatus.OCCUPIED, "Terisi"),
                        Segment(RoomStatus.MAINTENANCE, "Perawatan"),
                    ),
                    selected = selected,
                    onSelect = { selected = it },
                )
            }
        }
        compose.onNodeWithText("Perawatan").performClick()
        assertThat(selected).isEqualTo(RoomStatus.MAINTENANCE)
    }

    @Test fun moneyFieldKeepsDigitsAndShowsGroupedRupiah() {
        var digits by mutableStateOf("")
        compose.setContent {
            SekostTheme {
                MoneyTextField(digits = digits, onDigitsChange = { digits = it }, label = "Tarif", modifier = Modifier.testTag("money"))
            }
        }
        compose.onNodeWithTag("money").performTextInput("01.500.000abc")
        assertThat(digits).isEqualTo("1500000")
        compose.onNodeWithTag("money").assertTextContains("1.500.000", substring = true)
    }
}
