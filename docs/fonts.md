# Fonts — Tracker1.0

The design uses four typefaces. System fallbacks are in place right now (`FontFamily.Default`, `FontFamily.Serif`, `FontFamily.Monospace`). Follow the steps below to swap in the real fonts.

---

## Typefaces

| Role | Font | Weight(s) | Where used in code |
|------|------|-----------|-------------------|
| Headings & body | **Bricolage Grotesque** | 400, 500, 600, 700, 800 | `FontFamily.Default` → replace with `TrackerSans` |
| Italic accent words | **Instrument Serif** | 400 regular + italic | `FontFamily.Serif` → replace with `TrackerSerif` |
| Labels, eyebrows, timestamps, mono data | **JetBrains Mono** | 400, 500, 700 | `FontFamily.Monospace` → replace with `TrackerMono` |
| Hand-drawn annotations (optional) | **Caveat** | 500, 700 | not yet used in screens |

All four are free on [Google Fonts](https://fonts.google.com).

---

## How to bundle them (Compose Multiplatform)

### 1. Download TTF files

From Google Fonts download these files and rename them as shown:

```
Bricolage Grotesque
  BricolageGrotesque-Regular.ttf    (weight 400)
  BricolageGrotesque-Medium.ttf     (weight 500)
  BricolageGrotesque-SemiBold.ttf   (weight 600)
  BricolageGrotesque-Bold.ttf       (weight 700)
  BricolageGrotesque-ExtraBold.ttf  (weight 800)

Instrument Serif
  InstrumentSerif-Regular.ttf
  InstrumentSerif-Italic.ttf

JetBrains Mono
  JetBrainsMono-Regular.ttf         (weight 400)
  JetBrainsMono-Medium.ttf          (weight 500)
  JetBrainsMono-Bold.ttf            (weight 700)

Caveat (optional)
  Caveat-Medium.ttf                 (weight 500)
  Caveat-Bold.ttf                   (weight 700)
```

### 2. Place files in the resources directory

```
composeApp/src/commonMain/composeResources/font/
  BricolageGrotesque-Regular.ttf
  BricolageGrotesque-Medium.ttf
  BricolageGrotesque-SemiBold.ttf
  BricolageGrotesque-Bold.ttf
  BricolageGrotesque-ExtraBold.ttf
  InstrumentSerif-Regular.ttf
  InstrumentSerif-Italic.ttf
  JetBrainsMono-Regular.ttf
  JetBrainsMono-Medium.ttf
  JetBrainsMono-Bold.ttf
  Caveat-Medium.ttf      (optional)
  Caveat-Bold.ttf        (optional)
```

The `font/` subfolder under `composeResources` is the convention required by the Compose Multiplatform resources API.

### 3. Create `TrackerTypography.kt`

Create this file at `composeApp/src/commonMain/kotlin/com/tawandachiteshe/expensify/ui/theme/TrackerTypography.kt`:

```kotlin
package com.tawandachiteshe.expensify.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import expensify.composeapp.generated.resources.Res
import expensify.composeapp.generated.resources.*
import org.jetbrains.compose.resources.Font

val TrackerSans: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.BricolageGrotesque_Regular,  weight = FontWeight.Normal),
        Font(Res.font.BricolageGrotesque_Medium,   weight = FontWeight.Medium),
        Font(Res.font.BricolageGrotesque_SemiBold, weight = FontWeight.SemiBold),
        Font(Res.font.BricolageGrotesque_Bold,     weight = FontWeight.Bold),
        Font(Res.font.BricolageGrotesque_ExtraBold,weight = FontWeight.ExtraBold),
    )

val TrackerSerif: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.InstrumentSerif_Regular, weight = FontWeight.Normal, style = FontStyle.Normal),
        Font(Res.font.InstrumentSerif_Italic,  weight = FontWeight.Normal, style = FontStyle.Italic),
    )

val TrackerMono: FontFamily
    @Composable get() = FontFamily(
        Font(Res.font.JetBrainsMono_Regular, weight = FontWeight.Normal),
        Font(Res.font.JetBrainsMono_Medium,  weight = FontWeight.Medium),
        Font(Res.font.JetBrainsMono_Bold,    weight = FontWeight.Bold),
    )
```

> Note: `@Composable get()` is required because `Font(Res.font.*)` must be called inside a composition. If you need the font family outside composition (e.g. in a `TextStyle` constant), use `loadFont()` with a coroutine instead.

### 4. Replace system fallbacks in screens

Do a project-wide find-and-replace:

| Find | Replace with |
|------|-------------|
| `FontFamily.Default` | `TrackerSans` |
| `FontFamily.Serif` | `TrackerSerif` |
| `FontFamily.Monospace` | `TrackerMono` |

All three appear in:
- `ui/components/TrackerPrimitives.kt`
- `ui/components/TrackerTabBar.kt`
- Every screen under `feature/`

### 5. Verify with a Gradle sync

After adding the TTF files, run a Gradle sync. The KSP resource generator will pick up the new font files and generate `Res.font.*` accessors automatically. If accessors are missing, check that the file names exactly match what you reference in code (case-sensitive).

---

## Why `@Composable get()` instead of a `val`?

`Font(resource: FontResource)` calls `rememberFont` internally, which requires a composition context. Declaring the `FontFamily` as a top-level `val` (outside composition) will throw at runtime. The `@Composable get()` pattern is the standard workaround until Compose Multiplatform provides a non-composable font loader.

---

## CSS reference (original design)

```css
font-family: "Bricolage Grotesque", ui-sans-serif, system-ui, sans-serif;  /* --t-sans */
font-family: "Instrument Serif", ui-serif, Georgia, serif;                 /* --t-serif */
font-family: "JetBrains Mono", ui-monospace, monospace;                    /* --t-mono */
font-family: "Caveat", cursive;                                            /* --t-hand */
```