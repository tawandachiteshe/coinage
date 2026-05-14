# OCR Receipt Scanning — Plan

Users can photograph a receipt and have the amount, merchant, and date pre-filled in the Add screen. All OCR runs on-device — no server, no API cost, no data leaves the phone.

---

## Platform OCR engines

| Platform | Engine | Notes |
|----------|--------|-------|
| Android | ML Kit Text Recognition v2 | Free, on-device, ships with Play Services. Add `com.google.android.gms:play-services-mlkit-text-recognition`. |
| iOS | Vision `VNRecognizeTextRequest` | Built-in since iOS 13, no dependency needed. |
| Desktop (JVM) | Tesseract via `tess4j` | Optional — lower priority. |
| Web | Not planned | Camera API limitations; can add later via WebAssembly Tesseract. |

KMP boundary: the OCR call lives behind an `expect/actual` interface. Shared code handles parsing and state; platform code handles camera + recognition.

---

## Architecture

```
commonMain
  domain/
    ReceiptScanResult(merchant, amount, date, rawText)
    ReceiptScanner  ← expect interface
  feature/add/
    AddViewModel    ← new action: OnReceiptScanned(ReceiptScanResult)

androidMain
  feature/add/
    ReceiptScanner.android.kt   ← ML Kit impl + CameraX photo capture

iosMain
  feature/add/
    ReceiptScanner.ios.kt       ← Vision framework impl + UIImagePickerController
```

### `ReceiptScanner` interface

```kotlin
// commonMain
data class ReceiptScanResult(
    val merchant: String?,
    val amount: Double?,
    val date: LocalDate?,
    val rawText: String,
)

expect class ReceiptScanner {
    suspend fun scan(imageBytes: ByteArray): ReceiptScanResult
}
```

`imageBytes` is the JPEG from camera — keeps the interface pure Kotlin with no platform type leakage.

---

## Text parsing (commonMain)

After OCR returns raw text, a shared `ReceiptParser` extracts fields with regex + heuristics. All parsing happens in common code so it's testable without a device.

### Amount

```
Regex: (?:total|amount|due|charged)[^\d]*([\d,]+\.?\d{0,2})
Fallback: largest dollar value in the text
```

Multiple candidates → pick the one immediately after a "total"/"amount" keyword. If still ambiguous, pick the largest.

### Date

```
Patterns: MM/dd/yyyy · dd-MM-yyyy · MMM d, yyyy · yyyy-MM-dd
Library: kotlinx-datetime LocalDate.parse with custom formatters
Fallback: today's date (user can adjust in the form)
```

### Merchant name

```
Heuristic: first non-numeric line in the top 30% of the raw text, title-cased.
Fallback: empty string (user types it in)
```

Receipt layout is almost always: store name → address → items → total. The first line is reliable for merchant.

---

## AddViewModel integration

### New action and state

```kotlin
// AddState additions
val isScanning: Boolean = false
val scanError: String? = null

// AddAction additions
data object OnScanReceipt : AddAction
data class OnReceiptScanned(val result: ReceiptScanResult) : AddAction
data class OnScanError(val msg: String) : AddAction
```

### Handler

```kotlin
is AddAction.OnReceiptScanned -> _state.update { cur ->
    cur.copy(
        merchant = result.merchant ?: cur.merchant,
        amount   = result.amount?.toString() ?: cur.amount,
        isScanning = false,
        scanError  = null,
    )
    // date: emit AddEvent.SuggestDate(result.date) so the screen can show a confirm chip
}
```

Amount and merchant pre-fill silently. Date surfaces as a confirmation chip ("Receipt says May 12 — use it?") because auto-setting the date without confirmation is confusing if the receipt is old.

---

## AddScreen UX

### Entry point

Small camera icon button next to the amount field (or a "Scan receipt" pill above the card stack). Tapping it:

1. Requests `CAMERA` permission if not granted.
2. Launches platform camera picker (CameraX on Android, `UIImagePickerController` on iOS).
3. Shows a loading shimmer over the card while OCR runs.
4. Pre-fills fields; toasts "Receipt scanned ✓".

### Prefill behaviour

| Field | Behaviour |
|-------|-----------|
| Amount | Silently pre-filled; user can edit |
| Merchant | Silently pre-filled; user can edit |
| Date | Shown as a tappable confirmation chip: "Use May 12?" |
| Category | Not pre-filled by OCR — category hint from merchant name is a v2 feature |
| Notes | `rawText` optionally attached as note (off by default, toggle in Settings) |

### Error states

- Camera permission denied → sheet with "Allow camera access in Settings" deep link
- OCR returns no text → toast "Couldn't read receipt — fill in manually"
- Parsing finds no amount → pre-fill merchant only, amount field highlighted

---

## Permissions

**Android** — add to `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera" android:required="false" />
```

Request at runtime via `ActivityResultContracts.RequestPermission` in `MainActivity` (or a dedicated `rememberPermissionState` wrapper).

**iOS** — add to `Info.plist`:
```xml
<key>NSCameraUsageDescription</key>
<string>Used to scan receipts and pre-fill transaction details.</string>
```

---

## Dependencies to add

```kotlin
// androidMain only
implementation("com.google.android.gms:play-services-mlkit-text-recognition:19.0.1")

// CameraX (already common in Android apps; add if not present)
implementation("androidx.camera:camera-camera2:1.4.x")
implementation("androidx.camera:camera-lifecycle:1.4.x")
implementation("androidx.camera:camera-view:1.4.x")
```

No new commonMain dependencies — kotlinx-datetime is already in the project.

---

## Implementation order

1. **`ReceiptScanResult` + `ReceiptParser`** in commonMain — pure Kotlin, fully unit-testable, no device needed
2. **Android `ReceiptScanner` impl** — ML Kit + CameraX, returns `ByteArray` → `scan()`
3. **`AddViewModel` actions** — `OnScanReceipt`, `OnReceiptScanned`, `OnScanError`
4. **AddScreen camera button + shimmer + prefill UI**
5. **iOS `ReceiptScanner` impl** — Vision + UIImagePickerController
6. **v2 (optional):** merchant → category hint using a local lookup table of common store names

---

## What's NOT in scope (v1)

- Gallery photo import (camera-only for now; straightforward to add later)
- Multi-receipt batch scanning
- Itemised line extraction (only totals)
- Cloud OCR (Google Document AI, AWS Textract) — on-device is good enough for receipts
- Category auto-suggestion from merchant name (planned for v2 above)