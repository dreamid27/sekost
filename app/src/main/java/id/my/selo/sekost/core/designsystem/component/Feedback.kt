package id.my.selo.sekost.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import id.my.selo.sekost.core.designsystem.theme.SekostTheme

/** Centered illustration + copy + optional call to action, used whenever a list is empty. */
@Composable
fun EmptyState(
    illustration: @Composable () -> Unit,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = SekostTheme.spacing.xxl, vertical = SekostTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        illustration()
        Spacer(Modifier.height(SekostTheme.spacing.xl))
        Text(title, style = MaterialTheme.typography.headlineSmall, color = SekostTheme.colors.textPrimary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(SekostTheme.spacing.sm))
        Text(body, style = MaterialTheme.typography.bodyMedium, color = SekostTheme.colors.textSecondary, textAlign = TextAlign.Center)
        Spacer(Modifier.height(SekostTheme.spacing.xl))
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.sm),
            content = actions,
        )
    }
}

/** Branded modal sheet with a serif title. Content is scrollable and IME-aware. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SekostSheet(
    title: String,
    onDismiss: () -> Unit,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = SekostTheme.colors.surface,
        shape = RoundedCornerShape(topStart = SekostTheme.radii.xl, topEnd = SekostTheme.radii.xl),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = SekostTheme.spacing.screenGutter)
                .navigationBarsPadding()
                .padding(bottom = SekostTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md),
        ) {
            Column {
                Text(title, style = MaterialTheme.typography.headlineSmall, color = SekostTheme.colors.textPrimary)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SekostTheme.colors.textSecondary,
                        modifier = Modifier.padding(top = SekostTheme.spacing.xs),
                    )
                }
            }
            content()
        }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissLabel: String = "Batal",
    destructive: Boolean = false,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SekostTheme.colors.surface,
        shape = RoundedCornerShape(SekostTheme.radii.xl),
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = SekostTheme.colors.textSecondary) },
        confirmButton = {
            SekostTextButton(
                text = confirmLabel,
                onClick = onConfirm,
                contentColor = if (destructive) SekostTheme.colors.danger else SekostTheme.colors.brand,
            )
        },
        dismissButton = { SekostTextButton(text = dismissLabel, onClick = onDismiss, contentColor = SekostTheme.colors.textSecondary) },
    )
}
