# Stickers — Badge System Plan

Stickers are small achievement badges shown on the Profile screen. They unlock based on real app data — no server needed.

---

## Visual spec

Each badge is a 64×80dp rounded rectangle (12dp corners) in the `FlowRow` grid.

| State    | Border alpha | Card alpha | Shadow         |
|----------|-------------|------------|----------------|
| Locked   | 0.35        | 0.38       | none           |
| Unlocked | 1.0         | 1.0        | `popShadow(cornerRadius=12.dp, offsetX=2.5.dp, offsetY=3.dp)` |

Glyph: 22sp, `TrackerColors.Ink`
Label: 9sp, `FontFamily.Monospace`, `letterSpacing=0.4sp`, `lineHeight=11sp`, centred, 4dp horizontal padding

Tilt alternates: even index → `-2f`, odd index → `+2f`

---

## Badge catalog

### Implemented (v1)

| Glyph | Name | Color | Unlock condition | State field |
|-------|------|-------|-----------------|-------------|
| ❄ | first save | `Sky` | `txCount >= 1` | `hasFirstSave` |
| ★ | on a roll | `Butter` | `txCount >= 5` | `hasOnARoll` |
| ▲ | mountain mover | `Coral` | `totalTracked >= $1,000` | `hasMountainMover` |
| ◐ | half full | `Mint` | `jarCount >= 3` | `hasHalfFull` |

All four are derived live from `TransactionRepository` + `CategoryRepository` — no separate persistence needed.

---

### Planned — next batch

| Glyph | Name | Color | Unlock condition |
|-------|------|-------|-----------------|
| ✦ | streak keeper | `Grape` | transactions on 7 consecutive days |
| ⬡ | jar master | `Tangerine` | all active jars have at least 1 transaction |
| ↓ | debt slayer | `Cherry` | at least one debt paid off |
| ◎ | goal getter | `Butter` | at least one goal reached 100% |
| ∞ | long hauler | `Sky` | app used for 30+ days (join date delta) |
| ✿ | big spender | `Coral` | single transaction > $500 |

---

### Locked / mystery (always hidden until earned)

One permanent `?` tile shown at the end of the grid with `TrackerColors.Paper2` — signals there are more stickers to discover without revealing them.

---

## Persistence strategy

**Current approach:** badges are derived on every `ProfileViewModel` collect — no separate table.

This works for threshold-based badges (`txCount >= N`, `total >= N`). It breaks for:
- **Streak keeper** — requires querying transactions grouped by day; add `getTransactionDates(): Flow<List<LocalDate>>` to `TransactionRepository`
- **Jar master** — requires a join: jars that have ≥ 1 transaction each; add `getJarsWithActivity(): Flow<List<CategoryId>>` to `CategoryRepository`
- **Debt slayer** — requires `DebtRepository.getPaidOff(): Flow<List<Debt>>`
- **Goal getter** — requires `GoalRepository.getCompleted(): Flow<List<Goal>>`
- **Long hauler** — computed from `UserProfile.joined_at` vs `Clock.System.now()`; no new query needed
- **Big spender** — add `getMaxSingleAmount(): Flow<Double>` to `TransactionRepository`

No `EarnedBadge` table needed yet. Badges that require point-in-time detection (e.g. "first time X happened") should be stored when the condition first triggers — add a `badges` column to `UserProfile` as a comma-separated list of badge IDs when that pattern becomes necessary.

---

## ProfileUiState additions (next batch)

```kotlin
data class ProfileUiState(
    // ... existing fields ...
    val hasStreakKeeper: Boolean = false,
    val hasJarMaster: Boolean = false,
    val hasDebtSlayer: Boolean = false,
    val hasGoalGetter: Boolean = false,
    val hasLongHauler: Boolean = false,
    val hasBigSpender: Boolean = false,
)
```

---

## Implementation order

1. **`getTransactionDates()`** in `TransactionRepository` → streak keeper
2. **`getMaxSingleAmount()`** in `TransactionRepository` → big spender (trivial)
3. **Long hauler** — `UserProfile.joined_at` already stored; derive in VM (no repo change)
4. **Jar master** — needs category × transaction join query
5. **Debt slayer / Goal getter** — blocked on those repos being fully wired (see backlog)