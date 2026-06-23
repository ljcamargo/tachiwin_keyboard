#!/usr/bin/env python3
"""
Generate indigenous Mexican keyboard layout files and extension manifests
for the Tachiwin Tsokgnan keyboard project.

Creates a NEW standalone extension at assets/ime/keyboard/org.tachiwin.tsokgnan/
that does NOT modify any existing org.florisboard.* files.

Input:
  - new_layouts2.json  : dict of {iso3_code: [[keys_row0], [keys_row1], [keys_row2]]}
  - catalogue.json     : list of language metadata entries with 'code', 'inali_name', etc.

Output (all NEW files, never modifies existing):
  - assets/ime/keyboard/org.tachiwin.tsokgnan/layouts/characters/{code}.json
  - assets/ime/keyboard/org.tachiwin.tsokgnan/extension.json
"""

import json
import sys
from pathlib import Path

# ------------------------------------------------------------
# Configuration
# ------------------------------------------------------------
PROJECT_ROOT = Path(__file__).resolve().parent.parent

LAYOUTS_SRC = PROJECT_ROOT / "new_layouts2.json"
CATALOGUE_SRC = PROJECT_ROOT / "catalogue.json"

NEW_EXT_ID = "org.tachiwin.tsokgnan"
NEW_EXT_DIR = PROJECT_ROOT / "app" / "src" / "main" / "assets" / "ime" / "keyboard" / NEW_EXT_ID
NEW_LAYOUTS_DIR = NEW_EXT_DIR / "layouts" / "characters"

# European/non-indigenous codes to EXCLUDE from the indigenous extension
NON_INDIGENOUS_CODES = {"eng", "deu", "fra", "es-MX"}


def load_json(path):
    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)


def save_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")
    print(f"  ✏️  {path.relative_to(PROJECT_ROOT)}")


def build_catalogue_map(catalogue):
    """Build a map of code -> entry from catalogue, handling asterisk suffixes."""
    mapping = {}
    for entry in catalogue:
        code = entry["code"]
        base_code = code.rstrip("*")
        mapping[base_code] = entry
        if code != base_code:
            mapping[code] = entry
    return mapping


def get_label(code, entry):
    """Get the best display name for a language."""
    if entry:
        return (entry.get("inali_name", "") or
                entry.get("spanish_name", "") or
                entry.get("english_name", "") or
                code.upper()).title()
    return code.upper()


def main():
    print("=" * 60)
    print("Tachiwin — Indigenous Keyboard Layout Generator")
    print("=" * 60)

    # --------------------------------------------------------
    # 1. Load input data
    # --------------------------------------------------------
    print("\n📂 Loading data files...")
    if not LAYOUTS_SRC.exists():
        print(f"  ❌ {LAYOUTS_SRC} not found!")
        sys.exit(1)
    if not CATALOGUE_SRC.exists():
        print(f"  ❌ {CATALOGUE_SRC} not found!")
        sys.exit(1)

    layouts_data = load_json(LAYOUTS_SRC)
    catalogue = load_json(CATALOGUE_SRC)

    print(f"  📄 Layouts: {len(layouts_data)} languages in new_layouts2.json")
    print(f"  📄 Catalogue: {len(catalogue)} entries in catalogue.json")

    # Filter out non-indigenous codes
    layout_codes = sorted(c for c in layouts_data.keys() if c not in NON_INDIGENOUS_CODES)
    excluded = [c for c in layouts_data if c in NON_INDIGENOUS_CODES]
    print(f"  🗑️  Excluded {len(excluded)} non-indigenous: {excluded}")

    # --------------------------------------------------------
    # 2. Build catalogue lookup
    # --------------------------------------------------------
    catalogue_map = build_catalogue_map(catalogue)
    matched = sum(1 for c in layout_codes if c in catalogue_map)
    unmatched = [c for c in layout_codes if c not in catalogue_map]
    print(f"\n🔍 Catalogue matching: {matched}/{len(layout_codes)} matched")
    if unmatched:
        print(f"  ⚠️  {len(unmatched)} unmatched (still generated, code used as label): {unmatched}")

    # --------------------------------------------------------
    # 3. Generate individual layout JSON files
    # --------------------------------------------------------
    print(f"\n📝 Generating {len(layout_codes)} layout files...")
    for code in layout_codes:
        layout = layouts_data.get(code)
        if not layout:
            print(f"  ⚠️  No layout data for '{code}', skipping")
            continue
        save_json(NEW_LAYOUTS_DIR / f"{code}.json", layout)

    # --------------------------------------------------------
    # 4. Build the complete extension.json
    # --------------------------------------------------------
    print(f"\n📦 Building extension manifest...")

    # 4a. Characters layout list
    characters = []
    for code in layout_codes:
        entry = catalogue_map.get(code)
        characters.append({
            "id": code,
            "label": get_label(code, entry),
            "authors": ["Tachiwin Tutunakú"],
            "direction": "ltr",
        })

    # 4b. Subtype presets (embed in this extension, don't touch org.florisboard.localization)
    subtype_presets = []
    for code in layout_codes:
        entry = catalogue_map.get(code)
        label = get_label(code, entry)
        language_tag = f"{code}-MX"
        subtype_presets.append({
            "languageTag": language_tag,
            "composer": "org.florisboard.composers:appender",
            "currencySet": f"{NEW_EXT_ID}:mexican_peso",
            "popupMapping": "org.florisboard.localization:es",
            "preferred": {
                "characters": f"{NEW_EXT_ID}:{code}",
            },
        })

    # 4c. Mexican Peso currency set (embed in this extension)
    # Uses 'slots' (List<TextKeyData>) NOT 'symbols' (List<String>)
    currency_sets = [
        {
            "id": "mexican_peso",
            "label": "Mexican Peso ($)",
            "slots": [
                {"code": 36, "label": "$"},
                {"code": 162, "label": "¢"},
                {"code": 8364, "label": "€"},
                {"code": 163, "label": "£"},
                {"code": 165, "label": "¥"},
                {"code": 8369, "label": "₱"},
            ],
        },
    ]

    # 4d. Assemble the extension
    extension = {
        "$": "ime.extension.keyboard",
        "meta": {
            "id": NEW_EXT_ID,
            "version": "0.1.0",
            "title": "Lenguas Indígenas de México",
            "description": "Distribuciones de teclado para lenguas indígenas mexicanas.",
            "maintainers": ["Luis J Camargo <lsjcp@yahoo.com>"],
            "license": "apache-2.0",
        },
        "dependencies": [
            "org.florisboard.composers",
            "org.florisboard.currencysets",
            "org.florisboard.layouts",
            "org.florisboard.localization",
        ],
        "currencySets": currency_sets,
        "layouts": {
            "characters": characters,
        },
        "subtypePresets": subtype_presets,
    }

    save_json(NEW_EXT_DIR / "extension.json", extension)

    # --------------------------------------------------------
    # Summary
    # --------------------------------------------------------
    print("\n" + "=" * 60)
    print("✅ Done! Zero existing files were modified.")
    print("=" * 60)
    print(f"""
📁 New extension created (independently, no original files touched):
  {NEW_EXT_DIR.relative_to(PROJECT_ROOT)}/

  ├── extension.json
  │     • Extension ID: {NEW_EXT_ID}
  │     • {len(characters)} character layouts registered
  │     • {len(subtype_presets)} subtype presets (the app merges these automatically)
  │     • mexican_peso currency set included
  │     • References popupMapping from org.florisboard.localization:es
  │
  └── layouts/characters/
        • {len(layout_codes)} individual layout JSON files

To verify: ./gradlew assembleDebug
""")
    print(f"  Script saved at: scripts/generate_indigenous_layouts.py (keep for later use)\n")


if __name__ == "__main__":
    main()
