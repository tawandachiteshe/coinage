# Tracker 1.0 — Backlog

Items are roughly priority-ordered within each section. "Done" means merged to the working branch.

---

## In progress / next up

- [ ] **Real date in Home header** — "Tue · May 12" is hardcoded; derive from `kotlinx.datetime.Clock.System.now()`
- [ ] **Profile — Account row action** — tapping "Account" row should open an edit-name sheet
- [ ] **Profile — Categories row action** — tapping "Categories & jars" should navigate to `JarsManager`
- [ ] **Profile — Export row action** — wire up CSV/JSON export (stub ViewModel + file picker)
- [ ] **Onboarding → Home skip logic** — if name already saved in `UserProfile`, skip onboarding on relaunch

---

## Features not yet built

### Data / persistence
- [ ] `Goals` SQLDelight table + `GoalRepository` fully wired (schema exists, UI is static)
- [ ] `Debt` SQLDelight table + `DebtRepository` fully wired (schema exists, UI is static)
- [ ] Category budget limits — `budget_limit` column exists, not surfaced in UI
- [ ] Currency conversion — `CurrencyRepository` exists, rates fetch not triggered on launch

### Screens
- [ ] **Add screen — Income type** — currently defaults to Expense swipe flow; Income needs a separate path
- [ ] **Debt screen — live data** — mountain hero card and snowball plan are still hardcoded
- [ ] **Goals screen — live data** — jar progress bars are static
- [ ] **Insights — spending bar chart** — `categoryTotals` state is ready; stacked bar composable needed

### Auth / security
- [ ] Biometric gate on cold launch (Face ID / fingerprint) — toggle in Settings is UI-only
- [ ] Onboarding skip check — `UserProfile.getName()` on app start → navigate to Home if set

### Google Sheets sync
- [ ] OAuth2 flow (Android: Google Identity; iOS: ASWebAuthenticationSession)
- [ ] Sheets API v4 write — `POST /spreadsheets` on first sync, `batchUpdate` subsequently
- [ ] Store spreadsheet ID in DataStore after first create
- [ ] "Sync to Sheets" button in Settings → GoogleConnectSection

### AI assistant
- [ ] Auto-categorize endpoint — Settings toggle is UI-only, no backend wired
- [ ] Monthly write-up generation

---

## Design / polish

- [ ] **Real fonts** — Bricolage Grotesque, Instrument Serif, JetBrains Mono TTFs not bundled yet (see `docs/fonts.md`)
- [ ] **Paper dot-grid texture** — CSS spec exists in `docs/design-profile-settings.md`; needs `drawBehind` Canvas impl
- [ ] **Home header date** — replace hardcoded "Tue · May 12" with live `kotlinx.datetime`
- [ ] **Profile stats** — `$24k tracked`, `3 wk streak`, `6 jars` are all hardcoded; hook to real DB aggregates
- [ ] **Badge unlock logic** — badges in ProfileScreen are hardcoded; define unlock conditions and persist state
- [ ] **Toggle animation** — Settings toggle has smooth thumb animation ✅; same pattern not applied to any other toggle in AddScreen

---

## Tech debt

- [ ] Old screens (`LoginScreen`, `DashboardScreen`, `ExpenseListScreen`) still compile but are not wired into nav — delete or migrate
- [ ] `HomeState.userName` and `ProfileUiState.name` both load from `UserProfileRepository` separately; consider a shared `UserSession` singleton
- [ ] `AddViewModel` fetches categories on every open; cache with `stateIn`
- [ ] Hardcoded `"USD"` default in `TransactionRepository.insert` — should read `baseCurrencyCode` from `CurrencyRepository`

---

## Done ✅

- [x] SQLDelight schema — Transaction, Category, Debt, Goal, Currency, UserProfile tables
- [x] `TransactionRepository` — CRUD + date range queries + biggest expense + top merchant
- [x] `CategoryRepository` — active expense jars
- [x] `InsightsViewModel` — parallel async loads, moments data (biggest splurge, top merchant, tx count)
- [x] `TrackerScaffold` — Material3 Scaffold wrapper with pinned back button + tab bar
- [x] All screens migrated to `TrackerScaffold`
- [x] `SettingsScreen` — dashed separator, animated toggle thumb, theme picker, pill groups, danger zone
- [x] `ProfileScreen` — ID card, stats, FlowRow badges, link rows with Lucide icons + click handlers
- [x] `OnboardingScreen` — name input on last step, `imePadding` for keyboard
- [x] `UserProfileRepository` + `OnboardingViewModel` + `ProfileViewModel`
- [x] Home greeting uses real user name from DB
- [x] Profile avatar in Home header → navigates to Profile