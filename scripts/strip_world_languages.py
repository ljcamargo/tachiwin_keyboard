#!/usr/bin/env python3
"""
Phase B: Strip non-indigenous world languages from the existing FlorisBoard extensions.
Keep only: en-US, en-UK, en-CA, en-AU, es-ES, es-US, es-419, pt-BR
plus any indigenous presets already added by Phase A.

This script modifies:
  - org.florisboard.layouts/extension.json (layout registrations)
  - org.florisboard.layouts/layouts/characters/*.json (actual layout files)
  - org.florisboard.localization/extension.json (popup mappings + subtype presets)
  - org.florisboard.localization/popupMappings/*.json (popup mapping files)
  - org.florisboard.currencysets/extension.json (currency sets)
  - org.florisboard.composers/extension.json (composers)
"""

import json, os, shutil
from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parent.parent
ASSETS = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "ime" / "keyboard"

LAYOUTS_EXT = ASSETS / "org.florisboard.layouts"
LOCALIZATION_EXT = ASSETS / "org.florisboard.localization"
CURRENCYSETS_EXT = ASSETS / "org.florisboard.currencysets"
COMPOSERS_EXT = ASSETS / "org.florisboard.composers"

# --- Layout IDs to KEEP (characters) ---
KEEP_CHARACTER_LAYOUTS = {
    "qwerty",      # English base
    "spanish",     # Spanish base
    "azerty",      # French base (pt-BR uses qwerty)
    "qwertz",      # German/Austrian base
}
# These are needed by the kept locales for symbols, numeric, etc.
KEEP_OTHER_LAYOUTS = {
    "default",     # charactersMod, symbolsMod, symbols2Mod
    "western",     # symbols, symbols2
    "western_arabic",  # numeric, numericAdvanced, numericRow
    "western_arabic_pc",
    "telpad",      # phone, phone2
    "western_additional_symbols",
    "western_samsung",
    "eastern",     # for RTL languages? Keep just in case
    "clipboard_cursor_row",  # extension type
}
# Combined keep set for all layout types
KEEP_CHARACTER_MOD = {"default"}
KEEP_SYMBOLS = {"western", "western_additional_symbols", "western_samsung"}
KEEP_SYMBOLS_MOD = {"default"}
KEEP_SYMBOLS2 = {"western", "western_samsung"}
KEEP_SYMBOLS2_MOD = {"default"}
KEEP_NUMERIC = {"western_arabic", "western_arabic_pc"}
KEEP_NUMERIC_ADVANCED = {"western_arabic", "western_arabic_pc"}
KEEP_NUMERIC_ROW = {"western_arabic"}
KEEP_PHONE = {"telpad"}
KEEP_PHONE2 = {"telpad"}
KEEP_EXTENSION = {"clipboard_cursor_row"}

# --- Popup mappings to KEEP ---
KEEP_POPUP_MAPPINGS = {"default", "en", "es", "pt", "pt-BR"}

# --- Subtype preset locale tags to KEEP ---
KEEP_PRESET_TAGS = {
    "en-US", "en-UK", "en-CA", "en-AU",
    "es-ES", "es-US", "es-419",
    "pt-BR", "pt-PT",
}

# --- Currency sets to KEEP ---
KEEP_CURRENCY_SETS = {
    "dollar", "euro", "pound", "mexican_peso",
}

# --- Composers to KEEP (by their '$' type) ---
KEEP_COMPOSER_TYPES = {"appender"}


def load_json(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def save_json(path, data):
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def filter_layout_list(layouts_list, keep_ids):
    """Filter a list of layout dicts to only those with 'id' in keep_ids."""
    removed = []
    kept = []
    for layout in layouts_list:
        lid = layout["id"]
        if lid in keep_ids:
            kept.append(layout)
        else:
            removed.append(lid)
    return kept, removed


def strip_extension_json():
    """Strip the org.florisboard.layouts/extension.json to only needed layouts."""
    path = LAYOUTS_EXT / "extension.json"
    ext = load_json(path)
    layout_sections = ext.get("layouts", {})

    strippable = {
        "characters": KEEP_CHARACTER_LAYOUTS,
        "charactersMod": KEEP_CHARACTER_MOD,
        "symbols": KEEP_SYMBOLS,
        "symbolsMod": KEEP_SYMBOLS_MOD,
        "symbols2": KEEP_SYMBOLS2,
        "symbols2Mod": KEEP_SYMBOLS2_MOD,
        "numeric": KEEP_NUMERIC,
        "numericAdvanced": KEEP_NUMERIC_ADVANCED,
        "numericRow": KEEP_NUMERIC_ROW,
        "phone": KEEP_PHONE,
        "phone2": KEEP_PHONE2,
        "extension": KEEP_EXTENSION,
    }

    total_removed = 0
    for section, keep_set in strippable.items():
        if section in layout_sections:
            kept, removed = filter_layout_list(layout_sections[section], keep_set)
            layout_sections[section] = kept
            total_removed += len(removed)
            if removed:
                print(f"  🗑️  {section}: removed {len(removed)} layouts ({', '.join(removed)})")

    ext["layouts"] = layout_sections
    save_json(path, ext)
    print(f"  ✏️  Updated {path.relative_to(PROJECT_ROOT)}")
    return total_removed


def delete_unused_layout_files():
    """Delete JSON files for layouts we no longer need."""
    chars_dir = LAYOUTS_EXT / "layouts" / "characters"
    deleted = 0
    for f in chars_dir.glob("*.json"):
        layout_id = f.stem
        if layout_id not in KEEP_CHARACTER_LAYOUTS:
            os.remove(f)
            deleted += 1
    print(f"  🗑️  Deleted {deleted} character layout JSON files (kept only: {', '.join(sorted(KEEP_CHARACTER_LAYOUTS))})")


def strip_localization_extension():
    """Keep only needed popup mappings and subtype presets."""
    path = LOCALIZATION_EXT / "extension.json"
    ext = load_json(path)

    # --- Popup mappings ---
    popups = ext.get("popupMappings", [])
    kept_popups = [p for p in popups if p["id"] in KEEP_POPUP_MAPPINGS]
    removed_popups = [p["id"] for p in popups if p["id"] not in KEEP_POPUP_MAPPINGS]
    ext["popupMappings"] = kept_popups
    if removed_popups:
        print(f"  🗑️  popupMappings: removed {len(removed_popups)} ({', '.join(removed_popups[:10])}{'...' if len(removed_popups)>10 else ''})")

    # --- Subtype presets: KEEP specified + any indigenous ones ---
    presets = ext.get("subtypePresets", [])
    kept_presets = []
    indigenous_count = 0
    for p in presets:
        tag = p.get("languageTag", "")
        if tag in KEEP_PRESET_TAGS:
            kept_presets.append(p)
        elif tag.endswith("-MX"):  # indigenous presets
            kept_presets.append(p)
            indigenous_count += 1
        # else: drop it
    removed_presets = len(presets) - len(kept_presets)
    ext["subtypePresets"] = kept_presets
    if removed_presets:
        print(f"  🗑️  subtypePresets: removed {removed_presets} (kept {len(kept_presets)} — {indigenous_count} indigenous, {len(KEEP_PRESET_TAGS)} standard)")

    # --- Punctuation rules: keep "default" ---
    punct = ext.get("punctuationRules", [])
    kept_punct = [p for p in punct if p["id"] == "default"]
    ext["punctuationRules"] = kept_punct

    save_json(path, ext)
    print(f"  ✏️  Updated {path.relative_to(PROJECT_ROOT)}")


def delete_unused_popup_mapping_files():
    """Delete popup mapping JSON files for removed mappings."""
    popup_dir = LOCALIZATION_EXT / "popupMappings"
    deleted = 0
    for f in popup_dir.glob("*.json"):
        mapping_id = f.stem
        if mapping_id not in KEEP_POPUP_MAPPINGS:
            os.remove(f)
            deleted += 1
    print(f"  🗑️  Deleted {deleted} popup mapping JSON files (kept: {', '.join(sorted(KEEP_POPUP_MAPPINGS))})")


def strip_currency_sets():
    """Keep only needed currency sets."""
    path = CURRENCYSETS_EXT / "extension.json"
    ext = load_json(path)
    csets = ext.get("currencySets", [])
    kept = [c for c in csets if c["id"] in KEEP_CURRENCY_SETS]
    removed = [c["id"] for c in csets if c["id"] not in KEEP_CURRENCY_SETS]
    ext["currencySets"] = kept
    if removed:
        print(f"  🗑️  currencySets: removed {len(removed)} ({', '.join(removed)})")
    save_json(path, ext)
    print(f"  ✏️  Updated {path.relative_to(PROJECT_ROOT)}")


def strip_composers():
    """Keep only needed composers (by their '$' type identifier)."""
    path = COMPOSERS_EXT / "extension.json"
    ext = load_json(path)
    comps = ext.get("composers", [])
    kept = []
    removed = []
    for c in comps:
        ctype = c.get("$", "")
        if ctype in KEEP_COMPOSER_TYPES:
            kept.append(c)
        else:
            removed.append(ctype)
    ext["composers"] = kept
    if removed:
        print(f"  🗑️  composers: removed {len(removed)} ({', '.join(removed)})")
    save_json(path, ext)
    print(f"  ✏️  Updated {path.relative_to(PROJECT_ROOT)}")


def main():
    print("=" * 60)
    print("Phase B: Strip unwanted world languages")
    print("=" * 60)

    print("\n📦 org.florisboard.layouts (layout registrations)...")
    r = strip_extension_json()
    delete_unused_layout_files()

    print("\n📦 org.florisboard.localization (popup mappings + presets)...")
    strip_localization_extension()
    delete_unused_popup_mapping_files()

    print("\n📦 org.florisboard.currencysets...")
    strip_currency_sets()

    print("\n📦 org.florisboard.composers...")
    strip_composers()

    print("\n" + "=" * 60)
    print("✅ Phase B complete!")
    print("=" * 60)
    print()
    print("Summary:")
    print("  • org.florisboard.layouts: only qwerty, spanish, basic layouts kept")
    print("  • org.florisboard.localization: only en, es, pt popups + selected + indigenous presets")
    print("  • org.florisboard.currencysets: only dollar, euro, pound, mexican_peso")
    print("  • org.florisboard.composers: only appender")
    print()
    print("Next: run ./gradlew assembleDebug to verify build.")


if __name__ == "__main__":
    main()
