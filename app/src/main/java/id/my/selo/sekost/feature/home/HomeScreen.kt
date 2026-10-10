package id.my.selo.sekost.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.my.selo.sekost.R
import id.my.selo.sekost.core.common.Rupiah
import id.my.selo.sekost.core.common.displayName
import id.my.selo.sekost.core.designsystem.component.EmptyState
import id.my.selo.sekost.core.designsystem.component.IconTile
import id.my.selo.sekost.core.designsystem.component.ProgressTrack
import id.my.selo.sekost.core.designsystem.component.SectionHeader
import id.my.selo.sekost.core.designsystem.component.SekostButton
import id.my.selo.sekost.core.designsystem.component.SekostCard
import id.my.selo.sekost.core.designsystem.component.SekostTextButton
import id.my.selo.sekost.core.designsystem.component.StatusDot
import id.my.selo.sekost.core.designsystem.component.StatusRing
import id.my.selo.sekost.core.designsystem.icon.SekostIcons
import id.my.selo.sekost.core.designsystem.illustration.BrandMark
import id.my.selo.sekost.core.designsystem.illustration.HouseIllustration
import id.my.selo.sekost.core.designsystem.theme.SekostTheme
import id.my.selo.sekost.core.designsystem.theme.ToneColors
import id.my.selo.sekost.core.domain.PortfolioSummary
import id.my.selo.sekost.core.model.PropertyType
import id.my.selo.sekost.core.model.RoomStatus
import id.my.selo.sekost.core.ui.icon
import id.my.selo.sekost.core.ui.labelRes
import id.my.selo.sekost.core.ui.tone
import id.my.selo.sekost.feature.property.PropertyEditorSheet

@Composable
fun HomeRoute(
    onOpenProperty: (Long) -> Unit,
    onOpenBilling: () -> Unit,
    onOpenIssues: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onOpenProperty = onOpenProperty,
        onOpenBilling = onOpenBilling,
        onOpenIssues = onOpenIssues,
        onSaveProperty = viewModel::saveProperty,
        onLoadSample = viewModel::loadSampleData,
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onOpenProperty: (Long) -> Unit,
    onOpenBilling: () -> Unit,
    onOpenIssues: () -> Unit,
    onSaveProperty: (id.my.selo.sekost.core.model.Property) -> Unit,
    onLoadSample: () -> Unit,
) {
    var showEditor by rememberSaveable { mutableStateOf(false) }
    val ready = state as? HomeUiState.Ready ?: return
    val gutter = SekostTheme.spacing.screenGutter

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("home_list"),
        contentPadding = PaddingValues(bottom = SekostTheme.spacing.huge),
    ) {
        item { HomeHeader(ready) }
        if (ready.properties.isEmpty()) {
            item {
                EmptyState(
                    illustration = { HouseIllustration() },
                    title = stringResource(R.string.home_empty_title),
                    body = stringResource(R.string.home_empty_body),
                ) {
                    SekostButton(stringResource(R.string.home_add_property), onClick = { showEditor = true }, icon = SekostIcons.Plus)
                    SekostTextButton(stringResource(R.string.home_try_sample), onClick = onLoadSample, icon = SekostIcons.Spark)
                }
            }
        } else {
            item { IncomeHero(ready.summary, onOpenBilling, Modifier.padding(horizontal = gutter)) }
            item {
                Row(
                    Modifier.padding(horizontal = gutter).padding(top = SekostTheme.spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md),
                ) {
                    OccupancyCard(ready.summary, Modifier.weight(1.3f))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.md)) {
                        MiniStat(
                            icon = SekostIcons.Clipboard,
                            tone = SekostTheme.colors.complaint,
                            value = ready.summary.openIssues.toString(),
                            label = stringResource(R.string.home_open_issues),
                            onClick = onOpenIssues,
                            modifier = Modifier.testTag("home_open_issues"),
                        )
                        MiniStat(
                            icon = SekostIcons.Wallet,
                            tone = SekostTheme.colors.unpaid,
                            value = Rupiah.compact(ready.summary.outstandingIncome),
                            label = stringResource(R.string.home_outstanding),
                            onClick = onOpenBilling,
                        )
                    }
                }
            }
            item {
                SectionHeader(
                    title = stringResource(R.string.home_properties),
                    actionLabel = stringResource(R.string.action_add),
                    actionIcon = SekostIcons.Plus,
                    onAction = { showEditor = true },
                    modifier = Modifier.padding(horizontal = gutter),
                )
            }
            items(ready.properties, key = { it.property.id }) { card ->
                PropertyCardRow(
                    card = card,
                    onClick = { onOpenProperty(card.property.id) },
                    modifier = Modifier.padding(horizontal = gutter, vertical = SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
                )
            }
        }
    }

    if (showEditor) {
        PropertyEditorSheet(
            initial = null,
            globalFee = ready.globalFee,
            onDismiss = { showEditor = false },
            onSave = {
                onSaveProperty(it)
                showEditor = false
            },
        )
    }
}

@Composable
private fun HomeHeader(state: HomeUiState.Ready) {
    val greeting = stringResource(
        when (state.dayPart) {
            DayPart.MORNING -> R.string.greeting_morning
            DayPart.AFTERNOON -> R.string.greeting_afternoon
            DayPart.EVENING -> R.string.greeting_evening
            DayPart.NIGHT -> R.string.greeting_night
        },
    )
    Column(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = SekostTheme.spacing.screenGutter)
            .padding(top = SekostTheme.spacing.lg, bottom = SekostTheme.spacing.xl),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BrandMark(size = SekostTheme.sizes.iconTile - SekostTheme.spacing.sm)
            Spacer(Modifier.width(SekostTheme.spacing.sm + SekostTheme.spacing.xxs))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge, color = SekostTheme.colors.textPrimary)
        }
        Spacer(Modifier.height(SekostTheme.spacing.xl))
        val salutation = if (state.ownerName.isBlank()) greeting else stringResource(R.string.greeting_with_name, greeting, state.ownerName)
        Text(
            salutation.uppercase(),
            style = SekostTheme.typography.overline,
            color = SekostTheme.colors.accent,
        )
        Text(
            stringResource(R.string.home_title, state.period.displayName()),
            style = MaterialTheme.typography.headlineLarge,
            color = SekostTheme.colors.textPrimary,
            modifier = Modifier.padding(top = SekostTheme.spacing.xs),
        )
    }
}

@Composable
private fun IncomeHero(summary: PortfolioSummary, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    val shape = RoundedCornerShape(SekostTheme.radii.xl)
    Surface(onClick = onClick, shape = shape, color = Color.Transparent, modifier = modifier.fillMaxWidth().testTag("home_income")) {
        Column(
            Modifier
                .clip(shape)
                .background(Brush.linearGradient(listOf(colors.heroGradientStart, colors.heroGradientEnd)))
                .drawBehind { roofPattern(colors.onHero.copy(alpha = 0.07f)) }
                .padding(SekostTheme.spacing.xl),
        ) {
            Text(
                stringResource(R.string.home_income_label),
                style = MaterialTheme.typography.labelLarge,
                color = colors.onHero.copy(alpha = 0.75f),
            )
            Text(
                Rupiah.format(summary.collectedIncome),
                style = SekostTheme.typography.hero,
                color = colors.onHero,
                modifier = Modifier.padding(top = SekostTheme.spacing.xs),
            )
            Text(
                stringResource(R.string.home_income_of, Rupiah.format(summary.expectedIncome)),
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onHero.copy(alpha = 0.75f),
            )
            Spacer(Modifier.height(SekostTheme.spacing.lg))
            ProgressTrack(fraction = summary.collectionRate, color = colors.accent, trackColor = colors.onHero.copy(alpha = 0.16f))
            Spacer(Modifier.height(SekostTheme.spacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val allPaid = summary.unpaidCount == 0
                if (summary.occupied > 0) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(SekostTheme.radii.pill))
                            .background(colors.onHero.copy(alpha = 0.12f))
                            .padding(horizontal = SekostTheme.spacing.md, vertical = SekostTheme.spacing.xs + SekostTheme.spacing.xxs),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(if (allPaid) colors.occupied.content else colors.accent)
                            Spacer(Modifier.width(SekostTheme.spacing.sm))
                            Text(
                                if (allPaid) {
                                    stringResource(R.string.home_all_paid)
                                } else {
                                    stringResource(
                                        R.string.home_unpaid_count,
                                        summary.unpaidCount,
                                    )
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onHero,
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Icon(SekostIcons.ChevronRight, null, tint = colors.onHero.copy(alpha = 0.75f))
            }
        }
    }
}

/** Subtle repeating roof chevrons — the brand motif — behind the hero card. */
private fun androidx.compose.ui.graphics.drawscope.DrawScope.roofPattern(color: Color) {
    val step = size.width / 5f
    val h = step * 0.45f
    for (row in 0..1) {
        for (i in 0..5) {
            val x = i * step + if (row == 1) step / 2 else 0f
            val y = size.height * 0.15f + row * h * 1.6f
            val p = Path().apply {
                moveTo(x, y + h)
                lineTo(x + step / 2, y)
                lineTo(x + step, y + h)
            }
            drawPath(p, color, style = Stroke(width = 6f, cap = StrokeCap.Round))
        }
    }
    drawCircle(color, radius = size.height * 0.6f, center = Offset(size.width * 1.05f, size.height * 1.1f))
}

@Composable
private fun OccupancyCard(summary: PortfolioSummary, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    SekostCard(modifier.testTag("home_occupancy")) {
        Text(stringResource(R.string.home_occupancy), style = MaterialTheme.typography.titleSmall, color = colors.textPrimary)
        Spacer(Modifier.height(SekostTheme.spacing.md))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusRing(
                segments = listOf(
                    summary.occupied.toFloat() to RoomStatus.OCCUPIED.tone.content,
                    summary.vacant.toFloat() to RoomStatus.VACANT.tone.content,
                    summary.maintenance.toFloat() to RoomStatus.MAINTENANCE.tone.content,
                ),
                trackColor = colors.surfaceSunken,
                size = SekostTheme.sizes.iconTile * 1.5f,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${(summary.occupancyRate * 100).toInt()}%",
                        style = SekostTheme.typography.amountSmall,
                        color = colors.textPrimary,
                    )
                }
            }
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(SekostTheme.spacing.xs)) {
                LegendItem(RoomStatus.OCCUPIED.tone, stringResource(RoomStatus.OCCUPIED.labelRes), summary.occupied)
                LegendItem(RoomStatus.VACANT.tone, stringResource(RoomStatus.VACANT.labelRes), summary.vacant)
                LegendItem(RoomStatus.MAINTENANCE.tone, stringResource(R.string.status_maintenance), summary.maintenance)
            }
        }
    }
}

@Composable
private fun LegendItem(tone: ToneColors, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StatusDot(tone.content)
        Spacer(Modifier.width(SekostTheme.spacing.xs + SekostTheme.spacing.xxs))
        Text("$count", style = MaterialTheme.typography.labelLarge, color = SekostTheme.colors.textPrimary)
        Spacer(Modifier.width(SekostTheme.spacing.xs))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = SekostTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MiniStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tone: ToneColors,
    value: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SekostCard(modifier = modifier.fillMaxWidth(), onClick = onClick, contentPadding = PaddingValues(SekostTheme.spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tone, size = SekostTheme.sizes.iconTile - SekostTheme.spacing.sm)
            Spacer(Modifier.width(SekostTheme.spacing.sm + SekostTheme.spacing.xxs))
            Column {
                Text(value, style = SekostTheme.typography.amountSmall, color = SekostTheme.colors.textPrimary, maxLines = 1)
                Text(
                    label,
                    style = MaterialTheme.typography.bodySmall,
                    color = SekostTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun PropertyCardRow(card: PropertyCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = SekostTheme.colors
    val p = card.property
    SekostCard(modifier = modifier.fillMaxWidth().testTag("property_card_${p.id}"), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(
                p.type.icon,
                if (p.type == PropertyType.KOST) {
                    colors.occupied
                } else {
                    ToneColors(
                        colors.accent,
                        colors.accentContainer,
                        colors.onAccentContainer,
                    )
                },
            )
            Spacer(Modifier.width(SekostTheme.spacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    p.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOf(stringResource(p.type.labelRes), p.address).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(SekostIcons.ChevronRight, null, tint = colors.textTertiary, modifier = Modifier.size(SekostTheme.sizes.iconMd))
        }
        Spacer(Modifier.height(SekostTheme.spacing.md))
        ProgressTrack(
            fraction = card.summary.occupancyRate,
            color = colors.occupied.content,
            trackColor = colors.surfaceSunken,
            height = SekostTheme.spacing.xs + SekostTheme.spacing.xxs,
        )
        Spacer(Modifier.height(SekostTheme.spacing.sm))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.home_property_occupancy, card.summary.occupied, card.summary.totalRooms),
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            if (card.summary.unpaidCount > 0) {
                Text(
                    stringResource(R.string.home_unpaid_count, card.summary.unpaidCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.unpaid.content,
                )
            }
        }
    }
}
