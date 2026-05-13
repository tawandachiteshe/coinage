# Profile & Settings — Design Spec (Tracker 1.0)

Source: `tracker1-0/project/app/tracker-profile.jsx` and `tracker-settings.jsx`
Theme tokens: `tracker1-0/project/app/tracker-theme.css`

---

## Design Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `--t-ink` | `#1a1230` | borders, text, shadows |
| `--t-ink-2` | `#2c2244` | muted text |
| `--t-paper` | `#fff4e3` | background |
| `--t-paper-2` | `#f6e6c6` | card surfaces, inactive states |
| `--t-paper-white` | `#fffbf2` | card panels |
| `--t-tangerine` | `#ff7a2b` | CTA, FAB |
| `--t-grape` | `#8b5cf6` | accent, AI toggle |
| `--t-butter` | `#ffd84a` | profile hero card, active pills |
| `--t-sky` | `#7cc4ff` | stats, pill groups |
| `--t-mint` | `#5fd7a4` | biometric toggle |
| `--t-coral` | `#ff8da1` | streak stat |
| `--t-cherry` | `#e63946` | destructive actions |

### Paper texture (dot grid)
```css
background-image:
  radial-gradient(rgba(26,18,48,0.04) 1px, transparent 1.4px),
  radial-gradient(rgba(26,18,48,0.025) 1px, transparent 1.2px);
background-size: 14px 14px, 7px 7px;
background-position: 0 0, 3px 3px;
```
Not yet implemented in Compose — would need `drawBehind` Canvas with two `drawCircle` passes tiled across the surface.

---

## Profile Screen

### ID Hero Card
- Background: `Butter`, corner radius 22dp, border 2dp ink, shadow 4/5dp
- Tape strip decoration: 64×18dp, `#B3FF8A4D`, rotated −6°, top-start of card
- Avatar: 72×72dp circle, `Tangerine` fill, 2dp ink border, rotated −4°
- Name: 24sp Bold
- Subtitle: 16sp Italic Serif ("steady saver · joined feb '26")
- Star accent: 28sp Coral, rotated 14°

### Stats Row
Three equal-width `StickerCard`s with slight alternating tilt (−1.2°, +1.4°, −0.6°).

| Stat | Color |
|------|-------|
| `$24k tracked` | Mint |
| `3 wk streak` | Coral |
| `6 jars` | Sky |

### Badges ("Stickers earned")
- Title: "Stickers " (Bold) + "earned" (Italic Serif, Grape)
- Layout: **`FlowRow`** with `horizontalArrangement = spacedBy(12.dp)`, `verticalArrangement = spacedBy(12.dp)` — wraps onto multiple lines on narrow screens
- Each badge: 64×80dp, `RoundedCornerShape(12.dp)`, 1.6dp ink border, shadow 2.5/3dp
- Tilt: alternating −2° / +2° (`if (i % 2 == 0) -2f else 2f`)
- Locked badge (index 4): `color.copy(alpha = 0.45f)` overlay

Badges list:
| Glyph | Label | Color |
|-------|-------|-------|
| ❄ | first save | Sky |
| ★ | on a roll | Butter |
| ▲ | mountain mover | Coral |
| ◐ | half full | Mint |
| ? | ? (locked) | Paper2 |

### Link Rows
Four `StickerCard` rows (PaperWhite bg, alternating ±0.4°/+0.6° tilt):
- Account · Maya Rivera → Face ID linked
- Categories & jars → 6 active
- Export your data → JSON or CSV
- Open Settings → CTA glyph, Tangerine icon bg

---

## Settings Screen

### SettingsRow component
Design uses `padding: '12px 0'` with a **dashed bottom border** — no fixed height.

```kotlin
// Separator: dashed Canvas line replacing solid Box
Canvas(modifier = Modifier.fillMaxWidth().height(1.3.dp)) {
    drawLine(
        color = TrackerColors.Ink.copy(alpha = 0.18f),
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1.3.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f),
    )
}
```

Row uses natural height from `padding(vertical = 12.dp)` only — no `.height(...)` override.

### Toggle component
```
width: 48dp, height: 28dp
border: 1.6dp ink
background: color (on) / Paper2 (off)
shadow: popShadow 2/2dp

thumb: 20×20dp circle
  position x: 24dp (on) / 2dp (off)
  position y: 2dp
  background: Paper
  border: 1.4dp ink
```
Animate with `animateFloatAsState` on the x offset for smooth slide — currently uses static `.offset(x = if (on) 24.dp else 2.dp)`.

### PillGroup
Pills: `RoundedCornerShape(999.dp)`, 1.4dp ink border, `padding(horizontal = 9.dp, vertical = 4.dp)`.
Active: fill with group color + `popShadow(1.5/2dp)`. Inactive: Paper fill.

### Theme Picker
Three swatch cards side-by-side. Each card:
- 76×18dp colour strip (4 equal segments), 4dp rounded corners, 1dp ink border
- Label 11sp SemiBold below
- Active: 2dp border + `popShadow(3/4dp)`. Inactive: 1.5dp + `popShadow(1.5/2dp)`

Themes:
| Name | Swatches |
|------|---------|
| Tropicana | Tangerine · Grape · Butter · Sky |
| Sour candy | #c8ff3d · #ff5bb0 · #3b6dff · Paper |
| Soft serve | #ffb59c · #7ce0c0 · #c9b8ff · Paper |

### Sections
1. **Theme** — `PaperWhite` StickerCard
2. **General** — currency, week start, round-up, voice
3. **Privacy & sync** — biometric, AI assistant
4. **Google Sheets** — `GoogleConnectSection` (platform expect/actual)
5. **Danger zone** — dark Ink card, Butter label, Cherry "Delete everything" pill

### Footer
`"Tracker 1.0 · build 26.05 · made with care"` — 10sp Monospace, 50% ink opacity.