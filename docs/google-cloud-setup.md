# Google Cloud Setup — Drive + Sheets OAuth (Android)

Required before the Google Drive backup and Sheets export features will work.

---

## 1 — Create a project & enable APIs

1. Go to [console.cloud.google.com](https://console.cloud.google.com)
2. **New project** → name it (e.g. `Coinage`)
3. **APIs & Services → Library** — search and enable both:
   - `Google Drive API`
   - `Google Sheets API`

---

## 2 — Configure the OAuth consent screen

**APIs & Services → OAuth consent screen**

| Field | Value |
|---|---|
| User type | **External** |
| App name | Coinage |
| User support email | your email |
| Developer contact | your email |

**Scopes → Add or remove scopes** — add these two manually:

```
https://www.googleapis.com/auth/drive.appdata
https://www.googleapis.com/auth/spreadsheets
```

**Test users → Add users** — add your Google account email.

The app stays in "Testing" mode until you publish it. Only email addresses listed here can complete the OAuth flow during testing.

---

## 3 — Create an Android OAuth 2.0 client ID

**APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client ID**

| Field | Value |
|---|---|
| Application type | **Android** |
| Package name | `com.tawandachiteshe.coinage` |
| SHA-1 fingerprint | see below |

**Get your debug SHA-1:**

```bash
./gradlew signingReport
```

Look for the `debug` variant block:

```
Variant: debug
Config: debug
Store: ~/.android/debug.keystore
Alias: AndroidDebugKey
SHA1: AB:CD:EF:...   ← copy this
```

> The Android client ID does **not** need to be pasted into the app. GIS (`Identity.getAuthorizationClient`) identifies the app automatically from the package name + signing certificate. The `clientId` constructor parameter in `GoogleAuthRepositoryImpl` is currently unused and can be removed.

For a release build, repeat this step with the release keystore SHA-1 and create a second Android client ID.

---

## 4 — Debugging auth failures

Run with Logcat filtered to `GoogleAuth` and `GoogleConnect` tags:

```bash
adb logcat -s GoogleAuth:* GoogleConnect:*
```

The app logs every step: `requestAuthorization` result, whether `hasResolution` or `accessToken` is set, the `onActivityResult` data, `getAuthorizationResultFromIntent` outcome, `trySilentAuth` result, and final `isConnected` state.

### Common failure causes

| Symptom | Likely cause |
|---|---|
| `hasResolution=true` forever, token never arrives | SHA-1 mismatch (debug vs release keystore, or wrong project) |
| Account picker appears then closes, nothing happens | Test user email not added to consent screen |
| Token saved but Drive/Sheets API calls return 403 | Scopes not added to the consent screen, or APIs not enabled |
| `trySilentAuth THREW` in logs | Drive API or Sheets API not enabled in the project |
| Works on debug, fails on Play Store build | Release keystore SHA-1 not registered as a separate Android client ID |