# Tachiwin Keyboard — Project TODO

**Goal**: Repurpose FlorisBoard (open-source Android keyboard) into **Tachiwin Tsokgnan**, a keyboard for the Indigenous Languages of Mexico. Strip out unrelated world languages, disable/hide advanced NLP features, simplify the UI, and add 100+ Mexican indigenous language layouts based on the existing Spanish (es-ES) / Latin base.

---

## ✅ Already Done (previous commits)

| Commit | Change |
|--------|--------|
| `1095490` | Disabled Rust native build (commented out `native` lib dependency in `app/build.gradle.kts` and `settings.gradle.kts`). Updated strings + license files. |
| `ab71d90` | Project name change: package name `dev.patrickgold.florisboard` kept but `applicationId` changed to `org.tachiwin.tsokgnan`. |
| `23cc4dd` | Copyright headers updated to `Tachiwin Tutunakú` across all files (script-level change). |
| `2fb1d7e` | Brand name change: all visible "FlorisBoard" → "Tsokgnan" in strings.xml, manifests, comments. |
| `520e72f` | Minor copyright text fixes. |
| `afcb327` | UI cleanup: commented out Gestures and Extensions from HomeScreen settings; removed welcome/beta toolbox card; disabled notification permission step in SetupScreen; removed notification permission observer; removed setup footer. |

---

## 📋 Task Categories

### A. BRANDING & NAMING (find-and-replace pass)

#### A1. App name / display name  ️✅
- [x] `app_name` changed from "Tachiwin Keyboard" / "FlorisBoard" to **"Teclado Tachiwin"** in **all 41 locale** `values-*/strings.xml` files.
- [x] Remaining "FlorisBoard" references in English & Spanish string values updated → "Tachiwin" (crash dialog, clipboard, notification permission descriptions).
- [x] Hardcoded `"FlorisBoard"` fallback in `Resources.kt` (CompositionLocal) → `"Teclado Tachiwin"`.
- [x] Hardcoded `"FlorisBoard app icon"` in `AboutScreen.kt` → `"Tachiwin app icon"`.
- [x] Hardcoded `"FlorisBoard"` in crash report text (`CrashDialogActivity.kt`) → `"Tachiwin"`.
- [x] Theme compatibility warning in `DevtoolsOverlay.kt` — removed "FlorisBoard" reference.
- [ ] The remaining ~20 references in `FlorisRef.kt` docs (KDoc comments) and `Flog.kt` are internal documentation — nice-to-clean but not urgent.

#### A2. Internal references to "florisboard" in code  ️✅
- [x] **Package name** — **Kept as `dev.patrickgold.florisboard.*`** (safe choice, avoids massive refactor). The `applicationId` is already `org.tachiwin.tsokgnan`.
- [x] **Deep-link scheme**: `AndroidManifest.xml` host changed from `"florisboard"` → `"tachiwin"`.
- [x] **MIME types**: `application/vnd.florisboard.extension+zip` → `application/vnd.tachiwin.extension+zip` (both occurrences in manifest).
- [ ] **Extension IDs** (`org.florisboard.layouts`, etc.) — **SKIP for now**. Purely internal plumbing, never user-visible. Changing would require renaming directories + updating hundreds of cross-references in JSON and Kotlin. Low priority.
- [ ] **File paths** (`app/src/main/assets/ime/keyboard/org.florisboard.*`) — **SKIP for now**. Tied to extension IDs above.
- [x] **URL references** in `strings_dont_translate.xml` updated from `florisboard/florisboard` → `lsjcp/tachiwin_keyboard`. Update to your actual repo URL when created.
- [x] **Copyright headers** — Already done ("Tachiwin Tutunakú"). Spot-check confirmed.

#### A3. Play Store / store-facing metadata
- [ ] `.github/FUNDING.yml` and `.github/ISSUE_TEMPLATE/*` — Update for your project.
- [ ] `README.md` — Update description, badges, screenshots.
- [ ] `LICENSE` — Already updated to Tachiwin Tutunakú (Apache 2.0 kept).

---

### B. LANGUAGE STRIPPING (core change)

The app bundles **73 keyboard layouts** and **60+ subtype presets**. We need to keep only:

- **Keep**: English (en-US, en-UK, en-CA, en-AU), Spanish (es-ES, es-US, es-419), Portuguese (pt-PT, pt-BR) — as fallback/main base languages.
- **Remove**: All other world languages — Arabic, Armenian, Azerbaijani, Bengali, Bulgarian, Catalan, Czech, Danish, Dutch, Estonian, Faroese, Finnish, French, German, Greek, Hebrew, Hindi, Hungarian, Icelandic, Igbo, Indonesian, IPA, Japanese, Korean, Kurdish, Norwegian, Persian, Polish, Romanian, Russian, Rusyn, Serbian, Slovenian, Swedish, Tamil, Thai, Turkish, Ukrainian, Urdu, Vietnamese, Warang Citi, etc.
- **Add (LATER)**: 100+ Indigenous Mexican language layouts (mixtec, zapotec, náhuatl, maya, otomí, etc.) — see Section F.

#### B1. Subtype presets (the language picker list)

**File**: `app/src/main/assets/ime/keyboard/org.florisboard.localization/extension.json`

- [ ] Remove all `subtypePresets` entries **except**: `en-US`, `en-UK`, `en-CA`, `en-AU`, `es-ES`, `es-US`, `es-419`, `pt-PT`, `pt-BR`.
- [ ] Keep the `popupMappings` array but you can prune it later (only `"default"`, `"en"`, `"es"`, `"pt"`, `"pt-BR"` are needed).
- [ ] Keep `punctuationRules` array as-is (only `"default"` exists, fine).

#### B2. Keyboard layout JSON files

**Directory**: `app/src/main/assets/ime/keyboard/org.florisboard.layouts/layouts/characters/`

- [ ] Delete ALL `.json` layout files **except**:
  - `qwerty.json` (base for English)
  - `spanish.json` (base for Spanish & all Mexican indigenous layouts)
  - `azerty.json` (optional, keep if French will be kept)
  - `qwertz.json` (optional)
  - `dvorak.json` (optional, alternative layout)
- [ ] Delete corresponding entries from the `"characters"` array in `extension.json` for every deleted layout.

#### B3. Other layout types

**Directories**: `charactersMod/`, `numeric/`, `numericAdvanced/`, `numericRow/`, `phone/`, `phone2/`, `symbols/`, `symbolsMod/`, `symbols2/`, `symbols2Mod/`

- [ ] Prune to only what's needed (western arabic numeric, default symbols, default modifiers).
- [ ] Delete `cjk/`, `persian/`, `arabic/`, `armenian/`, `kurdish/`, `neo2/` etc. specific variants.
- [ ] Update `extension.json` for each section.

#### B4. Popup mapping files

**Directory**: `app/src/main/assets/ime/keyboard/org.florisboard.localization/popupMappings/`

- [ ] Keep: `default.json`, `en.json`, `es.json`, `pt.json`, `pt-BR.json`.
- [ ] Delete all others (55+ files).

#### B5. Currency sets

**Directory**: `app/src/main/assets/ime/keyboard/org.florisboard.currencysets/`

- [ ] Prune to only `dollar`, `euro`, `peso` (or `mexican_peso` — create one if needed).
- [ ] Remove `rial`, `yen`, `ruble`, `baht`, `rupee`, etc.
- [ ] Update `extension.json` for currencysets.

#### B6. Composers

**Directory**: `app/src/main/assets/ime/keyboard/org.florisboard.composers/`

- [ ] Keep `appender` (default). Remove `hangul-unicode`, `kana-unicode`, `telex` (Vietnamese). Clean up `extension.json`.

#### B7. Language packs (NLP/dictionaries)

**Directory**: `app/src/main/assets/ime/languagepack/`

- [ ] Remove `org.florisboard.hanshapebasedbasicpack` (Chinese shape-based).
- [ ] Keep `org.florisboard.languagepack` but evaluate if you need the dictionary binaries. For indigenous languages you'll likely need custom word lists.

#### B8. Themes

**Directory**: `app/src/main/assets/ime/theme/org.florisboard.themes/`

- [ ] Themes are mostly visual and language-agnostic. Keep them all — no changes needed.

---

### C. FEATURE SIMPLIFICATION (hide/disable advanced features)

#### C1. NLP / Suggestion / Spelling (hide)

The app has a full NLP pipeline (suggestions, spell checking, glide typing). Indigenous Mexican languages will not have NLP support initially.

**Target: make these features non-functional but don't crash.**

- [ ] **HomeScreen** (`HomeScreen.kt`) — Already done: Gestures (glide typing) is commented out. The `Typing` option still appears but leads to `TypingScreen` which has suggestion/spelling prefs.
- [ ] **TypingScreen** (`TypingScreen.kt`) — Either:
  - **Option A (simpler)**: Comment out the entire content except Dictionary preference. Or hide all suggestion/correction/spelling prefs behind `visibleIf = { false }`.
  - **Option B (recommended)**: Keep the settings accessible but pre-configure defaults such that NLP is effectively off:
    - `prefs.suggestion.enabled` default to `false`
    - `prefs.spelling.languageMode` default to `OFF`
    - Add a warning card: "Word suggestions and spell checking are not available for Mexican Indigenous languages at this time."
- [ ] **SmartbarScreen** (`SmartbarScreen.kt`) — The smartbar shows suggestion candidates. Either:
  - Set `prefs.smartbar.enabled = false` by default.
  - Or let users enable it — it will show nothing meaningful but won't crash.
- [ ] **SetupScreen** (`SetupScreen.kt`) — Already done: notification permission step removed, notification obs removed, footer removed.

#### C2. Extension system (hide)

- [ ] Already done in HomeScreen (`ExtensionHomeScreen` route is commented out).
- [ ] **Routes.kt** — The `Ext.Home`, `Ext.List`, `Ext.Edit`, `Ext.Export`, `Ext.Import`, `Ext.View`, `Ext.CheckUpdates` composable registrations in `AppNavHost()` are still active. If someone navigates to them directly (deep link), they'll work. Either:
  - Remove those `composableWithDeepLink` entries entirely, OR
  - Leave them (they're harmless if not linked from UI).
- [ ] **AndroidManifest.xml** — The intent filter for `application/vnd.florisboard.extension+zip` can be removed if you don't want extension import.

#### C3. DevTools (hide)

- [ ] **Routes.kt** — Devtools routes (`Devtools.Home`, `Devtools.AndroidLocales`, `Devtools.AndroidSettings`, `Devtools.ExportDebugLog`) are still registered. They're only accessible via debug build or deep link. Leave as-is or remove.

#### C4. Unused preference groups (in AppPrefs)

- [ ] `prefs.glide` (gesture typing) — All glide preferences can be optionally removed from the model if the Gestures screen is fully removed.
- [ ] `prefs.spelling.useContacts`, `prefs.spelling.useUdmEntries` — Already set to `visibleIf = { false }` in TypingScreen. Keep or remove.

#### C5. Notification permission

- [ ] Already handled (SetupScreen changes in commit `afcb327`). Verify `AndroidManifest.xml` still has `<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>` — it's needed for Android 13+ even if you don't request it in setup.

---

### D. UI SIMPLIFICATION

#### D1. Localization screen (language/layout picker) — **MAJOR SIMPLIFICATION**

The current `LocalizationScreen` is complex: it shows a list of user-added subtypes with a FAB to add new ones, which opens `SubtypeEditorScreen` with ~15 dropdown fields (locale, popup mapping, characters layout, symbols layout, composer, currency set, punctuation rule, numeric layouts, phone layouts, NLP providers...).

**Desired behavior**: A simple list of enabled languages where each inherits its config from a preset (es-MX / Spanish / Latin base).

- [ ] **Replace `LocalizationScreen.kt`** with a simplified version:
  - Remove the FAB for "add subtype".
  - Remove the `SubtypeEditorScreen` complexity.
  - Show a list of pre-configured languages (en-US, es-MX, es-ES, plus each indigenous language).
  - Each item = toggle switch to enable/disable.
  - Tapping a language opens a **simple layout selector** (just the characters layout variant, e.g., "QWERTY", "Spanish (Ñ)", "Náhuatl extended").
  - No need for separate symbol/numeric/phone layout selection — inherit from the es-MX base.
- [ ] **Simplify `SubtypeEditorScreen.kt`**: Either remove it entirely (navigation routes remain but show a "coming soon" or redirect), or gut it to just:
  - Language selector (locale)
  - Characters layout dropdown (filtered to available indigenous layouts)
  - All other fields set automatically from the preset.

#### D2. HomeScreen simplification

- [ ] Already done partially (Gestures, Extensions removed). Consider further simplifying:
  - Remove "Keyboard" settings (sub-screens for key press feedback, popup settings).
  - Remove "Smartbar" settings (suggestion bar).
  - Remove "Media" (emoji) settings — or keep, emojis are useful).
  - Keep: **Languages & Layouts**, **Theme**, **Clipboard**, **Other**, **About**.

#### D3. Setup screen simplification

- [ ] Already done (notification step removed). Verify the setup flow still works:
  - Step 1: Enable IME → Step 2: Select IME → Step 3: Finish.
  - If `Steps.SelectNotification` is removed, the `Steps` enum and `steps()` function must be updated.

---

### E. INDIGENOUS LANGUAGES OF MEXICO — LAYOUT ADDITION

This is the core of the project. Mexico has 68 recognized indigenous language groups, with over 100+ variants.

#### E1. Create a base "es-MX" or "latin-base" layout

The Spanish keyboard layout (`spanish.json`) is:
```
Row 1: q w e r t y u i o p
Row 2: a s d f g h j k l ñ
Row 3: z x c v b n m
```

This is the ideal starting point for most Mexican indigenous languages, which use the Latin alphabet with additional letters/diacritics.

- [ ] Create `es_mx_base.json` — identical to `spanish.json` initially, or create a shared base.
- [ ] Create a dedicated `subtypePreset` entry:
```json
{
  "languageTag": "es-MX",
  "composer": "org.florisboard.composers:appender",
  "currencySet": "org.florisboard.currencysets:mexican_peso",
  "popupMapping": "org.florisboard.localization:es",
  "preferred": {
    "characters": "org.florisboard.layouts:spanish"
  }
}
```

#### E2. Add individual language layouts

For each indigenous language, you need:
1. A `characters/<language>.json` layout file.
2. An entry in the `"characters"` array in `extension.json`.
3. A `subtypePreset` entry (or entries) in the localization `extension.json`.

**Pro tip for 100+ languages**: Instead of creating 100 separate JSON files, consider these approaches:

- **Option A (per-language JSON files)** — Most flexible. Each language gets its own file with custom keys. Needed for languages with special characters (e.g., Náhuatl: `tl`, `ts`, `tz`, `cu`, `hu`).

- **Option B (shared base with popup extras)** — Use the Spanish layout as base for all, and define language-specific characters only in the popup mapping. This is simpler but less discoverable.

- **Option C (programmatic generation)** — Write a script to generate `extension.json` and JSON layout files from a CSV manifest.

**Minimum viable**: Start with 1 layout per language family (Náhuatl, Maya, Mixtec, Zapotec, Otomí, Totonac, Mazatec, Chinantec, Purepecha, Huastec, etc.) and expand later.

#### E3. Create popup variants

Indigenous languages need popups for accented letters:
- Náhuatl: `ā`, `ē`, `ī`, `ō`, `ū` (long vowels), `tl` digraph
- Maya: `ä`, `ë`, `ö`, `ü`, `ñ`
- Mixtec: various tone markers
- Create `es_mx_popup.json` mapping file that extends the Spanish popup with indigenous-specific characters.

#### E4. Language metadata (subtype presets)

Each language needs a `subtypePreset` entry. Example:
```json
{
  "languageTag": "nah-MX",
  "composer": "org.florisboard.composers:appender",
  "currencySet": "org.florisboard.currencysets:mexican_peso",
  "popupMapping": "org.florisboard.localization:es",
  "preferred": {
    "characters": "org.florisboard.layouts:nahuatl"
  }
}
```

**Note**: Android's `Locale` class may not recognize `nah-MX`, `yua-MX`, `mix-MX` etc. The app uses `FlorisLocale.from()` which wraps Java's `Locale.forLanguageTag()`. Unrecognized locales will still work but won't have system localization (display name will be the tag itself). You may need to:

- [ ] Override `displayName()` for custom locales in `FlorisLocale.kt`.
- [ ] Or provide a hardcoded mapping of language tags → display names.

---

### F. BUILD & DEPLOYMENT

#### F1. Versioning

- [ ] `app/version.properties` — Already has version. Update as needed.
- [ ] `app/build.gradle.kts` — Version scheme `0.9.$code` is fine.

#### F2. CI / GitHub

- [ ] `.github/workflows/` — Remove or update CI workflows for your fork (no Crowdin translations, different test strategy).
- [ ] `.github/FUNDING.yml` — Update or remove.

#### F3. Remove unused modules

- [ ] `lib/native/` — Already disabled in build. The source directory still exists. Optionally delete it.
- [ ] `benchmark/` — Benchmark module. Optional to keep or remove.

---

### G. TESTING & VALIDATION

#### G1. Verify after stripping

- [ ] Build succeeds: `./gradlew assembleDebug`
- [ ] All language layouts removed from JSON don't cause crashes (the app dynamically loads them from `extension.json`).
- [ ] The "Languages & Layouts" screen shows only the kept presets.
- [ ] No crash when tapping any settings screen (even if content is empty/hidden).

#### G2. Verify IME functionality

- [ ] English typing works (QWERTY).
- [ ] Spanish typing works (Ñ key, accented popups).
- [ ] Keyboard switching between languages works.
- [ ] Emoji panel works.
- [ ] Clipboard works.
- [ ] Theme switching works.
- [ ] One-handed mode works.

#### G3. Verify disabled features don't crash

- [ ] Typing screen (if suggestion/spelling prefs are hidden).
- [ ] Smartbar screen (if enabled with no suggestions).
- [ ] Gestures (commented out from HomeScreen but route still exists).

---

### H. FUTURE ENHANCEMENTS (after MVP)

- [ ] Create custom popup mappings with indigenous-specific diacritics (macron, caron, breve, etc.).
- [ ] Create word suggestion dictionaries for indigenous languages.
- [ ] Add glide typing support for Latin-script indigenous languages.
- [ ] Add locale-specific number/date formatting for Mexico.
- [ ] Add voice input integration (if needed).
- [ ] Add a setup wizard for first-time language selection (since 100+ languages is overwhelming).
- [ ] Consider contributing layout files upstream to FlorisBoard once stable.

---

## 📁 KEY FILES REFERENCE

| File | Purpose |
|------|---------|
| `app/build.gradle.kts` | Build config, applicationId, version |
| `app/src/main/AndroidManifest.xml` | App manifest, services, permissions |
| `app/src/main/res/values/strings.xml` | UI strings (Spanish translations in `values-es/`) |
| `app/src/main/kotlin/dev/patrickgold/florisboard/app/AppPrefs.kt` | All user preferences model |
| `app/src/main/kotlin/dev/patrickgold/florisboard/app/Routes.kt` | Navigation routes + composable registrations |
| `app/src/main/kotlin/dev/patrickgold/florisboard/app/settings/HomeScreen.kt` | Main settings home |
| `app/src/main/kotlin/dev/patrickgold/florisboard/app/settings/localization/LocalizationScreen.kt` | Language/layout list |
| `app/src/main/kotlin/dev/patrickgold/florisboard/app/settings/localization/SubtypeEditorScreen.kt` | Add/edit language config |
| `app/src/main/kotlin/dev/patrickgold/florisboard/ime/core/Subtype.kt` | Subtype data model |
| `app/src/main/kotlin/dev/patrickgold/florisboard/ime/core/SubtypeManager.kt` | Subtype storage & management |
| `app/src/main/assets/ime/keyboard/org.florisboard.localization/extension.json` | **ALL subtype presets + popup mappings** |
| `app/src/main/assets/ime/keyboard/org.florisboard.layouts/extension.json` | **ALL keyboard layout registrations** |
| `app/src/main/assets/ime/keyboard/org.florisboard.layouts/layouts/characters/` | **Individual layout JSON files** |
| `app/src/main/assets/ime/languagepack/org.florisboard.languagepack/` | NLP dictionary data |
| `app/src/main/kotlin/dev/patrickgold/florisboard/ime/nlp/latin/LatinLanguageProvider.kt` | Suggestion provider (Latin-based) |

---

## 🔄 ORDER OF EXECUTION

```
Phase 1: Cleanup                         Phase 2: Core Config              Phase 3: UI Simplify
─────────────────────────────            ─────────────────────────────      ─────────────────────────────
  B1. Strip subtype presets                E1. Create es-MX base             D1. Simplify LocalizationScreen
  B2. Strip character layouts              E2. Add 1st indigenous layout     C1. Disable NLP/suggestions
  B3. Strip other layout types             E3. Create popup variant          C2. Remove extension routes
  B4. Strip popup mappings                 E4. Register subtype presets      D2. Further simplify HomeScreen
  B5. Strip currency sets                                                    
  B6. Strip composers                                                       Phase 5: Polish
  B7. Strip language packs                Phase 4: Code Tidy                ─────────────────────────────
                                           ─────────────────────────────       A1-A3. Final branding pass
                                           F1-F3. Build config cleanup        G1-G3. Testing & validation
                                           H. Prepare for 100+ languages      README & docs update
```

---

## 🚩 HIGH-RISK AREAS (proceed with caution)

1. **SubtypeManager / Subtype serialization** — Subtypes are stored as JSON in JetPref DataStore. If you change the Subtype data class or remove fields, existing user data may crash on restore. Always handle migration or clear data between builds.

2. **Asset extension.json format** — The app parses extension JSONs at startup. A malformed JSON (missing comma, extra comma) will silently fail to load ALL layouts. Always validate JSON after editing.

3. **Composer references** — If you remove composers (telex, hangul) but a subtypePreset still references them, the app will fall back to `appender` silently. Be clean about pruning.

4. **Route removal** — If you remove a `composable` call from `Routes.kt` but a deep link still exists, navigating to that route will crash. Remove both simultaneously.

5. **Preference model changes** — `AppPrefs.kt` uses JetPref annotation processing. Adding/removing fields requires a clean build (`./gradlew clean`). Removing a preference that has a default value referenced in `build.gradle.kts` (e.g., `prefs.suggestion.enabled`) will cause compilation errors if referenced in UI code.

---

*Generated by pi coding agent — 2026-06-22*
