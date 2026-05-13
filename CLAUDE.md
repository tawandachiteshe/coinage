# Expensify — Tracker1.0

## Product Vision

Personal finance tracker for individuals. Tracks **debt, income, and spending** across categories, then generates reports. All data is **local-first** (SQLite via SQLDelight). Cloud sync is handled by exporting to a user-owned **Google Sheet** — no proprietary backend required.

The Google Sheets layer is one-way: the app is always the source of truth, Sheets is a read-only export/report. This eliminates conflict resolution, gives users a living spreadsheet they can share or pivot, and costs nothing to operate.

KMP app (Compose Multiplatform UI) shared across Android, iOS, Desktop (JVM), Web (JS + Wasm).

---

## Architecture

### Modules
| Module | Purpose |
|--------|---------|
| `:composeApp` | Shared Compose Multiplatform UI + app-level DI wiring |
| `:shared` | Domain models, interfaces, Result/Error types — all targets |
| `:server` | Ktor backend |

### Layer rules
- **Domain** (`:shared`) — pure Kotlin, no Android/Compose deps. `Result<D,E>`, `DataError`, `AuthRepository`, `Expense`, `User`.
- **Data** (`:composeApp/data`) — Ktor `HttpClient`, `TokenStorage`, `SafeCall`, repository impls.
- **Feature** (`:composeApp/feature/*`) — MVI: `State`, `Action`, `Event`, `ViewModel`. Screen composables are dumb.
- **UI** (`:composeApp/ui`) — design system primitives, no business logic.

### DI (Koin)
- `androidModule` — Android-only bindings: `DatastoreTokenStorage` via `preferencesDataStore`.
- `appModule` — shared bindings: `HttpClient`, `AuthRepository`, `ExpenseRepository`, ViewModels.
- Both modules started in `ExpensifyApp.onCreate()`.

---

## Design System — Tracker1.0 "Receipt Sticker Book"

Design source: Claude Design export (`Tracker.html`). Tropicana palette, playful sticker-book aesthetic for young adults.

### Colours (`ui/theme/TrackerColors.kt`)
```
Ink        #1a1230   Ink2       #2c2244
Paper      #fff4e3   Paper2     #f6e6c6   Paper3  #efd9b1   PaperWhite #fffbf2
Tangerine  #ff7a2b   Grape      #8b5cf6   Butter  #ffd84a
Sky        #7cc4ff   Mint       #5fd7a4   Coral   #ff8da1   Cherry     #e63946
```

### Fonts (add as resource assets — currently using system fallbacks)
- **Bricolage Grotesque** → headings, body (`FontFamily.Default` until bundled)
- **Instrument Serif** → italic accent words (`FontFamily.Serif`)
- **JetBrains Mono** → labels, eyebrows, timestamps (`FontFamily.Monospace`)
- **Caveat** → hand-drawn annotations (optional, not yet used in code)

To bundle: download TTFs → `composeApp/src/commonMain/composeResources/font/` → load with `Font(resource = ...)`.

### Key primitives (`ui/components/TrackerPrimitives.kt`)
- `popShadow(shadowColor, cornerRadius, offsetX, offsetY)` — hard pixel offset shadow (CSS `box-shadow: Xpx Ypx 0 ink` equivalent). Uses `Modifier.layout` to expand bounds + `drawBehind` to render shadow in the extra space.
- `StickerCard(bgColor, tilt, ...)` — coloured rounded card with ink border + `popShadow`.
- `TapeStrip` — semi-transparent yellow tape decoration.
- `ProgressJar` — vertical fill bar used in the jars grid.
- `StripedProgressBar` — horizontal progress bar with % label.
- `ReceiptRow` — transaction list item with emoji icon, merchant, amount.
- `PageHeader(title, italicWord, eyebrow, kicker, accent)` — screen header pattern.

### Tab bar (`ui/components/TrackerTabBar.kt`)
- Paper background, 2px ink border, hard shadow, tape strip centred on top edge.
- Active tab = butter sticker square with ink border.
- FAB = tangerine circle at bottom-right, offset above bar.

---

## Screens

| Route | File | Notes |
|-------|------|-------|
| `Onboarding` | `feature/onboarding/OnboardingScreen.kt` | 3-step, sticker art, start destination |
| `Home` | `feature/home/HomeScreen.kt` | Balance card (Y/M/W toggle), jars 3-col grid, receipts |
| `Goals` | `feature/goals/GoalsScreen.kt` | Goal cards, striped progress, +$10/25/50 nudge chips |
| `Debt` | `feature/debt/DebtScreen.kt` | Mountain hero card, snowball plan, per-debt cards |
| `Insights` | `feature/insights/InsightsScreen.kt` | Stacked bar chart, moments collage grid |
| `Add` | `feature/add/AddScreen.kt` | Drag-to-categorize card stack (Need/Want/Save/Skip) |
| `Profile` | `feature/profile/ProfileScreen.kt` | ID card, stat tiles, badges, link rows |
| `Settings` | `feature/settings/SettingsScreen.kt` | Theme picker, toggles, pill groups, danger zone |

Navigation start: `Onboarding → Home`. Tab bar navigates Home/Goals/Debt/Insights. FAB → Add.

Old screens (`LoginScreen`, `DashboardScreen`, `ExpenseListScreen`) still exist and compile — not wired into nav but ViewModels remain registered in Koin.

---

## Data layer

### TokenStorage
- Interface in `composeApp/commonMain/data/TokenStorage.kt`
- Android impl: `composeApp/androidMain/data/DatastoreTokenStorage.kt` (uses `androidx.datastore:datastore-preferences`)
- `datastore-preferences-core` must NOT be in `commonMain` — it doesn't publish js/wasmJs artifacts. Keep it in `androidMain` only.

### HttpClient (`data/HttpClientFactory.kt`)
- Ktor with `ContentNegotiation` (JSON), `Logging` (Kermit), `Auth` (Bearer token from `TokenStorage`).

### SafeCall (`data/SafeCall.kt`)
- Wraps Ktor calls into `Result<T, DataError.Network>`. Maps HTTP status codes to typed errors.

### AuthRepository
- Interface in `:shared` → `domain/repository/AuthRepository.kt`
- Impl in `feature/auth/AuthRepositoryImpl.kt`. Caches token in memory; call `isLoggedIn()` returns false after process death (safe default — routes to onboarding).

### ExpenseRepository
- Interface + impl in `feature/expenses/`.

---

## Error handling
`Result<D, E : Error>` and `EmptyResult<E>` in `:shared/domain/`. Extensions: `onSuccess`, `onError`, `map`, `asEmptyResult`.
`DataError.Network` and `DataError.Local` enums cover all typed failure cases.

---

## Key dependencies
```
Compose Multiplatform  1.10.3
Kotlin                 2.3.21
Koin                   4.1.0
Ktor                   3.4.3
Navigation Compose     2.9.2
DataStore              1.1.2   (androidMain only)
Kermit logging         2.0.5
kotlinx-datetime       0.6.2
```

---

## Local persistence — SQLDelight

All financial data lives in SQLite via **SQLDelight** (KMP-native, works on Android/iOS/JVM/JS).
Reference: https://kotlinlang.org/docs/multiplatform/multiplatform-ktor-sqldelight.html

Tables to implement:
- `transactions` — amount, type (income/expense), category, date, notes
- `debts` — creditor, principal, current_balance, interest_rate, due_date
- `categories` — name, icon, budget_limit
- `goals` — name, target_amount, saved_amount, deadline

SQLDelight generates type-safe query functions from `.sq` files placed in `composeApp/src/commonMain/sqldelight/`.

---

## Google Sheets sync

**Architecture:** app is source of truth → manual "Sync to Sheets" in Settings → Sheets is read-only export.

No two-way sync. No conflict resolution needed.

Implementation plan:
1. **OAuth2** — use Google Identity (Android) / ASWebAuthenticationSession (iOS) via `expect/actual`. Store token in `DatastoreTokenStorage`.
2. **Sheets API v4** — pure HTTP via Ktor. Key endpoints:
   - `POST /spreadsheets` — create sheet on first sync
   - `PUT /spreadsheets/{id}/values/{range}:batchUpdate` — overwrite with current data
3. **Sheet layout** — one tab per data type: `Transactions`, `Debts`, `Goals`, `Summary`. Summary tab uses Sheets formulas for totals/charts so the user gets reports for free.
4. Store the spreadsheet ID in DataStore after first create so subsequent syncs update the same file.

Rate limits: 100 req / 100 s per user. A full sync is ~4 requests (one per tab). No polling needed.

---

## Planned / not yet built
- SQLDelight schema and repository impls (next priority).
- Google Sheets OAuth2 + sync (after local persistence is stable).
- Real auth flow wired to biometrics (Face ID / fingerprint) — `OnboardingScreen` has the CTA placeholder.
- AI assistant toggle in Settings is UI-only — no backend yet.
- Real font assets (Bricolage Grotesque, Instrument Serif, JetBrains Mono).