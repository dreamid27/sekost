package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

@Composable
fun SekostTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    prefix: String? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    singleLine: Boolean = true,
    readOnly: Boolean = false,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = if (singleLine) ImeAction.Next else ImeAction.Default,
    ),
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val colors = SekostTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it, color = colors.textTertiary) } },
        leadingIcon = leadingIcon?.let { { Icon(it, null, tint = colors.textSecondary) } },
        prefix = prefix?.let { { Text(it, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary) } },
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        readOnly = readOnly,
        minLines = minLines,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        textStyle = MaterialTheme.typography.bodyLarge,
        shape = RoundedCornerShape(SekostTheme.radii.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.brand,
            unfocusedBorderColor = colors.outline,
            focusedLabelColor = colors.brand,
            unfocusedLabelColor = colors.textSecondary,
            cursorColor = colors.brand,
            focusedContainerColor = colors.surfaceRaised,
            unfocusedContainerColor = colors.surfaceRaised,
            errorBorderColor = colors.danger,
            errorLabelColor = colors.danger,
            errorSupportingTextColor = colors.danger,
        ),
    )
}

/**
 * Rupiah input. [digits] holds only digits (e.g. "1500000"); thousands separators are drawn
 * by a [VisualTransformation] so the cursor never fights the formatting.
 */
@Composable
fun MoneyTextField(
    digits: String,
    onDigitsChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Done,
) {
    SekostTextField(
        value = digits,
        onValueChange = { input -> onDigitsChange(input.filter(Char::isDigit).trimStart('0').take(Rupiah.MAX_DIGITS)) },
        label = label,
        modifier = modifier,
        prefix = "Rp",
        placeholder = "0",
        supportingText = supportingText,
        isError = isError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        visualTransformation = ThousandsTransformation,
    )
}

private object ThousandsTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        if (raw.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val formatted = Rupiah.group(raw.toLong())
        return TransformedText(
            AnnotatedString(formatted),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val digitsAfter = raw.length - offset
                    val separatorsAfter = (digitsAfter - 1).coerceAtLeast(0) / 3
                    return formatted.length - digitsAfter - separatorsAfter
                }

                override fun transformedToOriginal(offset: Int): Int =
                    formatted.take(offset).count(Char::isDigit)
            },
        )
    }
}
