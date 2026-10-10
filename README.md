# Sekost

**Kelola kost & rumah sewa dengan tenang.** An offline-first Android app for owners of boarding
houses (*kost*) and rental houses: label rooms, track their status, set monthly fees, manage
tenants and payments, and keep on top of maintenance and complaints.

## Features

| Area | What you can do |
| --- | --- |
| Onboarding | First-run setup: owner name, global fee (with quick picks), due day, theme (applied live), then start empty or with sample data |
| Properties | Create kost / rumah sewa, address, optional property-wide default fee |
| Rooms | Label rooms (with floor/block and notes), status **Kosong / Terisi / Perawatan**, filter by status |
| Fees | Three-level fee: **room override → property default → global setting** (always shows its source) |
| Tenants | Check in (name, phone, move-in date), edit, check out; past tenant history |
| Payments | Mark a month paid/unpaid per room, payment history, overdue (*Terlambat*) after the due day, month-by-month billing view |
| Issues | Report *Perawatan* (maintenance) or *Keluhan* (complaint) per room, resolve / reopen / delete |
| Dashboard | Monthly income vs expected, occupancy ring, unpaid & open-issue shortcuts |
| Settings | Owner name (greeting), global fee, due day, light / dark / system theme |

New installs open a short animated **onboarding** (welcome → name → fee & due day → theme → summary).
It only shows when onboarding was never completed *and* there are no properties, so existing users
skip it. The last step (and the empty dashboard) offers **"data contoh"**, which seeds a realistic
portfolio for exploring.

## Tech stack

| Concern | Choice |
| --- | --- |
| Language / UI | Kotlin 2.1, Jetpack Compose, Material 3 (fully re-themed) |
| Architecture | Single-activity MVVM + unidirectional data flow (`StateFlow<UiState>`) |
| DI | Hilt (KSP) |
| Persistence | Room (relational data, cascading deletes, exported schemas) + DataStore Preferences (settings) |
| Navigation | Navigation Compose with type-safe `@Serializable` routes |
| Build | Gradle Kotlin DSL, version catalog (`gradle/libs.versions.toml`), configuration cache |
| Style | Spotless + ktlint (`android_studio` style, 140 cols) |
| Tests | JUnit4, Truth, Turbine, coroutines-test (JVM) · Room + Compose UI tests (instrumented) |

minSdk 29 · targetSdk/compileSdk 35 · JDK 17.

## Getting started

```bash
./gradlew installDebug          # build & install (debug app id: id.my.selo.sekost.debug)
./gradlew testDebugUnitTest     # fast JVM tests (domain + ViewModels)
./gradlew connectedDebugAndroidTest   # Room + Compose UI tests on a device/emulator
./gradlew spotlessApply         # format; CI should run spotlessCheck
```

## Folder structure

```
app/src/main/java/id/my/selo/sekost/
├── SekostApplication.kt, MainActivity.kt, MainViewModel.kt   # app entry, theme mode, splash
├── navigation/          # type-safe routes (+ nav-arg helper)
├── ui/                  # app shell: SekostApp (NavHost, scaffold), SekostNavigationBar
├── core/
│   ├── model/           # pure domain models (Property, Room, Tenant, Payment, Issue, AppSettings)
│   ├── domain/          # business rules: FeeResolver, RoomOverview, PortfolioSummary, use cases, TimeProvider
│   ├── data/
│   │   ├── database/    # Room DB, entities (+ mappers), DAOs, converters
│   │   ├── datastore/   # DataStore-backed SettingsRepository
│   │   ├── repository/  # repository interfaces, offline implementations, TransactionRunner, sample seeder
│   │   └── di/          # Hilt modules
│   ├── designsystem/
│   │   ├── theme/       # tokens: Palette, SekostColors, typography, spacing/radii/sizes/motion, SekostTheme
│   │   ├── component/   # SekostCard, buttons, text fields, SegmentedPicker, ToneBadge, sheets, progress…
│   │   ├── icon/        # SekostIcons — bespoke line icon set
│   │   └── illustration/# Canvas illustrations + BrandMark (theme-aware)
│   ├── ui/              # shared presentation helpers (enum → label/tone/icon, IssueCard)
│   └── common/          # formatting: Rupiah, Indonesian dates
└── feature/
    ├── onboarding/  home/  property/  room/  billing/  issues/  settings/
    │   └── <Feature>Screen.kt  <Feature>ViewModel.kt  [sheets]
app/schemas/             # exported Room schemas (commit these; needed for migrations)
app/src/test/            # JVM tests + testing/ (fakes, MainDispatcherRule)
app/src/androidTest/     # instrumented tests
```

Dependency direction: `feature → core/ui, core/designsystem, core/domain → core/data → core/model`.
Features never import each other except for explicitly shared sheets (e.g. `IssueSheet`).

## Design system

**Brand story.** *Sekost* blends "se-kost" (one boarding house) with calm hospitality. The palette
is drawn from Indonesian homes: **Jade** (tropical leaves, trust), **Genteng** terracotta (roof
tiles), **Krem** (limewashed walls), **Tinta** ink. The mark is a terracotta roof chevron over an
arched jade door; the chevron recurs as a subtle pattern on the dashboard hero.

**Typography.** *Plus Jakarta Sans* (a typeface commissioned for Jakarta) for UI text; *Fraunces*
(soft optical serif) for headlines and money amounts. Both are bundled variable fonts.

**Tokens** — always go through `SekostTheme`, never raw values:

| Token | Access | Notes |
| --- | --- | --- |
| Colors | `SekostTheme.colors.*` | Semantic: `surfaceRaised`, `textSecondary`, `brand`, `accent`, and `ToneColors` for `vacant / occupied / maintenance / paid / unpaid / complaint`. Light & dark variants. `Palette` is `internal` — features can't use raw hex. |
| Type | `MaterialTheme.typography.*`, `SekostTheme.typography.{hero, amount, amountSmall, overline}` | |
| Spacing | `SekostTheme.spacing.{xs…huge, screenGutter}` | 4dp scale |
| Radii | `SekostTheme.radii.{xs…xl, pill}` | cards `lg`, sheets `xl`, controls `pill` |
| Sizes | `SekostTheme.sizes.*` | icon sizes, tiles, button height, hairline |
| Motion | `SekostTheme.motion.{fast, medium, slow}` | ms |

Material components are mapped from the same tokens (`toMaterialColorScheme`, `toMaterialShapes`),
so stock M3 widgets stay on-brand.

**Icons.** `SekostIcons` — 24dp grid, 1.75 stroke, round caps/joins, architectural shapes echoing
the mark. Stroke is black and tinted by `Icon`.

**Illustrations.** Flat, outline-free scenes (house, open door + key, wallet + coins, potted plant)
drawn on Canvas from theme tokens, so they adapt to dark mode automatically. Onboarding adds
animated scenes (`OnboardingIllustrations.kt`): a one-shot entrance plus a slow ambient loop, read
in the draw phase so motion never recomposes. Some react to input — the nameplate shows the
owner's initials, the calendar highlights the chosen due day, the sun morphs into a moon.

## Code conventions

- **Screens** come in pairs: `XRoute` (gets the ViewModel via `hiltViewModel()`, collects state with
  `collectAsStateWithLifecycle`) and a stateless `XScreen(state, callbacks…)` that is previewable/testable.
- **ViewModels** expose one `StateFlow<XUiState>` built with `combine(...).stateIn(WhileSubscribed(5s))`;
  user actions are plain functions. Loading/not-found are explicit `sealed interface` states.
- **Business rules** live in `core/domain` as pure functions (e.g. `FeeResolver`, `buildRoomOverviews`)
  so they're unit-tested without Android.
- **Repositories** are interfaces; implementations are `internal`. Multi-step writes are atomic
  (`withTransaction` / `TransactionRunner`).
- **Time** comes from `TimeProvider`, never `LocalDate.now()` in logic, so tests can pin "today".
- **Strings** are in `res/values/strings.xml` (Indonesian); enum labels/tones/icons are mapped in
  `core/ui/ModelUi.kt`, never inside models.
- **No raw colors/dp in features.** Use tokens; add a token if one is missing.
- Money is `Long` rupiah; format with `Rupiah.format/compact`. Inputs use `MoneyTextField`
  (digits-only state + visual thousands separator).
- Every interactive element important for testing gets a `testTag`.

## Testing

| Suite | Location | Covers |
| --- | --- | --- |
| Domain | `src/test/.../core` | Rupiah formatting, fee precedence, overview joins, portfolio summary |
| ViewModels | `src/test/.../feature` | Onboarding (step flow, fee validation, atomic save, sample data, who sees it), Home (seeding is idempotent), Room detail (fee override, check-in/out, payments, issues, overdue), Billing (filters, month navigation, move-in rules) |
| Data | `src/androidTest/.../core/data` | Room: upsert ids, check-in/out transactions, one payment per room per month, cascade deletes, issue lifecycle |
| UI | `src/androidTest/.../core/designsystem` | SegmentedPicker selection, MoneyTextField digit filtering & formatting |

ViewModel tests run against in-memory fakes (`testing/FakeRepositories.kt`, wired by
`FakeDataGraph`) that honor the same contracts as the Room implementations.

## Deployment

Test builds ship through Firebase App Distribution. See [DEPLOYMENT.md](DEPLOYMENT.md).
