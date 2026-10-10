package id.my.selo.sekost.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import id.my.selo.sekost.R
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors
import id.my.selo.sekost.core.domain.FeeSource
import id.my.selo.sekost.core.model.IssueType
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.model.ThemeMode

/* Presentation mapping of domain enums: label, tone and icon live here, never in the model. */

@get:StringRes
val RoomStatus.labelRes: Int
    get() = when (this) {
        RoomStatus.VACANT -> R.string.status_vacant
        RoomStatus.OCCUPIED -> R.string.status_occupied
        RoomStatus.MAINTENANCE -> R.string.status_maintenance
    }

val RoomStatus.tone: ToneColors
    @Composable @ReadOnlyComposable
    get() = when (this) {
        RoomStatus.VACANT -> SekostTheme.colors.vacant
        RoomStatus.OCCUPIED -> SekostTheme.colors.occupied
        RoomStatus.MAINTENANCE -> SekostTheme.colors.maintenance
    }

val RoomStatus.icon: ImageVector
    get() = when (this) {
        RoomStatus.VACANT -> SekostIcons.Door
        RoomStatus.OCCUPIED -> SekostIcons.Person
        RoomStatus.MAINTENANCE -> SekostIcons.Toolbox
    }

@get:StringRes
val PropertyType.labelRes: Int
    get() = when (this) {
        PropertyType.KOST -> R.string.property_type_kost
        PropertyType.HOUSE -> R.string.property_type_house
    }

val PropertyType.icon: ImageVector
    get() = when (this) {
        PropertyType.KOST -> SekostIcons.Kost
        PropertyType.HOUSE -> SekostIcons.House
    }

@get:StringRes
val IssueType.labelRes: Int
    get() = when (this) {
        IssueType.MAINTENANCE -> R.string.issue_type_maintenance
        IssueType.COMPLAINT -> R.string.issue_type_complaint
    }

val IssueType.tone: ToneColors
    @Composable @ReadOnlyComposable
    get() = when (this) {
        IssueType.MAINTENANCE -> SekostTheme.colors.maintenance
        IssueType.COMPLAINT -> SekostTheme.colors.complaint
    }

val IssueType.icon: ImageVector
    get() = when (this) {
        IssueType.MAINTENANCE -> SekostIcons.Toolbox
        IssueType.COMPLAINT -> SekostIcons.Complaint
    }

@get:StringRes
val FeeSource.labelRes: Int
    get() = when (this) {
        FeeSource.ROOM -> R.string.fee_source_room
        FeeSource.PROPERTY -> R.string.fee_source_property
        FeeSource.GLOBAL -> R.string.fee_source_global
    }

@get:StringRes
val ThemeMode.labelRes: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> R.string.theme_system
        ThemeMode.LIGHT -> R.string.theme_light
        ThemeMode.DARK -> R.string.theme_dark
    }

/** "Rina Kartika" → "RK" */
fun initialsOf(name: String): String =
    name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }
